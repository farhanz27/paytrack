package com.avantdream.paytrack.invoice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avantdream.paytrack.company.repository.CompanyRepository;
import com.avantdream.paytrack.catalog.entity.CatalogItem;
import com.avantdream.paytrack.catalog.repository.CatalogItemRepository;
import com.avantdream.paytrack.customer.entity.Customer;
import com.avantdream.paytrack.customer.repository.CustomerRepository;
import com.avantdream.paytrack.invoice.dto.InvoiceSaveRequest;
import com.avantdream.paytrack.invoice.entity.Invoice;
import com.avantdream.paytrack.invoice.entity.InvoiceItem;
import com.avantdream.paytrack.invoice.entity.InvoiceStatus;
import com.avantdream.paytrack.invoice.entity.InvoiceStatusLog;
import com.avantdream.paytrack.invoice.repository.InvoiceRepository;
import com.avantdream.paytrack.invoice.repository.InvoiceStatusLogRepository;
import com.avantdream.paytrack.shared.exception.BadRequestException;
import com.avantdream.paytrack.shared.exception.ResourceNotFoundException;

@Service
public class InvoiceServiceImpl implements InvoiceService {

	private final InvoiceRepository invoiceRepository;
	private final InvoiceStatusLogRepository statusLogRepository;
	private final CompanyRepository companyRepository;
	private final CustomerRepository customerRepository;
	private final CatalogItemRepository catalogItemRepository;

	public InvoiceServiceImpl(
			InvoiceRepository invoiceRepository,
			InvoiceStatusLogRepository statusLogRepository,
			CompanyRepository companyRepository,
			CustomerRepository customerRepository,
			CatalogItemRepository catalogItemRepository) {
		this.invoiceRepository = invoiceRepository;
		this.statusLogRepository = statusLogRepository;
		this.companyRepository = companyRepository;
		this.customerRepository = customerRepository;
		this.catalogItemRepository = catalogItemRepository;
	}

	@Override
	@Transactional
	public Invoice save(Invoice invoice, String changedBy) {
		if (invoice.getCompany() == null && invoice.getCustomer() != null) {
			invoice.setCompany(invoice.getCustomer().getCompany());
		}

		validateNotArchived(invoice);
		computeTotals(invoice);
		assignInvoiceNumber(invoice);

		Invoice saved = invoiceRepository.save(invoice);

		InvoiceStatusLog log = new InvoiceStatusLog();
		log.setInvoiceId(saved.getId());
		log.setFromStatus(null);
		log.setToStatus(saved.getStatus());
		log.setChangedBy(changedBy);
		statusLogRepository.save(log);

		return saved;
	}

	@Override
	@Transactional(readOnly = true)
	public Invoice findById(Long companyId, Long id) {
		return invoiceRepository.findByIdAndCompany_Id(id, companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Invoice> findAll(Long companyId, InvoiceStatus status, Pageable pageable) {
		if (status != null) {
			return invoiceRepository.findByCompany_IdAndStatus(companyId, status, pageable);
		}
		return invoiceRepository.findByCompany_Id(companyId, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Invoice findByIdWithDetails(Long companyId, Long id) {
		Invoice invoice = invoiceRepository.fetchByIdWithCustomerAndItems(id, companyId);
		if (invoice == null) {
			throw new ResourceNotFoundException("Invoice", id);
		}
		return invoice;
	}

	@Override
	@Transactional
	public Invoice update(Long companyId, Long id, InvoiceSaveRequest request) {
		Invoice invoice = findByIdWithDetails(companyId, id);

		if (invoice.getStatus() != InvoiceStatus.DRAFT) {
			throw new BadRequestException("Invoice can only be edited in DRAFT status");
		}

		Customer customer = customerRepository.findByIdAndCompany_Id(request.getCustomerId(), companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));
		if (customer.getArchivedAt() != null) {
			throw new BadRequestException("Cannot use archived customer");
		}

		invoice.setCustomer(customer);
		applyCustomerSnapshot(invoice, customer);
		invoice.setNotes(request.getNotes());
		invoice.setIssueDate(request.getIssueDate());
		invoice.setDueDate(request.getDueDate());
		if (request.getCurrency() != null) {
			invoice.setCurrency(request.getCurrency());
		}
		invoice.setDiscount(request.getDiscount());
		invoice.setTax(request.getTax());

		invoice.getItems().clear();
		for (var line : request.getLines()) {
			if (line.getCatalogItemId() != null) {
				CatalogItem catalogItem = catalogItemRepository.findByIdAndCompany_Id(line.getCatalogItemId(), companyId)
						.orElseThrow(() -> new ResourceNotFoundException("CatalogItem", line.getCatalogItemId()));
				if (catalogItem.getArchivedAt() != null) {
					throw new BadRequestException("Cannot use archived catalog item: " + catalogItem.getName());
				}
			}
			InvoiceItem item = new InvoiceItem();
			item.setName(line.getName());
			item.setDescription(line.getDescription());
			item.setUnitPrice(line.getUnitPrice());
			item.setQuantity(line.getQuantity());
			invoice.addItem(item);
		}

		computeTotals(invoice);
		return invoiceRepository.save(invoice);
	}

	@Override
	@Transactional
	public Invoice transitionStatus(Long companyId, Long id, InvoiceStatus newStatus, String changedBy) {
		Invoice invoice = findById(companyId, id);
		InvoiceStatus current = invoice.getStatus();

		if (!current.canTransitionTo(newStatus)) {
			throw new BadRequestException(
				"Cannot transition invoice from " + current + " to " + newStatus);
		}

		if (newStatus == InvoiceStatus.PAID) {
			BigDecimal remaining = invoice.getRemainingAmount() != null ? invoice.getRemainingAmount() : invoice.getGrandTotal();
			if (remaining.compareTo(BigDecimal.ZERO) != 0) {
				throw new BadRequestException("Invoice cannot be marked as PAID: remaining amount is " + remaining);
			}
		}

		invoice.setStatus(newStatus);

		Date now = new Date();
		if (newStatus == InvoiceStatus.ISSUED && invoice.getIssuedAt() == null) {
			invoice.setIssuedAt(now);
		}
		if (newStatus == InvoiceStatus.PAID && invoice.getPaidAt() == null) {
			invoice.setPaidAt(now);
		}

		Invoice saved = invoiceRepository.save(invoice);

		InvoiceStatusLog log = new InvoiceStatusLog();
		log.setInvoiceId(saved.getId());
		log.setFromStatus(current);
		log.setToStatus(newStatus);
		log.setChangedBy(changedBy);
		statusLogRepository.save(log);

		return saved;
	}

	@Override
	@Transactional
	public void delete(Long companyId, Long id) {
		Invoice invoice = findById(companyId, id);
		if (invoice.getStatus() != InvoiceStatus.DRAFT) {
			throw new BadRequestException("Only DRAFT invoices can be deleted");
		}
		invoiceRepository.deleteById(invoice.getId());
	}

	private void validateNotArchived(Invoice invoice) {
		if (invoice.getCustomer() != null && invoice.getCustomer().getArchivedAt() != null) {
			throw new BadRequestException("Cannot create invoice for archived customer");
		}
	}

	private void computeTotals(Invoice invoice) {
		BigDecimal subtotal = BigDecimal.ZERO;
		for (InvoiceItem item : invoice.getItems()) {
			BigDecimal itemSubtotal = item.computeSubtotal();
			item.setSubtotal(itemSubtotal);
			subtotal = subtotal.add(itemSubtotal);
		}
		invoice.setSubtotal(subtotal);

		BigDecimal discount = invoice.getDiscount() != null ? invoice.getDiscount() : BigDecimal.ZERO;
		BigDecimal tax = invoice.getTax() != null ? invoice.getTax() : BigDecimal.ZERO;

		BigDecimal afterDiscount = subtotal.subtract(discount);
		if (afterDiscount.compareTo(BigDecimal.ZERO) < 0) {
			afterDiscount = BigDecimal.ZERO;
		}

		BigDecimal grandTotal = afterDiscount.add(tax).setScale(2, RoundingMode.HALF_UP);
		invoice.setGrandTotal(grandTotal);

		// Sync remaining amount on creation (paidAmount starts at 0)
		if (invoice.getPaidAmount() == null) {
			invoice.setPaidAmount(BigDecimal.ZERO);
		}
		invoice.setRemainingAmount(grandTotal.subtract(invoice.getPaidAmount()));
	}

	private void assignInvoiceNumber(Invoice invoice) {
		if (invoice.getInvoiceNumber() != null && !invoice.getInvoiceNumber().isBlank()) {
			return;
		}
		Long companyId = invoice.getCompany().getId();
		companyRepository.lockById(companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
		int year = LocalDate.now().getYear();
		long count = invoiceRepository.countByCompany_IdAndYear(companyId, year) + 1;
		invoice.setInvoiceNumber(String.format("INV-%d-%05d", year, count));
	}

	public static void applyCustomerSnapshot(Invoice invoice, Customer customer) {
		invoice.setCustomerName(customer.getName());
		invoice.setCustomerCompany(customer.getCompanyName());
		invoice.setCustomerEmail(customer.getEmail());

		StringBuilder addr = new StringBuilder();
		append(addr, customer.getBillingAddressLine1());
		append(addr, customer.getBillingAddressLine2());
		append(addr, customer.getBillingPostcode());
		append(addr, customer.getBillingCity());
		append(addr, customer.getBillingState());
		append(addr, customer.getBillingCountry());
		invoice.setBillingAddress(addr.toString());
	}

	private static void append(StringBuilder sb, String part) {
		if (part != null && !part.isBlank()) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(part.trim());
		}
	}

}
