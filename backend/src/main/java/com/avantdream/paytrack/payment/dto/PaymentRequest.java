package com.avantdream.paytrack.payment.dto;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.avantdream.paytrack.payment.entity.PaymentMethod;

public class PaymentRequest {

    @NotNull
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date paymentDate;

    @NotNull
    private PaymentMethod method;

    private String reference;
    private String notes;
    private String receiptUrl;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
}
