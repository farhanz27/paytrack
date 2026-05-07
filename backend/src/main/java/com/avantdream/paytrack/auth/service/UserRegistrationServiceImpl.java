package com.avantdream.paytrack.auth.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avantdream.paytrack.auth.dto.RegisterRequest;
import com.avantdream.paytrack.auth.entity.Role;
import com.avantdream.paytrack.auth.entity.User;
import com.avantdream.paytrack.auth.repository.UserRepository;
import com.avantdream.paytrack.company.entity.Company;
import com.avantdream.paytrack.company.entity.Membership;
import com.avantdream.paytrack.company.entity.MembershipRole;
import com.avantdream.paytrack.company.repository.CompanyRepository;
import com.avantdream.paytrack.company.repository.MembershipRepository;
import com.avantdream.paytrack.company.repository.MembershipRoleRepository;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final MembershipRepository membershipRepository;
	private final MembershipRoleRepository membershipRoleRepository;
	private final PasswordEncoder passwordEncoder;
	private final String demoShellRegistrationNumber;

	public UserRegistrationServiceImpl(UserRepository userRepository, CompanyRepository companyRepository,
			MembershipRepository membershipRepository, MembershipRoleRepository membershipRoleRepository,
			PasswordEncoder passwordEncoder,
			@Value("${paytrack.demo-shell-registration-number:198601234501}") String demoShellRegistrationNumber) {
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.membershipRepository = membershipRepository;
		this.membershipRoleRepository = membershipRoleRepository;
		this.passwordEncoder = passwordEncoder;
		this.demoShellRegistrationNumber = demoShellRegistrationNumber;
	}

	@Override
	@Transactional
	public void register(RegisterRequest request) {
		String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
		String companyName = request.getCompanyName().trim();

		if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
			throw new IllegalArgumentException("That email is already registered");
		}

		MembershipRole ownerRole = membershipRoleRepository.findByName(MembershipRole.OWNER)
				.orElseThrow(() -> new IllegalStateException("OWNER role not found in membership_roles"));
		MembershipRole staffRole = membershipRoleRepository.findByName(MembershipRole.MEMBER)
				.orElseThrow(() -> new IllegalStateException("MEMBER role not found in membership_roles"));

		User user = new User();
		user.setEmail(normalizedEmail);
		user.setName(request.getName().trim());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setEnabled(true);

		Role authority = new Role();
		authority.setAuthority("ROLE_USER");
		authority.setUser(user);

		List<Role> authorities = new ArrayList<>();
		authorities.add(authority);
		user.setRoles(authorities);

		userRepository.save(user);

		Company company = new Company();
		company.setName(companyName);
		company.setRegistrationNumber(uniqueRegistrationNumber());
		company.setStatus("ACTIVE");
		company.setBillingCountry("Malaysia");
		company.setPhone("");
		company.setEmail(normalizedEmail);
		company.setBillingAddressLine1("");
		company.setBillingCity("");
		company.setBillingPostcode("");
		company.setBillingState("");
		companyRepository.save(company);

		Membership membership = new Membership();
		membership.setUser(user);
		membership.setCompany(company);
		membership.setRole(ownerRole);
		membershipRepository.save(membership);

		if (demoShellRegistrationNumber != null && !demoShellRegistrationNumber.isBlank()) {
			companyRepository.findByRegistrationNumber(demoShellRegistrationNumber.trim()).ifPresent(shell -> {
				if (!shell.getId().equals(company.getId())) {
					Membership shellAccess = new Membership();
					shellAccess.setUser(user);
					shellAccess.setCompany(shell);
					shellAccess.setRole(staffRole);
					membershipRepository.save(shellAccess);
				}
			});
		}
	}

	private String uniqueRegistrationNumber() {
		for (int i = 0; i < 5; i++) {
			String candidate = "PT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
			if (!companyRepository.existsByRegistrationNumber(candidate)) {
				return candidate;
			}
		}
		return "PT-" + UUID.randomUUID().toString().replace("-", "");
	}

	static String defaultUsernameFromEmail(String normalizedEmail) {
		int at = normalizedEmail.indexOf('@');
		String local = at > 0 ? normalizedEmail.substring(0, at) : normalizedEmail;
		if (local.isBlank()) {
			return "User";
		}
		return Arrays.stream(local.split("[._-]+")).filter(s -> !s.isBlank()).map(s -> Character
				.toUpperCase(s.charAt(0))
				+ s.substring(1).toLowerCase(Locale.ROOT))
				.collect(Collectors.joining(" "));
	}

}
