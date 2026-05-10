package com.avantdream.paytrack.company.service;

import java.util.List;

import com.avantdream.paytrack.company.dto.CompanyRequest;
import com.avantdream.paytrack.company.dto.InviteMemberRequest;
import com.avantdream.paytrack.company.dto.MemberResponse;
import com.avantdream.paytrack.company.dto.UpdateMemberRoleRequest;
import com.avantdream.paytrack.company.entity.Company;

public interface CompanyService {

    List<Company> findAllForUser(String email);

    Company findById(Long companyId, String email);

    Company getCurrent(String email, Long companyId);

    Company create(String email, CompanyRequest request);

    Company update(Long companyId, String email, CompanyRequest request);

    List<MemberResponse> findMembers(Long companyId, String email);

    MemberResponse invite(Long companyId, String actorEmail, InviteMemberRequest request);

    void removeMember(Long companyId, Long userId, String actorEmail);

    void updateMemberRole(Long companyId, Long userId, UpdateMemberRoleRequest request, String actorEmail);

    void transferOwnership(Long companyId, Long newOwnerUserId, String actorEmail);
}
