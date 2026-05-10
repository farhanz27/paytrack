package com.avantdream.paytrack.company.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avantdream.paytrack.company.entity.Membership;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByUser_EmailIgnoreCase(String email);

    List<Membership> findByCompany_Id(Long companyId);

    Optional<Membership> findByCompany_IdAndUser_Id(Long companyId, Long userId);
}
