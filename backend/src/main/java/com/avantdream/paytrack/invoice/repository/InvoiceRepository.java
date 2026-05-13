package com.avantdream.paytrack.invoice.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.avantdream.paytrack.invoice.entity.Invoice;
import com.avantdream.paytrack.invoice.entity.InvoiceStatus;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

	List<Invoice> findAllByCompany_Id(Long companyId);

	@Modifying(clearAutomatically = true)
	@Query(value = "DELETE FROM invoice_items WHERE invoice_id IN (SELECT id FROM invoices WHERE company_id = :cid)", nativeQuery = true)
	void deleteItemsByCompanyId(@Param("cid") Long cid);

	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM Invoice i WHERE i.company.id = :cid")
	void deleteAllByCompanyId(@Param("cid") Long cid);

	@Query("SELECT i FROM Invoice i JOIN FETCH i.customer JOIN FETCH i.company JOIN FETCH i.items WHERE i.id = :id AND i.company.id = :companyId")
	Invoice fetchByIdWithCustomerAndItems(@Param("id") Long id, @Param("companyId") Long companyId);

	Optional<Invoice> findByIdAndCompany_Id(Long id, Long companyId);

	Optional<Invoice> findBySourceQuotationIdAndCompany_Id(Long sourceQuotationId, Long companyId);

	long countByCompany_Id(Long companyId);

	@Query("SELECT COUNT(i) FROM Invoice i WHERE i.company.id = :companyId AND YEAR(i.createdAt) = :year")
	long countByCompany_IdAndYear(@Param("companyId") Long companyId, @Param("year") int year);

	Page<Invoice> findByCompany_Id(Long companyId, Pageable pageable);

	Page<Invoice> findByCompany_IdAndStatus(Long companyId, InvoiceStatus status, Pageable pageable);

	@Query("SELECT COALESCE(SUM(i.grandTotal), 0) FROM Invoice i WHERE i.company.id = :companyId AND i.status = com.avantdream.paytrack.invoice.entity.InvoiceStatus.PAID AND i.paidAt BETWEEN :from AND :to")
	BigDecimal sumTotalRevenue(@Param("companyId") Long companyId, @Param("from") Date from, @Param("to") Date to);

	@Query("SELECT COALESCE(SUM(i.remainingAmount), 0) FROM Invoice i WHERE i.company.id = :companyId AND i.status IN :statuses")
	BigDecimal sumOutstandingAmount(@Param("companyId") Long companyId, @Param("statuses") Collection<InvoiceStatus> statuses);

	@Query("SELECT i.status, COUNT(i) FROM Invoice i WHERE i.company.id = :companyId GROUP BY i.status")
	List<Object[]> countGroupByStatus(@Param("companyId") Long companyId);

	@Query(value = "SELECT DATE_FORMAT(paid_at, '%Y-%m') AS month, SUM(grand_total) AS revenue FROM invoices WHERE company_id = :companyId AND status = 'PAID' AND paid_at BETWEEN :from AND :to GROUP BY DATE_FORMAT(paid_at, '%Y-%m') ORDER BY month ASC", nativeQuery = true)
	List<Object[]> monthlyRevenue(@Param("companyId") Long companyId, @Param("from") Date from, @Param("to") Date to);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Invoice i SET i.pdfS3Key = :key WHERE i.id = :id")
	void updatePdfS3Key(@Param("id") Long id, @Param("key") String key);

}
