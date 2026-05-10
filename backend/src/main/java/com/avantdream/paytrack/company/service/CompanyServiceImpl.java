package com.avantdream.paytrack.company.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.avantdream.paytrack.auth.entity.User;
import com.avantdream.paytrack.auth.repository.UserRepository;
import com.avantdream.paytrack.company.dto.CompanyRequest;
import com.avantdream.paytrack.company.dto.InviteMemberRequest;
import com.avantdream.paytrack.company.dto.MemberResponse;
import com.avantdream.paytrack.company.dto.UpdateMemberRoleRequest;
import com.avantdream.paytrack.company.entity.Company;
import com.avantdream.paytrack.company.entity.Membership;
import com.avantdream.paytrack.company.entity.MembershipRole;
import com.avantdream.paytrack.company.repository.CompanyRepository;
import com.avantdream.paytrack.company.repository.MembershipRepository;
import com.avantdream.paytrack.company.repository.MembershipRoleRepository;
import com.avantdream.paytrack.shared.exception.ResourceNotFoundException;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipRoleRepository membershipRoleRepository;
    private final UserRepository userRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository,
            MembershipRepository membershipRepository,
            MembershipRoleRepository membershipRoleRepository,
            UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.membershipRepository = membershipRepository;
        this.membershipRoleRepository = membershipRoleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Company> findAllForUser(String email) {
        return membershipRepository.findByUser_EmailIgnoreCase(email)
                .stream().map(Membership::getCompany).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Company findById(Long companyId, String email) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        requireMembership(companyId, email);
        return company;
    }

    @Override
    @Transactional(readOnly = true)
    public Company getCurrent(String email, Long companyId) {
        List<Membership> memberships = membershipRepository.findByUser_EmailIgnoreCase(email);
        if (memberships.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no company memberships");
        }
        if (companyId != null) {
            return memberships.stream()
                    .filter(m -> m.getCompany().getId().equals(companyId))
                    .map(Membership::getCompany)
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Access denied to company " + companyId));
        }
        return memberships.get(0).getCompany();
    }

    @Override
    @Transactional
    public Company create(String email, CompanyRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        MembershipRole ownerRole = roleByName(MembershipRole.OWNER);

        Company company = new Company();
        applyRequest(company, request);
        company.setStatus("ACTIVE");
        if (company.getRegistrationNumber() == null || company.getRegistrationNumber().isBlank()) {
            company.setRegistrationNumber(generateUniqueRegNo());
        }
        companyRepository.save(company);

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setCompany(company);
        membership.setRole(ownerRole);
        membershipRepository.save(membership);

        return company;
    }

    @Override
    @Transactional
    public Company update(Long companyId, String email, CompanyRequest request) {
        requireOwnerOrAdmin(companyId, email);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        applyRequest(company, request);
        return companyRepository.save(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> findMembers(Long companyId, String email) {
        requireMembership(companyId, email);
        return membershipRepository.findByCompany_Id(companyId)
                .stream().map(MemberResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MemberResponse invite(Long companyId, String actorEmail, InviteMemberRequest request) {
        requireOwnerOrAdmin(companyId, actorEmail);
        String inviteeEmail = request.getEmail().trim().toLowerCase();

        User invitee = userRepository.findByEmailIgnoreCase(inviteeEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No account found for " + inviteeEmail + ". Ask them to register first."));

        java.util.Optional<Membership> existing =
                membershipRepository.findByCompany_IdAndUser_Id(companyId, invitee.getId());
        if (existing.isPresent()) {
            Membership m = existing.get();
            if (m.isActive()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        inviteeEmail + " is already a member of this company");
            }
            m.setActive(true);
            m.setRole(roleByName(request.getRole()));
            return MemberResponse.from(membershipRepository.save(m));
        }

        MembershipRole role = roleByName(request.getRole());
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        Membership membership = new Membership();
        membership.setUser(invitee);
        membership.setCompany(company);
        membership.setRole(role);
        return MemberResponse.from(membershipRepository.save(membership));
    }

    @Override
    @Transactional
    public void removeMember(Long companyId, Long userId, String actorEmail) {
        requireOwnerOrAdmin(companyId, actorEmail);
        Membership target = membershipRepository.findByCompany_IdAndUser_Id(companyId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        if (MembershipRole.OWNER.equals(target.getRole().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove the company owner");
        }
        target.setActive(false);
        membershipRepository.save(target);
    }

    @Override
    @Transactional
    public void updateMemberRole(Long companyId, Long userId, UpdateMemberRoleRequest request, String actorEmail) {
        requireOwnerOrAdmin(companyId, actorEmail);
        Membership target = membershipRepository.findByCompany_IdAndUser_Id(companyId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        if (MembershipRole.OWNER.equals(target.getRole().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot change the owner's role directly — use transfer ownership");
        }
        target.setRole(roleByName(request.getRole()));
        membershipRepository.save(target);
    }

    @Override
    @Transactional
    public void transferOwnership(Long companyId, Long newOwnerUserId, String actorEmail) {
        // pessimistic lock to prevent race conditions
        companyRepository.lockById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        Membership currentOwnerMembership = membershipRepository.findByUser_EmailIgnoreCase(actorEmail)
                .stream().filter(m -> m.getCompany().getId().equals(companyId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member"));

        if (!MembershipRole.OWNER.equals(currentOwnerMembership.getRole().getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can transfer ownership");
        }

        Membership newOwnerMembership = membershipRepository
                .findByCompany_IdAndUser_Id(companyId, newOwnerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Target user is not a member of this company"));

        MembershipRole ownerRole = roleByName(MembershipRole.OWNER);
        MembershipRole adminRole = roleByName("ADMIN");

        // downgrade current owner to ADMIN
        currentOwnerMembership.setRole(adminRole);
        membershipRepository.save(currentOwnerMembership);

        // promote new owner
        newOwnerMembership.setRole(ownerRole);
        membershipRepository.save(newOwnerMembership);
    }

    // ---- helpers ----

    private void requireMembership(Long companyId, String email) {
        membershipRepository.findByUser_EmailIgnoreCase(email).stream()
                .filter(m -> m.getCompany().getId().equals(companyId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Access denied to company " + companyId));
    }

    private void requireOwnerOrAdmin(Long companyId, String email) {
        Membership m = membershipRepository.findByUser_EmailIgnoreCase(email).stream()
                .filter(ms -> ms.getCompany().getId().equals(companyId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Access denied to company " + companyId));
        String role = m.getRole().getName();
        if (!MembershipRole.OWNER.equals(role) && !"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
        }
    }

    private MembershipRole roleByName(String name) {
        return membershipRoleRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + name));
    }

    private void applyRequest(Company c, CompanyRequest r) {
        c.setName(r.getName().trim());
        c.setRegistrationNumber(r.getRegistrationNumber() != null ? r.getRegistrationNumber().trim() : null);
        c.setEmail(r.getEmail());
        c.setPhone(r.getPhone());
        c.setBillingAddressLine1(r.getBillingAddressLine1());
        c.setBillingAddressLine2(r.getBillingAddressLine2());
        c.setBillingCity(r.getBillingCity());
        c.setBillingState(r.getBillingState());
        c.setBillingPostcode(r.getBillingPostcode());
        c.setBillingCountry(r.getBillingCountry());
        if (r.getDefaultCurrency() != null && !r.getDefaultCurrency().isBlank()) {
            c.setDefaultCurrency(r.getDefaultCurrency().trim().toUpperCase());
        }
    }

    private String generateUniqueRegNo() {
        for (int i = 0; i < 5; i++) {
            String candidate = "PT-" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 12).toUpperCase();
            if (!companyRepository.existsByRegistrationNumber(candidate)) {
                return candidate;
            }
        }
        return "PT-" + UUID.randomUUID().toString().replace("-", "");
    }
}
