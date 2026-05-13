package com.avantdream.paytrack.invoice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.avantdream.paytrack.invoice.dto.InvoiceSaveRequest;
import com.avantdream.paytrack.invoice.entity.Invoice;
import com.avantdream.paytrack.invoice.entity.InvoiceStatus;

public interface InvoiceService {

	Invoice save(Invoice invoice, String changedBy);

	Invoice findById(Long companyId, Long id);

	Invoice findByIdWithDetails(Long companyId, Long id);

	Page<Invoice> findAll(Long companyId, InvoiceStatus status, Pageable pageable);

	Invoice update(Long companyId, Long id, InvoiceSaveRequest request);

	Invoice transitionStatus(Long companyId, Long id, InvoiceStatus newStatus, String changedBy);

	void delete(Long companyId, Long id);

}
