package com.avantdream.paytrack.pdf.dto;

import java.math.BigDecimal;
import java.util.List;

public record PdfDocumentData(
    String companyName,
    List<String> companyAddressLines,
    String companyEmail,
    String companyPhone,

    String customerName,
    String customerCompany,
    String customerEmail,
    List<String> billingAddressLines,

    String docType,
    String docNumber,
    String status,
    String issueDate,
    String secondDateLabel,
    String secondDate,
    String currency,

    List<PdfItemDTO> items,
    BigDecimal subtotal,
    BigDecimal discount,
    BigDecimal tax,
    BigDecimal grandTotal,
    BigDecimal paidAmount,
    BigDecimal remainingAmount,

    String notes
) {}
