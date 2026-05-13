package com.avantdream.paytrack.pdf.service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.avantdream.paytrack.pdf.dto.InvoicePdfDTO;
import com.avantdream.paytrack.pdf.dto.PdfDocumentData;
import com.avantdream.paytrack.pdf.dto.QuotationPdfDTO;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Media;
import com.microsoft.playwright.options.WaitUntilState;

@Service
public class PdfGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(PdfGeneratorService.class);

    private final TemplateEngine templateEngine;
    private final Browser browser;
    // API Gateway returns text/plain even for JSON bodies — extend the converter to accept it
    private static final RestClient restClient = RestClient.builder()
            .messageConverters(converters -> {
                MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
                converter.setSupportedMediaTypes(List.of(
                        MediaType.APPLICATION_JSON,
                        MediaType.TEXT_PLAIN,
                        new MediaType("application", "*+json")
                ));
                converters.add(0, converter);
            })
            .build();

    @Value("${pdf.lambda.url:}")
    private String lambdaUrl;

    @Value("${pdf.lambda.secret:}")
    private String lambdaSecret;

    public PdfGeneratorService(TemplateEngine templateEngine, Browser browser) {
        this.templateEngine = templateEngine;
        this.browser = browser;
    }

    public byte[] renderInvoice(InvoicePdfDTO dto) {
        PdfDocumentData data = new PdfDocumentData(
            dto.getCompanyName(),
            lines(dto.getCompanyAddress()),
            dto.getCompanyEmail(),
            dto.getCompanyPhone(),
            dto.getCustomerName(),
            dto.getCustomerCompany(),
            dto.getCustomerEmail(),
            lines(dto.getBillingAddress()),
            "INVOICE",
            dto.getInvoiceNumber(),
            dto.getStatus(),
            dto.getIssueDate(),
            "Due Date",
            dto.getDueDate(),
            dto.getCurrency(),
            dto.getItems(),
            dto.getSubtotal(),
            dto.getDiscount(),
            dto.getTax(),
            dto.getGrandTotal(),
            dto.getPaidAmount(),
            dto.getRemainingAmount(),
            dto.getNotes()
        );
        return render(data);
    }

    public byte[] renderQuotation(QuotationPdfDTO dto) {
        PdfDocumentData data = new PdfDocumentData(
            dto.getCompanyName(),
            lines(dto.getCompanyAddress()),
            dto.getCompanyEmail(),
            dto.getCompanyPhone(),
            dto.getCustomerName(),
            dto.getCustomerCompany(),
            dto.getCustomerEmail(),
            lines(dto.getBillingAddress()),
            "QUOTATION",
            dto.getQuotationNumber(),
            dto.getStatus(),
            dto.getIssueDate(),
            "Valid Until",
            dto.getValidUntil(),
            dto.getCurrency(),
            dto.getItems(),
            dto.getSubtotal(),
            dto.getDiscount(),
            dto.getTax(),
            dto.getGrandTotal(),
            null,
            null,
            dto.getNotes()
        );
        return render(data);
    }

    private byte[] render(PdfDocumentData data) {
        Context ctx = new Context();
        ctx.setVariable("doc", data);
        String html = templateEngine.process("pdf/document", ctx);

        if (lambdaUrl != null && !lambdaUrl.isBlank()) {
            try {
                return renderViaLambda(html);
            } catch (Exception e) {
                log.warn("Lambda PDF render failed, falling back to local Playwright: {}", e.getMessage());
            }
        }

        return renderLocal(html);
    }

    private byte[] renderViaLambda(String html) {
        record LambdaRequest(String html) {}
        record LambdaResponse(String url) {}

        LambdaResponse response = restClient.post()
                .uri(lambdaUrl)
                .header("X-Pdf-Secret", lambdaSecret)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LambdaRequest(html))
                .retrieve()
                .body(LambdaResponse.class);

        if (response == null || response.url() == null) {
            throw new RuntimeException("Lambda returned null URL");
        }

        byte[] bytes = restClient.get()
                .uri(URI.create(response.url()))
                .retrieve()
                .body(byte[].class);

        if (bytes == null || bytes.length == 0) {
            throw new RuntimeException("Downloaded empty PDF from Lambda presigned URL");
        }
        return bytes;
    }

    private byte[] renderLocal(String html) {
        try (BrowserContext browserCtx = browser.newContext();
             Page page = browserCtx.newPage()) {
            page.setContent(html, new Page.SetContentOptions().setWaitUntil(WaitUntilState.LOAD));
            page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));
            return page.pdf(new Page.PdfOptions()
                .setFormat("A4")
                .setPrintBackground(true)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private List<String> lines(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split("\n"))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .toList();
    }

    @SuppressWarnings("unused")
    private boolean isPositive(BigDecimal val) {
        return val != null && val.compareTo(BigDecimal.ZERO) > 0;
    }
}
