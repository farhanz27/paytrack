package com.avantdream.paytrack.customer.controller;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avantdream.paytrack.customer.dto.CustomerRequest;
import com.avantdream.paytrack.customer.dto.CustomerResponse;
import com.avantdream.paytrack.customer.entity.Customer;
import com.avantdream.paytrack.customer.service.CustomerService;
import com.avantdream.paytrack.shared.dto.PageResponse;
import com.avantdream.paytrack.shared.workspace.WorkspaceAccess;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

	private static final String COMPANY_HEADER = "X-Company-Id";

	private final CustomerService customerService;
	private final WorkspaceAccess workspaceAccess;

	public CustomerController(CustomerService customerService, WorkspaceAccess workspaceAccess) {
		this.customerService = customerService;
		this.workspaceAccess = workspaceAccess;
	}

	@Secured("ROLE_USER")
	@GetMapping
	public ResponseEntity<PageResponse<CustomerResponse>> list(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		return ResponseEntity.ok(PageResponse.from(
				customerService.findAll(companyId, pageable).map(CustomerResponse::from)));
	}

	@Secured("ROLE_USER")
	@GetMapping("/search")
	public ResponseEntity<List<CustomerResponse>> search(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@RequestParam(required = false, defaultValue = "") String q) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		List<CustomerResponse> results = customerService.search(companyId, q)
				.stream().map(CustomerResponse::from).collect(Collectors.toList());
		return ResponseEntity.ok(results);
	}

	@Secured("ROLE_USER")
	@GetMapping("/{id}")
	public ResponseEntity<CustomerResponse> view(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		return ResponseEntity.ok(CustomerResponse.from(customerService.findById(companyId, id)));
	}

	@Secured("ROLE_USER")
	@PostMapping
	public ResponseEntity<CustomerResponse> create(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@Valid @RequestBody CustomerRequest request) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		Customer saved = customerService.create(companyId, request);
		return ResponseEntity.created(URI.create("/api/customers/" + saved.getId()))
				.body(CustomerResponse.from(saved));
	}

	@Secured("ROLE_USER")
	@PutMapping("/{id}")
	public ResponseEntity<CustomerResponse> update(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id,
			@Valid @RequestBody CustomerRequest request) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		return ResponseEntity.ok(CustomerResponse.from(customerService.update(companyId, id, request)));
	}

	@Secured("ROLE_USER")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		customerService.delete(companyId, id);
		return ResponseEntity.noContent().build();
	}

}
