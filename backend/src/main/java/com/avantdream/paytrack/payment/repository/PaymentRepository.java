package com.avantdream.paytrack.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.avantdream.paytrack.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoice_IdAndCompany_IdOrderByCreatedAtDesc(Long invoiceId, Long companyId);

    List<Payment> findAllByCompany_Id(Long companyId);

    long countByCompany_Id(Long companyId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.company.id = :companyId AND YEAR(p.createdAt) = :year")
    long countByCompany_IdAndYear(@Param("companyId") Long companyId, @Param("year") int year);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Payment p WHERE p.company.id = :cid")
    void deleteAllByCompanyId(@Param("cid") Long cid);

    Page<Payment> findByCompany_IdOrderByCreatedAtDesc(Long companyId, Pageable pageable);

    Optional<Payment> findByIdAndInvoice_IdAndCompany_Id(Long id, Long invoiceId, Long companyId);
}
