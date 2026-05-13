package com.avantdream.paytrack.payment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avantdream.paytrack.company.repository.CompanyRepository;
import com.avantdream.paytrack.invoice.entity.Invoice;
import com.avantdream.paytrack.invoice.entity.InvoiceStatus;
import com.avantdream.paytrack.invoice.entity.InvoiceStatusLog;
import com.avantdream.paytrack.invoice.repository.InvoiceRepository;
import com.avantdream.paytrack.invoice.repository.InvoiceStatusLogRepository;
import com.avantdream.paytrack.payment.dto.PaymentRequest;
import com.avantdream.paytrack.payment.entity.Payment;
import com.avantdream.paytrack.payment.repository.PaymentRepository;
import com.avantdream.paytrack.shared.exception.BadRequestException;
import com.avantdream.paytrack.shared.exception.ResourceNotFoundException;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusLogRepository statusLogRepository;
    private final CompanyRepository companyRepository;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository,
            InvoiceStatusLogRepository statusLogRepository,
            CompanyRepository companyRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.statusLogRepository = statusLogRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public Payment recordPayment(Long companyId, Long invoiceId, PaymentRequest request, String createdBy) {
        Invoice invoice = invoiceRepository.findByIdAndCompany_Id(invoiceId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        InvoiceStatus currentStatus = invoice.getStatus();
        if (currentStatus == InvoiceStatus.DRAFT || currentStatus == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Cannot record payment for a " + currentStatus + " invoice");
        }

        BigDecimal amount = request.getAmount();
        BigDecimal remaining = invoice.getRemainingAmount() != null
                ? invoice.getRemainingAmount()
                : invoice.getGrandTotal();

        if (amount.compareTo(remaining) > 0) {
            throw new BadRequestException(
                    "Payment amount " + amount + " exceeds remaining balance of " + remaining);
        }

        companyRepository.lockById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        int year = LocalDate.now().getYear();
        long receiptCount = paymentRepository.countByCompany_IdAndYear(companyId, year) + 1;

        Payment payment = new Payment();
        payment.setCompany(invoice.getCompany());
        payment.setInvoice(invoice);
        payment.setReceiptNumber(String.format("REC-%d-%05d", year, receiptCount));
        payment.setAmount(amount);
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : new Date());
        payment.setMethod(request.getMethod());
        payment.setReference(request.getReference());
        payment.setNotes(request.getNotes());
        payment.setReceiptUrl(request.getReceiptUrl());
        paymentRepository.save(payment);

        BigDecimal newPaidAmount = invoice.getPaidAmount().add(amount);
        BigDecimal newRemaining = invoice.getGrandTotal().subtract(newPaidAmount).max(BigDecimal.ZERO);
        invoice.setPaidAmount(newPaidAmount);
        invoice.setRemainingAmount(newRemaining);

        InvoiceStatus newStatus = newRemaining.compareTo(BigDecimal.ZERO) == 0
                ? InvoiceStatus.PAID
                : InvoiceStatus.PARTIALLY_PAID;

        if (newStatus != currentStatus) {
            invoice.setStatus(newStatus);
            if (newStatus == InvoiceStatus.PAID) {
                invoice.setPaidAt(new Date());
            }
            InvoiceStatusLog log = new InvoiceStatusLog();
            log.setInvoiceId(invoice.getId());
            log.setFromStatus(currentStatus);
            log.setToStatus(newStatus);
            log.setChangedBy(createdBy);
            statusLogRepository.save(log);
        }

        invoiceRepository.save(invoice);
        return payment;
    }

    @Override
    @Transactional
    public void voidPayment(Long companyId, Long invoiceId, Long paymentId, String voidedBy) {
        Payment payment = paymentRepository.findByIdAndInvoice_IdAndCompany_Id(paymentId, invoiceId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if (payment.isVoided()) {
            throw new BadRequestException("Payment is already voided");
        }

        Invoice invoice = invoiceRepository.findByIdAndCompany_Id(invoiceId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        BigDecimal newPaidAmount = invoice.getPaidAmount().subtract(payment.getAmount()).max(BigDecimal.ZERO);
        BigDecimal newRemaining = invoice.getGrandTotal().subtract(newPaidAmount).max(BigDecimal.ZERO);
        invoice.setPaidAmount(newPaidAmount);
        invoice.setRemainingAmount(newRemaining);

        InvoiceStatus currentStatus = invoice.getStatus();
        InvoiceStatus newStatus;
        if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
            newStatus = InvoiceStatus.PAID;
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) == 0) {
            newStatus = InvoiceStatus.ISSUED;
        } else {
            newStatus = InvoiceStatus.PARTIALLY_PAID;
        }

        if (newStatus != currentStatus) {
            invoice.setStatus(newStatus);
            if (currentStatus == InvoiceStatus.PAID) {
                invoice.setPaidAt(null);
            }
            InvoiceStatusLog log = new InvoiceStatusLog();
            log.setInvoiceId(invoice.getId());
            log.setFromStatus(currentStatus);
            log.setToStatus(newStatus);
            log.setChangedBy(voidedBy);
            statusLogRepository.save(log);
        }

        invoiceRepository.save(invoice);

        payment.setVoided(true);
        payment.setVoidedAt(new Date());
        payment.setVoidedBy(voidedBy);
        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void deletePayment(Long companyId, Long invoiceId, Long paymentId) {
        Payment payment = paymentRepository.findByIdAndInvoice_IdAndCompany_Id(paymentId, invoiceId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if (!payment.isVoided()) {
            throw new BadRequestException("Payment must be voided before it can be deleted");
        }

        paymentRepository.delete(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByInvoice(Long companyId, Long invoiceId) {
        invoiceRepository.findByIdAndCompany_Id(invoiceId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));
        return paymentRepository.findByInvoice_IdAndCompany_IdOrderByCreatedAtDesc(invoiceId, companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> findAll(Long companyId, Pageable pageable) {
        return paymentRepository.findByCompany_IdOrderByCreatedAtDesc(companyId, pageable);
    }
}
