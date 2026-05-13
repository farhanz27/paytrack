package com.avantdream.paytrack.quotation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avantdream.paytrack.company.repository.CompanyRepository;
import com.avantdream.paytrack.catalog.entity.CatalogItem;
import com.avantdream.paytrack.catalog.repository.CatalogItemRepository;
import com.avantdream.paytrack.customer.entity.Customer;
import com.avantdream.paytrack.customer.repository.CustomerRepository;
import com.avantdream.paytrack.invoice.entity.Invoice;
import com.avantdream.paytrack.invoice.entity.InvoiceItem;
import com.avantdream.paytrack.invoice.entity.InvoiceStatusLog;
import com.avantdream.paytrack.invoice.repository.InvoiceRepository;
import com.avantdream.paytrack.invoice.repository.InvoiceStatusLogRepository;
import com.avantdream.paytrack.quotation.dto.QuotationSaveRequest;
import com.avantdream.paytrack.quotation.entity.Quotation;
import com.avantdream.paytrack.quotation.entity.QuotationItem;
import com.avantdream.paytrack.quotation.entity.QuotationStatus;
import com.avantdream.paytrack.quotation.repository.QuotationRepository;
import com.avantdream.paytrack.shared.exception.BadRequestException;
import com.avantdream.paytrack.shared.exception.ResourceNotFoundException;

@Service
public class QuotationServiceImpl implements QuotationService {

	private final QuotationRepository quotationRepository;
	private final CompanyRepository companyRepository;
	private final CustomerRepository customerRepository;
	private final CatalogItemRepository catalogItemRepository;
	private final InvoiceRepository invoiceRepository;
	private final InvoiceStatusLogRepository invoiceStatusLogRepository;

	public QuotationServiceImpl(
			QuotationRepository quotationRepository,
			CompanyRepository companyRepository,
			CustomerRepository customerRepository,
			CatalogItemRepository catalogItemRepository,
			InvoiceRepository invoiceRepository,
			InvoiceStatusLogRepository invoiceStatusLogRepository) {
		this.quotationRepository = quotationRepository;
		this.companyRepository = companyRepository;
		this.customerRepository = customerRepository;
		this.catalogItemRepository = catalogItemRepository;
		this.invoiceRepository = invoiceRepository;
		this.invoiceStatusLogRepository = invoiceStatusLogRepository;
	}

	@Override
	@Transactional
	public Quotation save(Quotation quotation, String createdBy) {
		if (quotation.getCustomer() != null && quotation.getCustomer().getArchivedAt() != null) {
			throw new BadRequestException("Cannot create quotation for archived customer");
		}
		computeTotals(quotation);
		assignQuotationNumber(quotation);
		return quotationRepository.save(quotation);
	}

	@Override
	@Transactional(readOnly = true)
	public Quotation findById(Long companyId, Long id) {
		return quotationRepository.findByIdAndCompany_Id(id, companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Quotation", id));
	}

	@Override
	@Transactional(readOnly = true)
	public Quotation findByIdWithDetails(Long companyId, Long id) {
		Quotation quotation = quotationRepository.fetchByIdWithCustomerAndItems(id, companyId);
		if (quotation == null) {
			throw new ResourceNotFoundException("Quotation", id);
		}
		return quotation;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Quotation> findAll(Long companyId, QuotationStatus status, Pageable pageable) {
		if (status != null) {
			return quotationRepository.findByCompany_IdAndStatus(companyId, status, pageable);
		}
		return quotationRepository.findByCompany_Id(companyId, pageable);
	}

	@Override
	@Transactional
	public Quotation update(Long companyId, Long id, QuotationSaveRequest request) {
		Quotation quotation = findByIdWithDetails(companyId, id);

		if (quotation.getStatus() != QuotationStatus.DRAFT) {
			throw new BadRequestException("Quotation can only be edited in DRAFT status");
		}

		Customer customer = customerRepository.findByIdAndCompany_Id(request.getCustomerId(), companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));
		if (customer.getArchivedAt() != null) {
			throw new BadRequestException("Cannot use archived customer");
		}

		quotation.setCustomer(customer);
		applyCustomerSnapshot(quotation, customer);
		quotation.setIssueDate(request.getIssueDate());
		quotation.setValidUntil(request.getValidUntil());
		quotation.setCurrency(request.getCurrency() != null ? request.getCurrency() : "MYR");
		quotation.setDiscount(request.getDiscount());
		quotation.setTax(request.getTax());
		quotation.setNotes(request.getNotes());

		quotation.getItems().clear();
		for (var line : request.getLines()) {
			if (line.getCatalogItemId() != null) {
				CatalogItem catalogItem = catalogItemRepository.findByIdAndCompany_Id(line.getCatalogItemId(), companyId)
						.orElseThrow(() -> new ResourceNotFoundException("CatalogItem", line.getCatalogItemId()));
				if (catalogItem.getArchivedAt() != null) {
					throw new BadRequestException("Cannot use archived catalog item: " + catalogItem.getName());
				}
			}
			QuotationItem item = new QuotationItem();
			item.setName(line.getName());
			item.setDescription(line.getDescription());
			item.setUnitPrice(line.getUnitPrice());
			item.setQuantity(line.getQuantity());
			quotation.addItem(item);
		}

		computeTotals(quotation);
		return quotationRepository.save(quotation);
	}

	@Override
	@Transactional
	public Quotation transitionStatus(Long companyId, Long id, QuotationStatus newStatus, String changedBy) {
		Quotation quotation = findById(companyId, id);
		QuotationStatus current = quotation.getStatus();

		if (!current.canTransitionTo(newStatus)) {
			throw new BadRequestException(
				"Cannot transition quotation from " + current + " to " + newStatus);
		}

		quotation.setStatus(newStatus);
		return quotationRepository.save(quotation);
	}

	@Override
	@Transactional
	public void delete(Long companyId, Long id) {
		Quotation quotation = findById(companyId, id);
		if (quotation.getStatus() != QuotationStatus.DRAFT) {
			throw new BadRequestException("Only DRAFT quotations can be deleted");
		}
		quotationRepository.deleteById(quotation.getId());
	}

	@Override
	@Transactional
	public Invoice convertToInvoice(Long companyId, Long id, String convertedBy) {
		Quotation quotation = findByIdWithDetails(companyId, id);

		if (quotation.getStatus() != QuotationStatus.ACCEPTED) {
			throw new BadRequestException("Only ACCEPTED quotations can be converted to invoice");
		}

		// Idempotency: return existing invoice if already converted
		Optional<Invoice> existing = invoiceRepository.findBySourceQuotationIdAndCompany_Id(id, companyId);
		if (existing.isPresent()) {
			return existing.get();
		}

		Invoice invoice = new Invoice();
		invoice.setCompany(quotation.getCompany());
		invoice.setCustomer(quotation.getCustomer());
		invoice.setCustomerName(quotation.getCustomerName());
		invoice.setCustomerCompany(quotation.getCustomerCompany());
		invoice.setCustomerEmail(quotation.getCustomerEmail());
		invoice.setBillingAddress(quotation.getBillingAddress());
		invoice.setIssueDate(new Date());
		invoice.setCurrency(quotation.getCurrency() != null ? quotation.getCurrency() : "MYR");
		invoice.setDiscount(quotation.getDiscount());
		invoice.setTax(quotation.getTax());
		invoice.setSourceQuotationId(quotation.getId());

		for (QuotationItem qi : quotation.getItems()) {
			InvoiceItem item = new InvoiceItem();
			item.setName(qi.getName());
			item.setDescription(qi.getDescription());
			item.setUnitPrice(qi.getUnitPrice());
			item.setQuantity(qi.getQuantity());
			invoice.addItem(item);
		}

		computeInvoiceTotals(invoice);
		assignInvoiceNumber(invoice);

		Invoice saved = invoiceRepository.save(invoice);

		InvoiceStatusLog log = new InvoiceStatusLog();
		log.setInvoiceId(saved.getId());
		log.setFromStatus(null);
		log.setToStatus(saved.getStatus());
		log.setChangedBy(convertedBy);
		invoiceStatusLogRepository.save(log);

		return saved;
	}

	private void computeTotals(Quotation quotation) {
		BigDecimal subtotal = BigDecimal.ZERO;
		for (QuotationItem item : quotation.getItems()) {
			BigDecimal itemSubtotal = item.computeSubtotal();
			item.setSubtotal(itemSubtotal);
			subtotal = subtotal.add(itemSubtotal);
		}
		quotation.setSubtotal(subtotal);

		BigDecimal discount = quotation.getDiscount() != null ? quotation.getDiscount() : BigDecimal.ZERO;
		BigDecimal tax = quotation.getTax() != null ? quotation.getTax() : BigDecimal.ZERO;

		BigDecimal afterDiscount = subtotal.subtract(discount);
		if (afterDiscount.compareTo(BigDecimal.ZERO) < 0) {
			afterDiscount = BigDecimal.ZERO;
		}

		quotation.setGrandTotal(afterDiscount.add(tax).setScale(2, RoundingMode.HALF_UP));
	}

	private void computeInvoiceTotals(Invoice invoice) {
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
		invoice.setPaidAmount(BigDecimal.ZERO);
		invoice.setRemainingAmount(grandTotal);
	}

	private void assignQuotationNumber(Quotation quotation) {
		if (quotation.getQuotationNumber() != null && !quotation.getQuotationNumber().isBlank()) {
			return;
		}
		Long companyId = quotation.getCompany().getId();
		companyRepository.lockById(companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
		int year = LocalDate.now().getYear();
		long count = quotationRepository.countByCompany_IdAndYear(companyId, year) + 1;
		quotation.setQuotationNumber(String.format("QUO-%d-%05d", year, count));
	}

	private void assignInvoiceNumber(Invoice invoice) {
		Long companyId = invoice.getCompany().getId();
		companyRepository.lockById(companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
		int year = LocalDate.now().getYear();
		long count = invoiceRepository.countByCompany_IdAndYear(companyId, year) + 1;
		invoice.setInvoiceNumber(String.format("INV-%d-%05d", year, count));
	}

	public static void applyCustomerSnapshot(Quotation quotation, Customer customer) {
		quotation.setCustomerName(customer.getName());
		quotation.setCustomerCompany(customer.getCompanyName());
		quotation.setCustomerEmail(customer.getEmail());

		StringBuilder addr = new StringBuilder();
		append(addr, customer.getBillingAddressLine1());
		append(addr, customer.getBillingAddressLine2());
		append(addr, customer.getBillingPostcode());
		append(addr, customer.getBillingCity());
		append(addr, customer.getBillingState());
		append(addr, customer.getBillingCountry());
		quotation.setBillingAddress(addr.toString());
	}

	private static void append(StringBuilder sb, String part) {
		if (part != null && !part.isBlank()) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(part.trim());
		}
	}

}
