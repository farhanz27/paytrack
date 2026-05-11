package com.avantdream.paytrack.customer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.avantdream.paytrack.customer.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByIdAndCompany_Id(Long id, Long companyId);

    List<Customer> findAllByCompany_Id(Long companyId);

    long countByCompany_Id(Long companyId);

    Page<Customer> findByCompany_Id(Long companyId, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.company.id = :cid AND (" +
           "LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%',:q,'%')))")
    List<Customer> searchByCompany(@Param("cid") Long companyId, @Param("q") String q);

    @Modifying
    @Query("DELETE FROM Customer c WHERE c.company.id = :cid")
    void deleteAllByCompanyId(@Param("cid") Long cid);
}
