package com.avantdream.paytrack.payment.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.avantdream.paytrack.payment.dto.PaymentRequest;
import com.avantdream.paytrack.payment.entity.Payment;

public interface PaymentService {

    Payment recordPayment(Long companyId, Long invoiceId, PaymentRequest request, String createdBy);

    void voidPayment(Long companyId, Long invoiceId, Long paymentId, String voidedBy);

    void deletePayment(Long companyId, Long invoiceId, Long paymentId);

    List<Payment> findByInvoice(Long companyId, Long invoiceId);

    Page<Payment> findAll(Long companyId, Pageable pageable);
}
