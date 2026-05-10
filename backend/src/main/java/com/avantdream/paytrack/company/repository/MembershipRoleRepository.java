package com.avantdream.paytrack.company.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avantdream.paytrack.company.entity.MembershipRole;

public interface MembershipRoleRepository extends JpaRepository<MembershipRole, Long> {

    Optional<MembershipRole> findByName(String name);
}
