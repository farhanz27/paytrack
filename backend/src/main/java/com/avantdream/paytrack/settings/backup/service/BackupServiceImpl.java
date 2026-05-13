package com.avantdream.paytrack.settings.backup.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.avantdream.paytrack.catalog.entity.CatalogItem;
import com.avantdream.paytrack.catalog.repository.CatalogItemRepository;
import com.avantdream.paytrack.company.entity.Company;
import com.avantdream.paytrack.company.repository.CompanyRepository;
import com.avantdream.paytrack.customer.entity.Customer;
import com.avantdream.paytrack.customer.entity.CustomerStatus;
import com.avantdream.paytrack.customer.repository.CustomerRepository;
import com.avantdream.paytrack.invoice.entity.Invoice;
import com.avantdream.paytrack.invoice.entity.InvoiceItem;
import com.avantdream.paytrack.invoice.entity.InvoiceStatus;
import com.avantdream.paytrack.invoice.repository.InvoiceRepository;
import com.avantdream.paytrack.invoice.repository.InvoiceStatusLogRepository;
import com.avantdream.paytrack.payment.entity.Payment;
import com.avantdream.paytrack.payment.entity.PaymentMethod;
import com.avantdream.paytrack.payment.repository.PaymentRepository;
import com.avantdream.paytrack.quotation.entity.Quotation;
import com.avantdream.paytrack.quotation.entity.QuotationItem;
import com.avantdream.paytrack.quotation.entity.QuotationStatus;
import com.avantdream.paytrack.quotation.repository.QuotationRepository;
import com.avantdream.paytrack.settings.backup.dto.BackupData;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BackupServiceImpl implements BackupService {

    private final CompanyRepository companyRepo;
    private final CustomerRepository customerRepo;
    private final CatalogItemRepository catalogRepo;
    private final QuotationRepository quotationRepo;
    private final InvoiceRepository invoiceRepo;
    private final InvoiceStatusLogRepository logRepo;
    private final PaymentRepository paymentRepo;
    private final ObjectMapper objectMapper;

    public BackupServiceImpl(CompanyRepository companyRepo, CustomerRepository customerRepo,
            CatalogItemRepository catalogRepo, QuotationRepository quotationRepo,
            InvoiceRepository invoiceRepo, InvoiceStatusLogRepository logRepo,
            PaymentRepository paymentRepo, ObjectMapper objectMapper) {
        this.companyRepo = companyRepo;
        this.customerRepo = customerRepo;
        this.catalogRepo = catalogRepo;
        this.quotationRepo = quotationRepo;
        this.invoiceRepo = invoiceRepo;
        this.logRepo = logRepo;
        this.paymentRepo = paymentRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public BackupData export(Long companyId) {
        Company company = companyRepo.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        BackupData data = new BackupData();
        data.exportedAt = new java.util.Date();
        data.companyId = companyId;
        data.companyName = company.getName();

        data.customers = customerRepo.findAllByCompany_Id(companyId).stream().map(c -> {
            BackupData.CustomerRecord r = new BackupData.CustomerRecord();
            r.id = c.getId();
            r.name = c.getName();
            r.email = c.getEmail();
            r.phone = c.getPhone();
            r.companyName = c.getCompanyName();
            r.registrationNumber = c.getRegistrationNumber();
            r.billingAddressLine1 = c.getBillingAddressLine1();
            r.billingAddressLine2 = c.getBillingAddressLine2();
            r.billingPostcode = c.getBillingPostcode();
            r.billingCity = c.getBillingCity();
            r.billingState = c.getBillingState();
            r.billingCountry = c.getBillingCountry();
            r.notes = c.getNotes();
            r.status = c.getStatus() != null ? c.getStatus().name() : null;
            r.archivedAt = c.getArchivedAt();
            return r;
        }).collect(Collectors.toList());

        data.catalog = catalogRepo.findAllByCompany_Id(companyId).stream().map(c -> {
            BackupData.CatalogRecord r = new BackupData.CatalogRecord();
            r.id = c.getId();
            r.name = c.getName();
            r.description = c.getDescription();
            r.price = c.getPrice();
            r.archivedAt = c.getArchivedAt();
            return r;
        }).collect(Collectors.toList());

        data.quotations = quotationRepo.findAllByCompany_Id(companyId).stream().map(q -> {
            BackupData.QuotationRecord r = new BackupData.QuotationRecord();
            r.id = q.getId();
            r.customerId = q.getCustomer() != null ? q.getCustomer().getId() : null;
            r.quotationNumber = q.getQuotationNumber();
            r.status = q.getStatus() != null ? q.getStatus().name() : null;
            r.issueDate = q.getIssueDate();
            r.validUntil = q.getValidUntil();
            r.currency = q.getCurrency();
            r.customerName = q.getCustomerName();
            r.customerEmail = q.getCustomerEmail();
            r.billingAddress = q.getBillingAddress();
            r.subtotal = q.getSubtotal();
            r.discount = q.getDiscount();
            r.tax = q.getTax();
            r.grandTotal = q.getGrandTotal();
            r.items = q.getItems().stream().map(i -> toLineItem(i.getDescription(), i.getUnitPrice(), i.getQuantity(), i.getSubtotal())).collect(Collectors.toList());
            return r;
        }).collect(Collectors.toList());

        data.invoices = invoiceRepo.findAllByCompany_Id(companyId).stream().map(inv -> {
            BackupData.InvoiceRecord r = new BackupData.InvoiceRecord();
            r.id = inv.getId();
            r.customerId = inv.getCustomer() != null ? inv.getCustomer().getId() : null;
            r.sourceQuotationId = inv.getSourceQuotationId();
            r.invoiceNumber = inv.getInvoiceNumber();
            r.status = inv.getStatus() != null ? inv.getStatus().name() : null;
            r.issueDate = inv.getIssueDate();
            r.dueDate = inv.getDueDate();
            r.currency = inv.getCurrency();
            r.notes = inv.getNotes();
            r.customerName = inv.getCustomerName();
            r.customerEmail = inv.getCustomerEmail();
            r.billingAddress = inv.getBillingAddress();
            r.subtotal = inv.getSubtotal();
            r.discount = inv.getDiscount();
            r.tax = inv.getTax();
            r.grandTotal = inv.getGrandTotal();
            r.paidAmount = inv.getPaidAmount();
            r.remainingAmount = inv.getRemainingAmount();
            r.issuedAt = inv.getIssuedAt();
            r.paidAt = inv.getPaidAt();
            r.items = inv.getItems().stream().map(i -> toLineItem(i.getDescription(), i.getUnitPrice(), i.getQuantity(), i.getSubtotal())).collect(Collectors.toList());
            return r;
        }).collect(Collectors.toList());

        data.payments = paymentRepo.findAllByCompany_Id(companyId).stream().map(p -> {
            BackupData.PaymentRecord r = new BackupData.PaymentRecord();
            r.invoiceId = p.getInvoice().getId();
            r.amount = p.getAmount();
            r.paymentDate = p.getPaymentDate();
            r.method = p.getMethod() != null ? p.getMethod().name() : null;
            r.reference = p.getReference();
            r.notes = p.getNotes();
            r.receiptUrl = p.getReceiptUrl();
            r.voided = p.isVoided();
            r.voidedAt = p.getVoidedAt();
            r.voidedBy = p.getVoidedBy();
            return r;
        }).collect(Collectors.toList());

        return data;
    }

    @Override
    @Transactional
    public void restore(Long companyId, MultipartFile file) {
        BackupData data;
        try {
            data = objectMapper.readValue(file.getInputStream(), BackupData.class);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid backup file");
        }

        // Delete existing data in FK-safe order
        paymentRepo.deleteAllByCompanyId(companyId);
        logRepo.deleteAllByCompanyId(companyId);
        invoiceRepo.deleteItemsByCompanyId(companyId);
        invoiceRepo.deleteAllByCompanyId(companyId);
        quotationRepo.deleteItemsByCompanyId(companyId);
        quotationRepo.deleteAllByCompanyId(companyId);
        catalogRepo.deleteAllByCompanyId(companyId);
        customerRepo.deleteAllByCompanyId(companyId);

        Company company = companyRepo.getReferenceById(companyId);

        Map<Long, Long> customerIdMap = new HashMap<>();
        for (BackupData.CustomerRecord rec : safe(data.customers)) {
            Customer c = new Customer();
            c.setCompany(company);
            c.setName(rec.name);
            c.setEmail(rec.email);
            c.setPhone(rec.phone);
            c.setCompanyName(rec.companyName);
            c.setRegistrationNumber(rec.registrationNumber);
            c.setBillingAddressLine1(rec.billingAddressLine1);
            c.setBillingAddressLine2(rec.billingAddressLine2);
            c.setBillingPostcode(rec.billingPostcode);
            c.setBillingCity(rec.billingCity);
            c.setBillingState(rec.billingState);
            c.setBillingCountry(rec.billingCountry);
            c.setNotes(rec.notes);
            if (rec.status != null) {
                try { c.setStatus(CustomerStatus.valueOf(rec.status)); } catch (IllegalArgumentException ignored) {}
            }
            c.setArchivedAt(rec.archivedAt);
            customerIdMap.put(rec.id, customerRepo.save(c).getId());
        }

        for (BackupData.CatalogRecord rec : safe(data.catalog)) {
            CatalogItem ci = new CatalogItem();
            ci.setCompany(company);
            ci.setName(rec.name);
            ci.setDescription(rec.description);
            ci.setPrice(rec.price);
            ci.setArchivedAt(rec.archivedAt);
            catalogRepo.save(ci);
        }

        Map<Long, Long> quotationIdMap = new HashMap<>();
        for (BackupData.QuotationRecord rec : safe(data.quotations)) {
            Quotation q = new Quotation();
            q.setCompany(company);
            Long newCustId = rec.customerId != null ? customerIdMap.get(rec.customerId) : null;
            if (newCustId != null) q.setCustomer(customerRepo.getReferenceById(newCustId));
            q.setQuotationNumber(rec.quotationNumber);
            if (rec.status != null) {
                try { q.setStatus(QuotationStatus.valueOf(rec.status)); } catch (IllegalArgumentException ignored) {}
            }
            q.setIssueDate(rec.issueDate);
            q.setValidUntil(rec.validUntil);
            q.setCurrency(rec.currency);
            q.setCustomerName(rec.customerName);
            q.setCustomerEmail(rec.customerEmail);
            q.setBillingAddress(rec.billingAddress);
            q.setSubtotal(rec.subtotal);
            q.setDiscount(rec.discount);
            q.setTax(rec.tax);
            q.setGrandTotal(rec.grandTotal);
            for (BackupData.LineItem li : safe(rec.items)) {
                QuotationItem qi = new QuotationItem();
                qi.setDescription(li.description);
                qi.setUnitPrice(li.unitPrice);
                qi.setQuantity(li.quantity);
                qi.setSubtotal(li.subtotal);
                q.addItem(qi);
            }
            quotationIdMap.put(rec.id, quotationRepo.save(q).getId());
        }

        Map<Long, Long> invoiceIdMap = new HashMap<>();
        for (BackupData.InvoiceRecord rec : safe(data.invoices)) {
            Invoice inv = new Invoice();
            inv.setCompany(company);
            Long newCustId = rec.customerId != null ? customerIdMap.get(rec.customerId) : null;
            if (newCustId != null) inv.setCustomer(customerRepo.getReferenceById(newCustId));
            Long newQuotId = rec.sourceQuotationId != null ? quotationIdMap.get(rec.sourceQuotationId) : null;
            inv.setSourceQuotationId(newQuotId);
            inv.setInvoiceNumber(rec.invoiceNumber);
            if (rec.status != null) {
                try { inv.setStatus(InvoiceStatus.valueOf(rec.status)); } catch (IllegalArgumentException ignored) {}
            }
            inv.setIssueDate(rec.issueDate);
            inv.setDueDate(rec.dueDate);
            inv.setCurrency(rec.currency);
            inv.setNotes(rec.notes);
            inv.setCustomerName(rec.customerName);
            inv.setCustomerEmail(rec.customerEmail);
            inv.setBillingAddress(rec.billingAddress);
            inv.setSubtotal(rec.subtotal);
            inv.setDiscount(rec.discount);
            inv.setTax(rec.tax);
            inv.setGrandTotal(rec.grandTotal);
            inv.setPaidAmount(rec.paidAmount);
            inv.setRemainingAmount(rec.remainingAmount);
            inv.setIssuedAt(rec.issuedAt);
            inv.setPaidAt(rec.paidAt);
            for (BackupData.LineItem li : safe(rec.items)) {
                InvoiceItem ii = new InvoiceItem();
                ii.setDescription(li.description);
                ii.setUnitPrice(li.unitPrice);
                ii.setQuantity(li.quantity);
                ii.setSubtotal(li.subtotal);
                inv.addItem(ii);
            }
            invoiceIdMap.put(rec.id, invoiceRepo.save(inv).getId());
        }

        for (BackupData.PaymentRecord rec : safe(data.payments)) {
            Long newInvId = rec.invoiceId != null ? invoiceIdMap.get(rec.invoiceId) : null;
            if (newInvId == null) continue;
            Payment p = new Payment();
            p.setCompany(company);
            p.setInvoice(invoiceRepo.getReferenceById(newInvId));
            p.setAmount(rec.amount);
            p.setPaymentDate(rec.paymentDate);
            if (rec.method != null) {
                try { p.setMethod(PaymentMethod.valueOf(rec.method)); } catch (IllegalArgumentException ignored) {}
            }
            p.setReference(rec.reference);
            p.setNotes(rec.notes);
            p.setReceiptUrl(rec.receiptUrl);
            p.setVoided(rec.voided);
            p.setVoidedAt(rec.voidedAt);
            p.setVoidedBy(rec.voidedBy);
            paymentRepo.save(p);
        }
    }

    private BackupData.LineItem toLineItem(String desc, java.math.BigDecimal price, Integer qty, java.math.BigDecimal sub) {
        BackupData.LineItem li = new BackupData.LineItem();
        li.description = desc;
        li.unitPrice = price;
        li.quantity = qty;
        li.subtotal = sub;
        return li;
    }

    private <T> List<T> safe(List<T> list) {
        return list != null ? list : List.of();
    }
}
