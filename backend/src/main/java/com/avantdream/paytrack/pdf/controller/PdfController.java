package com.avantdream.paytrack.pdf.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avantdream.paytrack.pdf.service.InvoicePdfService;
import com.avantdream.paytrack.pdf.service.PdfStorageService;
import com.avantdream.paytrack.pdf.service.QuotationPdfService;
import com.avantdream.paytrack.shared.workspace.WorkspaceAccess;

@RestController
public class PdfController {

    static final String COMPANY_HEADER = "X-Company-Id";

    private final InvoicePdfService invoicePdfService;
    private final QuotationPdfService quotationPdfService;
    private final PdfStorageService pdfStorageService;
    private final WorkspaceAccess workspaceAccess;

    public PdfController(InvoicePdfService invoicePdfService, QuotationPdfService quotationPdfService,
            PdfStorageService pdfStorageService, WorkspaceAccess workspaceAccess) {
        this.invoicePdfService = invoicePdfService;
        this.quotationPdfService = quotationPdfService;
        this.pdfStorageService = pdfStorageService;
        this.workspaceAccess = workspaceAccess;
    }

    @Secured("ROLE_USER")
    @GetMapping("/api/invoices/{id}/pdf")
    public ResponseEntity<Map<String, String>> invoicePdf(
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
            @PathVariable Long id,
            @RequestParam(defaultValue = "inline") String disposition) {

        long companyId = workspaceAccess.resolve(principal, companyHeader);

        if (!pdfStorageService.isConfigured()) {
            String localUrl = "/api/invoices/" + id + "/pdf/data?disposition=" + disposition
                    + "&companyId=" + companyId;
            return ResponseEntity.ok(Map.of("url", localUrl));
        }

        String url = invoicePdfService.getOrGeneratePresignedUrl(companyId, id, disposition);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @Secured("ROLE_USER")
    @GetMapping("/api/quotations/{id}/pdf")
    public ResponseEntity<Map<String, String>> quotationPdf(
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
            @PathVariable Long id,
            @RequestParam(defaultValue = "inline") String disposition) {

        long companyId = workspaceAccess.resolve(principal, companyHeader);

        if (!pdfStorageService.isConfigured()) {
            String localUrl = "/api/quotations/" + id + "/pdf/data?disposition=" + disposition
                    + "&companyId=" + companyId;
            return ResponseEntity.ok(Map.of("url", localUrl));
        }

        String url = quotationPdfService.getOrGeneratePresignedUrl(companyId, id, disposition);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @Secured("ROLE_USER")
    @GetMapping("/api/invoices/{id}/pdf/data")
    public ResponseEntity<byte[]> invoicePdfData(
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
            @PathVariable Long id,
            @RequestParam(defaultValue = "inline") String disposition,
            @RequestParam(required = false) Long companyId) {

        long resolvedCompanyId = (companyId != null)
                ? workspaceAccess.resolve(principal, companyId)
                : workspaceAccess.resolve(principal, companyHeader);

        byte[] bytes = invoicePdfService.generate(resolvedCompanyId, id);
        String cd = "attachment".equalsIgnoreCase(disposition)
                ? "attachment; filename=\"invoice-" + id + ".pdf\""
                : "inline; filename=\"invoice-" + id + ".pdf\"";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd)
                .body(bytes);
    }

    @Secured("ROLE_USER")
    @GetMapping("/api/quotations/{id}/pdf/data")
    public ResponseEntity<byte[]> quotationPdfData(
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
            @PathVariable Long id,
            @RequestParam(defaultValue = "inline") String disposition,
            @RequestParam(required = false) Long companyId) {

        long resolvedCompanyId = (companyId != null)
                ? workspaceAccess.resolve(principal, companyId)
                : workspaceAccess.resolve(principal, companyHeader);

        byte[] bytes = quotationPdfService.generate(resolvedCompanyId, id);
        String cd = "attachment".equalsIgnoreCase(disposition)
                ? "attachment; filename=\"quotation-" + id + ".pdf\""
                : "inline; filename=\"quotation-" + id + ".pdf\"";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd)
                .body(bytes);
    }
}
