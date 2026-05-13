package com.avantdream.paytrack.quotation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.avantdream.paytrack.quotation.entity.Quotation;
import com.avantdream.paytrack.quotation.entity.QuotationStatus;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {

	List<Quotation> findAllByCompany_Id(Long companyId);

	@Modifying(clearAutomatically = true)
	@Query(value = "DELETE FROM quotation_items WHERE quotation_id IN (SELECT id FROM quotations WHERE company_id = :cid)", nativeQuery = true)
	void deleteItemsByCompanyId(@Param("cid") Long cid);

	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM Quotation q WHERE q.company.id = :cid")
	void deleteAllByCompanyId(@Param("cid") Long cid);

	@Query("SELECT q FROM Quotation q JOIN FETCH q.customer JOIN FETCH q.company JOIN FETCH q.items WHERE q.id = :id AND q.company.id = :companyId")
	Quotation fetchByIdWithCustomerAndItems(@Param("id") Long id, @Param("companyId") Long companyId);

	Optional<Quotation> findByIdAndCompany_Id(Long id, Long companyId);

	long countByCompany_Id(Long companyId);

	@Query("SELECT COUNT(q) FROM Quotation q WHERE q.company.id = :companyId AND YEAR(q.createdAt) = :year")
	long countByCompany_IdAndYear(@Param("companyId") Long companyId, @Param("year") int year);

	Page<Quotation> findByCompany_Id(Long companyId, Pageable pageable);

	Page<Quotation> findByCompany_IdAndStatus(Long companyId, QuotationStatus status, Pageable pageable);

	@Query("SELECT q.status, COUNT(q) FROM Quotation q WHERE q.company.id = :companyId GROUP BY q.status")
	List<Object[]> countGroupByStatus(@Param("companyId") Long companyId);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Quotation q SET q.pdfS3Key = :key WHERE q.id = :id")
	void updatePdfS3Key(@Param("id") Long id, @Param("key") String key);

}
