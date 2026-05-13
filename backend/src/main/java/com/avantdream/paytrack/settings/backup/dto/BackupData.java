package com.avantdream.paytrack.settings.backup.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class BackupData {

    public String version = "1.0";
    public Date exportedAt;
    public Long companyId;
    public String companyName;
    public List<CustomerRecord> customers;
    public List<CatalogRecord> catalog;
    public List<QuotationRecord> quotations;
    public List<InvoiceRecord> invoices;
    public List<PaymentRecord> payments;

    public static class CustomerRecord {
        public Long id;
        public String name, email, phone, companyName, registrationNumber;
        public String billingAddressLine1, billingAddressLine2, billingPostcode;
        public String billingCity, billingState, billingCountry;
        public String notes, status;
        public Date archivedAt;
    }

    public static class CatalogRecord {
        public Long id;
        public String name, description;
        public BigDecimal price;
        public Date archivedAt;
    }

    public static class QuotationRecord {
        public Long id;
        public Long customerId;
        public String quotationNumber, status, currency;
        public Date issueDate, validUntil;
        public String customerName, customerEmail, billingAddress;
        public BigDecimal subtotal, discount, tax, grandTotal;
        public List<LineItem> items;
    }

    public static class InvoiceRecord {
        public Long id;
        public Long customerId, sourceQuotationId;
        public String invoiceNumber, status, currency, notes;
        public Date issueDate, dueDate, issuedAt, paidAt;
        public String customerName, customerEmail, billingAddress;
        public BigDecimal subtotal, discount, tax, grandTotal, paidAmount, remainingAmount;
        public List<LineItem> items;
    }

    public static class LineItem {
        public String description;
        public BigDecimal unitPrice;
        public Integer quantity;
        public BigDecimal subtotal;
    }

    public static class PaymentRecord {
        public Long invoiceId;
        public BigDecimal amount;
        public Date paymentDate;
        public String method, reference, notes, receiptUrl;
        public boolean voided;
        public Date voidedAt;
        public String voidedBy;
    }
}
