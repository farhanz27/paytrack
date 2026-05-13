package com.avantdream.paytrack.invoice.controller;

import java.net.URI;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avantdream.paytrack.catalog.entity.CatalogItem;
import com.avantdream.paytrack.catalog.service.CatalogItemService;
import com.avantdream.paytrack.customer.entity.Customer;
import com.avantdream.paytrack.customer.service.CustomerService;
import com.avantdream.paytrack.invoice.dto.InvoiceResponse;
import com.avantdream.paytrack.invoice.dto.InvoiceSaveRequest;
import com.avantdream.paytrack.invoice.dto.InvoiceStatusRequest;
import com.avantdream.paytrack.invoice.dto.InvoiceSummaryResponse;
import com.avantdream.paytrack.invoice.entity.Invoice;
import com.avantdream.paytrack.invoice.entity.InvoiceItem;
import com.avantdream.paytrack.invoice.entity.InvoiceStatus;
import com.avantdream.paytrack.invoice.mapper.InvoiceMapper;
import com.avantdream.paytrack.invoice.service.InvoiceService;
import com.avantdream.paytrack.invoice.service.InvoiceServiceImpl;
import com.avantdream.paytrack.pdf.service.InvoicePdfService;
import com.avantdream.paytrack.shared.dto.PageResponse;
import com.avantdream.paytrack.shared.exception.BadRequestException;
import com.avantdream.paytrack.shared.workspace.WorkspaceAccess;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

	private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);
	public static final String COMPANY_HEADER = "X-Company-Id";

	private final CatalogItemService catalogItemService;
	private final CustomerService customerService;
	private final InvoiceService invoiceService;
	private final InvoicePdfService invoicePdfService;
	private final WorkspaceAccess workspaceAccess;

	public InvoiceController(CatalogItemService catalogItemService, CustomerService customerService,
			InvoiceService invoiceService, InvoicePdfService invoicePdfService, WorkspaceAccess workspaceAccess) {
		this.catalogItemService = catalogItemService;
		this.customerService = customerService;
		this.invoiceService = invoiceService;
		this.invoicePdfService = invoicePdfService;
		this.workspaceAccess = workspaceAccess;
	}

	@Secured("ROLE_USER")
	@GetMapping
	public ResponseEntity<PageResponse<InvoiceSummaryResponse>> list(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@RequestParam(required = false) InvoiceStatus status,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		return ResponseEntity.ok(PageResponse.from(invoiceService.findAll(companyId, status, pageable).map(InvoiceMapper::toSummaryResponse)));
	}

	@Secured("ROLE_USER")
	@GetMapping("/{id}")
	public ResponseEntity<InvoiceResponse> view(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		return ResponseEntity.ok(InvoiceMapper.toResponse(invoiceService.findByIdWithDetails(companyId, id)));
	}

	@Secured("ROLE_USER")
	@PostMapping
	public ResponseEntity<InvoiceResponse> create(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@Valid @RequestBody InvoiceSaveRequest request) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);

		Customer customer = customerService.findById(companyId, request.getCustomerId());
		if (customer.getArchivedAt() != null) {
			throw new BadRequestException("Cannot create invoice for archived customer");
		}

		Invoice invoice = new Invoice();
		invoice.setCustomer(customer);
		invoice.setCompany(customer.getCompany());
		invoice.setNotes(request.getNotes());
		invoice.setIssueDate(request.getIssueDate());
		invoice.setDueDate(request.getDueDate());
		if (request.getCurrency() != null) {
			invoice.setCurrency(request.getCurrency());
		}
		invoice.setDiscount(request.getDiscount());
		invoice.setTax(request.getTax());

		InvoiceServiceImpl.applyCustomerSnapshot(invoice, customer);

		for (var line : request.getLines()) {
			if (line.getCatalogItemId() != null) {
				CatalogItem catalogItem = catalogItemService.findById(companyId, line.getCatalogItemId());
				if (catalogItem.getArchivedAt() != null) {
					throw new BadRequestException("Cannot use archived catalog item: " + catalogItem.getName());
				}
			}
			InvoiceItem item = new InvoiceItem();
			item.setDescription(line.getDescription());
			item.setUnitPrice(line.getUnitPrice());
			item.setQuantity(line.getQuantity());
			invoice.addItem(item);
		}

		Invoice saved = invoiceService.save(invoice, principal.getUsername());
		try { invoicePdfService.generateAndStore(companyId, saved.getId()); }
		catch (Exception e) { log.warn("PDF generation failed for invoice {}", saved.getId(), e); }
		return ResponseEntity.created(URI.create("/api/invoices/" + saved.getId()))
				.body(InvoiceMapper.toResponse(saved));
	}

	@Secured("ROLE_USER")
	@PutMapping("/{id}")
	public ResponseEntity<InvoiceResponse> update(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id,
			@Valid @RequestBody InvoiceSaveRequest request) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		Invoice updated = invoiceService.update(companyId, id, request);
		try { invoicePdfService.generateAndStore(companyId, updated.getId()); }
		catch (Exception e) { log.warn("PDF generation failed for invoice {}", updated.getId(), e); }
		return ResponseEntity.ok(InvoiceMapper.toResponse(updated));
	}

	@Secured("ROLE_USER")
	@PutMapping("/{id}/status")
	public ResponseEntity<InvoiceResponse> transitionStatus(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id,
			@Valid @RequestBody InvoiceStatusRequest request) {

		InvoiceStatus target = request.getStatus();
		if (target == InvoiceStatus.PAID || target == InvoiceStatus.PARTIALLY_PAID) {
			throw new BadRequestException("Status " + target + " can only be set through payment processing");
		}

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		Invoice updated = invoiceService.transitionStatus(companyId, id, target, principal.getUsername());
		try { invoicePdfService.generateAndStore(companyId, updated.getId()); }
		catch (Exception e) { log.warn("PDF generation failed for invoice {} after status change", updated.getId(), e); }
		return ResponseEntity.ok(InvoiceMapper.toResponse(updated));
	}

	@Secured("ROLE_USER")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		invoiceService.delete(companyId, id);
		return ResponseEntity.noContent().build();
	}

}
