package com.avantdream.paytrack.invoice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.avantdream.paytrack.invoice.entity.InvoiceStatusLog;

public interface InvoiceStatusLogRepository extends JpaRepository<InvoiceStatusLog, Long> {

	List<InvoiceStatusLog> findByInvoiceIdOrderByChangedAtAsc(Long invoiceId);

	@Modifying(clearAutomatically = true)
	@Query(value = "DELETE FROM invoice_status_logs WHERE invoice_id IN (SELECT id FROM invoices WHERE company_id = :cid)", nativeQuery = true)
	void deleteAllByCompanyId(@Param("cid") Long cid);

}
