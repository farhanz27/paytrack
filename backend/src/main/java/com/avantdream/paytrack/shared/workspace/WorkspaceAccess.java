package com.avantdream.paytrack.shared.workspace;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.avantdream.paytrack.company.entity.Membership;
import com.avantdream.paytrack.company.repository.MembershipRepository;

@Component
public class WorkspaceAccess {

    private final MembershipRepository membershipRepository;

    public WorkspaceAccess(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public long resolve(UserDetails principal, Long companyId) {
        String email = principal.getUsername();
        List<Membership> memberships = membershipRepository.findByUser_EmailIgnoreCase(email);

        if (memberships.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no company memberships");
        }

        if (companyId != null) {
            boolean isMember = memberships.stream()
                    .anyMatch(m -> m.getCompany().getId().equals(companyId));
            if (!isMember) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to company " + companyId);
            }
            return companyId;
        }

        return memberships.get(0).getCompany().getId();
    }
}
