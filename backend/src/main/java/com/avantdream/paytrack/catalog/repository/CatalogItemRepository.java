package com.avantdream.paytrack.catalog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.avantdream.paytrack.catalog.entity.CatalogItem;

public interface CatalogItemRepository extends JpaRepository<CatalogItem, Long> {

    Optional<CatalogItem> findByIdAndCompany_Id(Long id, Long companyId);

    List<CatalogItem> findAllByCompany_Id(Long companyId);

    long countByCompany_Id(Long companyId);

    @Modifying
    @Query("DELETE FROM CatalogItem c WHERE c.company.id = :cid")
    void deleteAllByCompanyId(@Param("cid") Long cid);
}
