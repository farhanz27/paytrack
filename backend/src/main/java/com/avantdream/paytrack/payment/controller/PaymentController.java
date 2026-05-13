package com.avantdream.paytrack.payment.controller;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avantdream.paytrack.payment.dto.PaymentRequest;
import com.avantdream.paytrack.payment.dto.PaymentResponse;
import com.avantdream.paytrack.payment.entity.Payment;
import com.avantdream.paytrack.payment.mapper.PaymentMapper;
import com.avantdream.paytrack.payment.service.PaymentService;
import com.avantdream.paytrack.pdf.service.InvoicePdfService;
import com.avantdream.paytrack.shared.dto.PageResponse;
import com.avantdream.paytrack.shared.workspace.WorkspaceAccess;

@RestController
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private static final String COMPANY_HEADER = "X-Company-Id";

    private final PaymentService paymentService;
    private final InvoicePdfService invoicePdfService;
    private final WorkspaceAccess workspaceAccess;

    public PaymentController(PaymentService paymentService, InvoicePdfService invoicePdfService,
            WorkspaceAccess workspaceAccess) {
        this.paymentService = paymentService;
        this.invoicePdfService = invoicePdfService;
        this.workspaceAccess = workspaceAccess;
    }

    @Secured("ROLE_USER")
    @GetMapping("/api/payments")
    public ResponseEntity<PageResponse<PaymentResponse>> listAll(
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        long companyId = workspaceAccess.resolve(principal, companyHeader);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<PaymentResponse> result = PageResponse.from(
                paymentService.findAll(companyId, pageable).map(PaymentMapper::toResponse));
        return ResponseEntity.ok(result);
    }

    @Secured("ROLE_USER")
    @PostMapping("/api/invoices/{invoiceId}/payments")
    public ResponseEntity<PaymentResponse> recordPayment(
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
            @PathVariable Long invoiceId,
            @Valid @RequestBody PaymentRequest request) {

        long companyId = workspaceAccess.resolve(principal, companyHeader);
        Payment payment = paymentService.recordPayment(companyId, invoiceId, request, principal.getUsername());
        try { invoicePdfService.generateAndStore(companyId, invoiceId); }
        catch (Exception e) { log.warn("PDF generation failed for invoice {} after payment", invoiceId, e); }
        return ResponseEntity
                .created(URI.create("/api/invoices/" + invoiceId + "/payments/" + payment.getId()))
                .body(PaymentMapper.toResponse(payment));
    }

    @Secured("ROLE_USER")
    @PostMapping("/api/invoices/{invoiceId}/payments/{paymentId}/void")
    public ResponseEntity<Void> voidPayment(
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
            @PathVariable Long invoiceId,
            @PathVariable Long paymentId) {

        long companyId = workspaceAccess.resolve(principal, companyHeader);
        paymentService.voidPayment(companyId, invoiceId, paymentId, principal.getUsername());
        try { invoicePdfService.generateAndStore(companyId, invoiceId); }
        catch (Exception e) { log.warn("PDF generation failed for invoice {} after void payment", invoiceId, e); }
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/api/invoices/{invoiceId}/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
            @PathVariable Long invoiceId,
            @PathVariable Long paymentId) {

        long companyId = workspaceAccess.resolve(principal, companyHeader);
        paymentService.deletePayment(companyId, invoiceId, paymentId);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_USER")
    @GetMapping("/api/invoices/{invoiceId}/payments")
    public ResponseEntity<List<PaymentResponse>> listByInvoice(
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
            @PathVariable Long invoiceId) {

        long companyId = workspaceAccess.resolve(principal, companyHeader);
        List<Payment> payments = paymentService.findByInvoice(companyId, invoiceId);
        return ResponseEntity.ok(payments.stream().map(PaymentMapper::toResponse).collect(Collectors.toList()));
    }
}
