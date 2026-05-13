package com.avantdream.paytrack.quotation.controller;

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
import com.avantdream.paytrack.invoice.entity.Invoice;
import com.avantdream.paytrack.invoice.mapper.InvoiceMapper;
import com.avantdream.paytrack.invoice.dto.InvoiceResponse;
import com.avantdream.paytrack.pdf.service.InvoicePdfService;
import com.avantdream.paytrack.pdf.service.QuotationPdfService;
import com.avantdream.paytrack.quotation.dto.QuotationResponse;
import com.avantdream.paytrack.quotation.dto.QuotationSaveRequest;
import com.avantdream.paytrack.quotation.dto.QuotationStatusRequest;
import com.avantdream.paytrack.quotation.dto.QuotationSummaryResponse;
import com.avantdream.paytrack.quotation.entity.Quotation;
import com.avantdream.paytrack.quotation.entity.QuotationItem;
import com.avantdream.paytrack.quotation.entity.QuotationStatus;
import com.avantdream.paytrack.quotation.mapper.QuotationMapper;
import com.avantdream.paytrack.quotation.service.QuotationService;
import com.avantdream.paytrack.quotation.service.QuotationServiceImpl;
import com.avantdream.paytrack.shared.dto.PageResponse;
import com.avantdream.paytrack.shared.exception.BadRequestException;
import com.avantdream.paytrack.shared.workspace.WorkspaceAccess;

@RestController
@RequestMapping("/api/quotations")
public class QuotationController {

	private static final Logger log = LoggerFactory.getLogger(QuotationController.class);
	public static final String COMPANY_HEADER = "X-Company-Id";

	private final CatalogItemService catalogItemService;
	private final CustomerService customerService;
	private final QuotationService quotationService;
	private final QuotationPdfService quotationPdfService;
	private final InvoicePdfService invoicePdfService;
	private final WorkspaceAccess workspaceAccess;

	public QuotationController(CatalogItemService catalogItemService, CustomerService customerService,
			QuotationService quotationService, QuotationPdfService quotationPdfService,
			InvoicePdfService invoicePdfService, WorkspaceAccess workspaceAccess) {
		this.catalogItemService = catalogItemService;
		this.customerService = customerService;
		this.quotationService = quotationService;
		this.quotationPdfService = quotationPdfService;
		this.invoicePdfService = invoicePdfService;
		this.workspaceAccess = workspaceAccess;
	}

	@Secured("ROLE_USER")
	@GetMapping
	public ResponseEntity<PageResponse<QuotationSummaryResponse>> list(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@RequestParam(required = false) QuotationStatus status,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		return ResponseEntity.ok(PageResponse.from(quotationService.findAll(companyId, status, pageable).map(QuotationMapper::toSummaryResponse)));
	}

	@Secured("ROLE_USER")
	@GetMapping("/{id}")
	public ResponseEntity<QuotationResponse> view(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		return ResponseEntity.ok(QuotationMapper.toResponse(quotationService.findByIdWithDetails(companyId, id)));
	}

	@Secured("ROLE_USER")
	@PostMapping
	public ResponseEntity<QuotationResponse> create(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@Valid @RequestBody QuotationSaveRequest request) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);

		Customer customer = customerService.findById(companyId, request.getCustomerId());
		if (customer.getArchivedAt() != null) {
			throw new BadRequestException("Cannot create quotation for archived customer");
		}

		Quotation quotation = new Quotation();
		quotation.setCustomer(customer);
		quotation.setCompany(customer.getCompany());
		quotation.setIssueDate(request.getIssueDate());
		quotation.setValidUntil(request.getValidUntil());
		quotation.setCurrency(request.getCurrency() != null ? request.getCurrency() : "MYR");
		quotation.setDiscount(request.getDiscount());
		quotation.setTax(request.getTax());
		quotation.setNotes(request.getNotes());

		QuotationServiceImpl.applyCustomerSnapshot(quotation, customer);

		for (var line : request.getLines()) {
			if (line.getCatalogItemId() != null) {
				CatalogItem catalogItem = catalogItemService.findById(companyId, line.getCatalogItemId());
				if (catalogItem.getArchivedAt() != null) {
					throw new BadRequestException("Cannot use archived catalog item: " + catalogItem.getName());
				}
			}
			QuotationItem item = new QuotationItem();
			item.setDescription(line.getDescription());
			item.setUnitPrice(line.getUnitPrice());
			item.setQuantity(line.getQuantity());
			quotation.addItem(item);
		}

		Quotation saved = quotationService.save(quotation, principal.getUsername());
		try { quotationPdfService.generateAndStore(companyId, saved.getId()); }
		catch (Exception e) { log.warn("PDF generation failed for quotation {}", saved.getId(), e); }
		return ResponseEntity.created(URI.create("/api/quotations/" + saved.getId()))
				.body(QuotationMapper.toResponse(saved));
	}

	@Secured("ROLE_USER")
	@PutMapping("/{id}")
	public ResponseEntity<QuotationResponse> update(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id,
			@Valid @RequestBody QuotationSaveRequest request) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		Quotation updated = quotationService.update(companyId, id, request);
		try { quotationPdfService.generateAndStore(companyId, updated.getId()); }
		catch (Exception e) { log.warn("PDF generation failed for quotation {}", updated.getId(), e); }
		return ResponseEntity.ok(QuotationMapper.toResponse(updated));
	}

	@Secured("ROLE_USER")
	@PutMapping("/{id}/status")
	public ResponseEntity<QuotationResponse> transitionStatus(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id,
			@Valid @RequestBody QuotationStatusRequest request) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		Quotation updated = quotationService.transitionStatus(companyId, id, request.getStatus(), principal.getUsername());
		try { quotationPdfService.generateAndStore(companyId, updated.getId()); }
		catch (Exception e) { log.warn("PDF generation failed for quotation {} after status change", updated.getId(), e); }
		return ResponseEntity.ok(QuotationMapper.toResponse(updated));
	}

	@Secured("ROLE_USER")
	@PostMapping("/{id}/convert")
	public ResponseEntity<InvoiceResponse> convertToInvoice(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		Invoice invoice = quotationService.convertToInvoice(companyId, id, principal.getUsername());
		try { invoicePdfService.generateAndStore(companyId, invoice.getId()); }
		catch (Exception e) { log.warn("PDF generation failed for converted invoice {}", invoice.getId(), e); }
		return ResponseEntity.ok(InvoiceMapper.toResponse(invoice));
	}

	@Secured("ROLE_USER")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		quotationService.delete(companyId, id);
		return ResponseEntity.noContent().build();
	}

}
