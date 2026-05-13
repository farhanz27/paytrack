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
import com.avantdream.paytrack.pdf.dto.PdfItemDTO;
import com.avantdream.paytrack.pdf.dto.QuotationPdfDTO;
import com.avantdream.paytrack.quotation.entity.Quotation;
import com.avantdream.paytrack.quotation.repository.QuotationRepository;
import com.avantdream.paytrack.quotation.service.QuotationService;

@Service
public class QuotationPdfService {

    private static final Logger log = LoggerFactory.getLogger(QuotationPdfService.class);
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd MMM yyyy");

    private final QuotationService quotationService;
    private final QuotationRepository quotationRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final PdfStorageService pdfStorageService;

    public QuotationPdfService(QuotationService quotationService, QuotationRepository quotationRepository,
            PdfGeneratorService pdfGeneratorService, PdfStorageService pdfStorageService) {
        this.quotationService = quotationService;
        this.quotationRepository = quotationRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.pdfStorageService = pdfStorageService;
    }

    @Transactional(readOnly = true)
    public byte[] generate(Long companyId, Long quotationId) {
        Quotation quotation = quotationService.findByIdWithDetails(companyId, quotationId);
        QuotationPdfDTO dto = toDto(quotation);
        return pdfGeneratorService.renderQuotation(dto);
    }

    public void generateAndStore(long companyId, long quotationId) {
        Quotation quotation = quotationService.findByIdWithDetails(companyId, quotationId);
        generateAndStoreInternal(quotation);
    }

    public String getOrGeneratePresignedUrl(long companyId, long quotationId, String disposition) {
        Quotation quotation = quotationService.findByIdWithDetails(companyId, quotationId);
        String key = quotation.getPdfS3Key();
        if (key == null) {
            key = generateAndStoreInternal(quotation);
        }
        String filename = "quotation-" + quotation.getQuotationNumber() + ".pdf";
        String contentDisposition = "attachment".equalsIgnoreCase(disposition)
                ? "attachment; filename=\"" + filename + "\""
                : "inline; filename=\"" + filename + "\"";
        return pdfStorageService.presignUrl(key, contentDisposition);
    }

    private String generateAndStoreInternal(Quotation quotation) {
        byte[] pdf = pdfGeneratorService.renderQuotation(toDto(quotation));
        String key = "quotations/" + quotation.getCompany().getId() + "/" + quotation.getId() + ".pdf";
        pdfStorageService.store(key, pdf);
        quotationRepository.updatePdfS3Key(quotation.getId(), key);
        log.debug("PDF stored for quotation {}: {}", quotation.getId(), key);
        return key;
    }

    private QuotationPdfDTO toDto(Quotation quotation) {
        QuotationPdfDTO dto = new QuotationPdfDTO();

        Company company = quotation.getCompany();
        dto.setCompanyName(company.getName());
        dto.setCompanyAddress(formatCompanyAddress(company));
        dto.setCompanyEmail(company.getEmail());
        dto.setCompanyPhone(company.getPhone());

        dto.setCustomerName(quotation.getCustomerName());
        dto.setCustomerCompany(quotation.getCustomerCompany());
        dto.setCustomerEmail(quotation.getCustomerEmail());
        dto.setBillingAddress(quotation.getBillingAddress());

        dto.setQuotationNumber(quotation.getQuotationNumber());
        dto.setStatus(quotation.getStatus() != null ? quotation.getStatus().name() : null);
        dto.setIssueDate(formatDate(quotation.getIssueDate()));
        dto.setValidUntil(formatDate(quotation.getValidUntil()));
        dto.setCurrency("MYR");

        List<PdfItemDTO> items = quotation.getItems().stream()
                .map(i -> new PdfItemDTO(i.getName(), i.getDescription(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal()))
                .collect(Collectors.toList());
        dto.setItems(items);

        dto.setSubtotal(quotation.getSubtotal());
        dto.setDiscount(quotation.getDiscount());
        dto.setTax(quotation.getTax());
        dto.setGrandTotal(quotation.getGrandTotal());

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
