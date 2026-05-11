package com.avantdream.paytrack.catalog.controller;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

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
import org.springframework.web.bind.annotation.RestController;

import com.avantdream.paytrack.catalog.dto.CatalogItemRequest;
import com.avantdream.paytrack.catalog.dto.CatalogItemResponse;
import com.avantdream.paytrack.catalog.entity.CatalogItem;
import com.avantdream.paytrack.catalog.service.CatalogItemService;
import com.avantdream.paytrack.shared.workspace.WorkspaceAccess;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

	private static final String COMPANY_HEADER = "X-Company-Id";

	private final CatalogItemService catalogItemService;
	private final WorkspaceAccess workspaceAccess;

	public CatalogController(CatalogItemService catalogItemService, WorkspaceAccess workspaceAccess) {
		this.catalogItemService = catalogItemService;
		this.workspaceAccess = workspaceAccess;
	}

	@Secured("ROLE_USER")
	@GetMapping
	public ResponseEntity<List<CatalogItemResponse>> list(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		List<CatalogItemResponse> items = catalogItemService.findAll(companyId)
				.stream().map(CatalogItemResponse::from).collect(Collectors.toList());
		return ResponseEntity.ok(items);
	}

	@Secured("ROLE_USER")
	@GetMapping("/{id}")
	public ResponseEntity<CatalogItemResponse> view(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		return ResponseEntity.ok(CatalogItemResponse.from(catalogItemService.findById(companyId, id)));
	}

	@Secured("ROLE_USER")
	@PostMapping
	public ResponseEntity<CatalogItemResponse> create(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@Valid @RequestBody CatalogItemRequest request) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		CatalogItem saved = catalogItemService.create(companyId, request);
		return ResponseEntity.created(URI.create("/api/catalog/" + saved.getId()))
				.body(CatalogItemResponse.from(saved));
	}

	@Secured("ROLE_USER")
	@PutMapping("/{id}")
	public ResponseEntity<CatalogItemResponse> update(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id,
			@Valid @RequestBody CatalogItemRequest request) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		return ResponseEntity.ok(CatalogItemResponse.from(catalogItemService.update(companyId, id, request)));
	}

	@Secured("ROLE_USER")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> archive(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@PathVariable Long id) {

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		catalogItemService.archive(companyId, id);
		return ResponseEntity.noContent().build();
	}

}
