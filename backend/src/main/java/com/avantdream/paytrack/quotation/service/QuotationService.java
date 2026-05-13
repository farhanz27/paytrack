package com.avantdream.paytrack.quotation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.avantdream.paytrack.invoice.entity.Invoice;
import com.avantdream.paytrack.quotation.dto.QuotationSaveRequest;
import com.avantdream.paytrack.quotation.entity.Quotation;
import com.avantdream.paytrack.quotation.entity.QuotationStatus;

public interface QuotationService {

	Quotation save(Quotation quotation, String createdBy);

	Quotation findById(Long companyId, Long id);

	Quotation findByIdWithDetails(Long companyId, Long id);

	Page<Quotation> findAll(Long companyId, QuotationStatus status, Pageable pageable);

	Quotation update(Long companyId, Long id, QuotationSaveRequest request);

	Quotation transitionStatus(Long companyId, Long id, QuotationStatus newStatus, String changedBy);

	void delete(Long companyId, Long id);

	Invoice convertToInvoice(Long companyId, Long id, String convertedBy);

}
