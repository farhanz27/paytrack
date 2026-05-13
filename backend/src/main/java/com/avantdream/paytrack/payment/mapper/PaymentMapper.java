package com.avantdream.paytrack.payment.mapper;

import com.avantdream.paytrack.payment.dto.PaymentResponse;
import com.avantdream.paytrack.payment.entity.Payment;

public class PaymentMapper {

    private PaymentMapper() {}

    public static PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setReceiptNumber(payment.getReceiptNumber());
        if (payment.getInvoice() != null) {
            response.setInvoiceId(payment.getInvoice().getId());
            response.setInvoiceNumber(payment.getInvoice().getInvoiceNumber());
            response.setCustomerName(payment.getInvoice().getCustomerName());
        }
        response.setAmount(payment.getAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setMethod(payment.getMethod());
        response.setReference(payment.getReference());
        response.setNotes(payment.getNotes());
        response.setReceiptUrl(payment.getReceiptUrl());
        response.setCreatedAt(payment.getCreatedAt());
        response.setVoided(payment.isVoided());
        response.setVoidedAt(payment.getVoidedAt());
        response.setVoidedBy(payment.getVoidedBy());
        return response;
    }
}
