package com.avantdream.paytrack.company.controller;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avantdream.paytrack.company.dto.CompanyRequest;
import com.avantdream.paytrack.company.dto.CompanyResponse;
import com.avantdream.paytrack.company.dto.InviteMemberRequest;
import com.avantdream.paytrack.company.dto.MemberResponse;
import com.avantdream.paytrack.company.dto.UpdateMemberRoleRequest;
import com.avantdream.paytrack.company.entity.Company;
import com.avantdream.paytrack.company.service.CompanyService;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

	private static final String COMPANY_HEADER = "X-Company-Id";

	private final CompanyService companyService;

	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}

	@Secured("ROLE_USER")
	@GetMapping
	public ResponseEntity<List<CompanyResponse>> list(@AuthenticationPrincipal UserDetails principal) {
		List<CompanyResponse> companies = companyService.findAllForUser(principal.getUsername())
				.stream().map(CompanyResponse::from).collect(Collectors.toList());
		return ResponseEntity.ok(companies);
	}

	@Secured("ROLE_USER")
	@GetMapping("/current")
	public ResponseEntity<CompanyResponse> getCurrent(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader) {
		Company company = companyService.getCurrent(principal.getUsername(), companyHeader);
		return ResponseEntity.ok(CompanyResponse.from(company));
	}

	@Secured("ROLE_USER")
	@GetMapping("/{id}")
	public ResponseEntity<CompanyResponse> view(
			@AuthenticationPrincipal UserDetails principal,
			@PathVariable Long id) {
		return ResponseEntity.ok(CompanyResponse.from(companyService.findById(id, principal.getUsername())));
	}

	@Secured("ROLE_USER")
	@PostMapping
	public ResponseEntity<CompanyResponse> create(
			@AuthenticationPrincipal UserDetails principal,
			@Valid @RequestBody CompanyRequest request) {
		Company saved = companyService.create(principal.getUsername(), request);
		return ResponseEntity.created(URI.create("/api/companies/" + saved.getId()))
				.body(CompanyResponse.from(saved));
	}

	@Secured("ROLE_USER")
	@PutMapping("/{id}")
	public ResponseEntity<CompanyResponse> update(
			@AuthenticationPrincipal UserDetails principal,
			@PathVariable Long id,
			@Valid @RequestBody CompanyRequest request) {
		return ResponseEntity.ok(CompanyResponse.from(companyService.update(id, principal.getUsername(), request)));
	}

	@Secured("ROLE_USER")
	@GetMapping("/{id}/members")
	public ResponseEntity<List<MemberResponse>> listMembers(
			@AuthenticationPrincipal UserDetails principal,
			@PathVariable Long id) {
		return ResponseEntity.ok(companyService.findMembers(id, principal.getUsername()));
	}

	@Secured("ROLE_USER")
	@PostMapping("/{id}/members/invite")
	public ResponseEntity<MemberResponse> invite(
			@AuthenticationPrincipal UserDetails principal,
			@PathVariable Long id,
			@Valid @RequestBody InviteMemberRequest request) {
		MemberResponse member = companyService.invite(id, principal.getUsername(), request);
		return ResponseEntity.status(201).body(member);
	}

	@Secured("ROLE_USER")
	@PostMapping("/{id}/members/{userId}/remove")
	public ResponseEntity<Void> removeMember(
			@AuthenticationPrincipal UserDetails principal,
			@PathVariable Long id,
			@PathVariable Long userId) {
		companyService.removeMember(id, userId, principal.getUsername());
		return ResponseEntity.noContent().build();
	}

	@Secured("ROLE_USER")
	@PatchMapping("/{id}/members/{userId}/role")
	public ResponseEntity<Void> updateMemberRole(
			@AuthenticationPrincipal UserDetails principal,
			@PathVariable Long id,
			@PathVariable Long userId,
			@Valid @RequestBody UpdateMemberRoleRequest request) {
		companyService.updateMemberRole(id, userId, request, principal.getUsername());
		return ResponseEntity.noContent().build();
	}

	@Secured("ROLE_USER")
	@PostMapping("/{id}/members/{userId}/transfer-ownership")
	public ResponseEntity<Void> transferOwnership(
			@AuthenticationPrincipal UserDetails principal,
			@PathVariable Long id,
			@PathVariable Long userId) {
		companyService.transferOwnership(id, userId, principal.getUsername());
		return ResponseEntity.noContent().build();
	}

}
