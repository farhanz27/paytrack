package com.avantdream.paytrack.pdf.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avantdream.paytrack.company.entity.Company;
import com.avantdream.paytrack.invoice.entity.Invoice;
import com.avantdream.paytrack.invoice.repository.InvoiceRepository;
import com.avantdream.paytrack.invoice.service.InvoiceService;
import com.avantdream.paytrack.pdf.dto.InvoicePdfDTO;
import com.avantdream.paytrack.pdf.dto.PdfItemDTO;

@Service
public class InvoicePdfService {

    private static final Logger log = LoggerFactory.getLogger(InvoicePdfService.class);
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd MMM yyyy");

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final PdfStorageService pdfStorageService;

    public InvoicePdfService(InvoiceService invoiceService, InvoiceRepository invoiceRepository,
            PdfGeneratorService pdfGeneratorService, PdfStorageService pdfStorageService) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.pdfStorageService = pdfStorageService;
    }

    @Transactional(readOnly = true)
    public byte[] generate(Long companyId, Long invoiceId) {
        Invoice invoice = invoiceService.findByIdWithDetails(companyId, invoiceId);
        InvoicePdfDTO dto = toDto(invoice);
        return pdfGeneratorService.renderInvoice(dto);
    }

    public void generateAndStore(long companyId, long invoiceId) {
        Invoice invoice = invoiceService.findByIdWithDetails(companyId, invoiceId);
        generateAndStoreInternal(invoice);
    }

    public String getOrGeneratePresignedUrl(long companyId, long invoiceId, String disposition) {
        Invoice invoice = invoiceService.findByIdWithDetails(companyId, invoiceId);
        String key = invoice.getPdfS3Key();
        if (key == null) {
            key = generateAndStoreInternal(invoice);
        }
        String filename = "invoice-" + invoice.getInvoiceNumber() + ".pdf";
        String contentDisposition = "attachment".equalsIgnoreCase(disposition)
                ? "attachment; filename=\"" + filename + "\""
                : "inline; filename=\"" + filename + "\"";
        return pdfStorageService.presignUrl(key, contentDisposition);
    }

    private String generateAndStoreInternal(Invoice invoice) {
        byte[] pdf = pdfGeneratorService.renderInvoice(toDto(invoice));
        String key = "invoices/" + invoice.getCompany().getId() + "/" + invoice.getId() + ".pdf";
        pdfStorageService.store(key, pdf);
        invoiceRepository.updatePdfS3Key(invoice.getId(), key);
        log.debug("PDF stored for invoice {}: {}", invoice.getId(), key);
        return key;
    }

    private InvoicePdfDTO toDto(Invoice invoice) {
        InvoicePdfDTO dto = new InvoicePdfDTO();

        Company company = invoice.getCompany();
        dto.setCompanyName(company.getName());
        dto.setCompanyAddress(formatCompanyAddress(company));
        dto.setCompanyEmail(company.getEmail());
        dto.setCompanyPhone(company.getPhone());

        dto.setCustomerName(invoice.getCustomerName());
        dto.setCustomerCompany(invoice.getCustomerCompany());
        dto.setCustomerEmail(invoice.getCustomerEmail());
        dto.setBillingAddress(invoice.getBillingAddress());

        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setStatus(invoice.getStatus() != null ? invoice.getStatus().name() : null);
        dto.setIssueDate(formatDate(invoice.getIssueDate()));
        dto.setDueDate(formatDate(invoice.getDueDate()));
        dto.setCurrency(invoice.getCurrency());

        List<PdfItemDTO> items = invoice.getItems().stream()
                .map(i -> new PdfItemDTO(i.getName(), i.getDescription(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal()))
                .collect(Collectors.toList());
        dto.setItems(items);

        dto.setSubtotal(invoice.getSubtotal());
        dto.setDiscount(invoice.getDiscount());
        dto.setTax(invoice.getTax());
        dto.setGrandTotal(invoice.getGrandTotal());
        dto.setPaidAmount(invoice.getPaidAmount());
        dto.setRemainingAmount(invoice.getRemainingAmount());
        dto.setNotes(invoice.getNotes());

        return dto;
    }

    private String formatCompanyAddress(Company c) {
        StringBuilder sb = new StringBuilder();
        appendLine(sb, c.getBillingAddressLine1());
        appendLine(sb, c.getBillingAddressLine2());
        String cityLine = joinNonBlank(", ", c.getBillingCity(), c.getBillingState(), c.getBillingPostcode());
        appendLine(sb, cityLine);
        appendLine(sb, c.getBillingCountry());
        return sb.toString().trim();
    }

    private void appendLine(StringBuilder sb, String val) {
        if (val != null && !val.isBlank()) {
            sb.append(val.trim()).append("\n");
        }
    }

    private String joinNonBlank(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                if (!sb.isEmpty()) sb.append(sep);
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }

    private String formatDate(Date date) {
        return date != null ? DATE_FMT.format(date) : "-";
    }
}
