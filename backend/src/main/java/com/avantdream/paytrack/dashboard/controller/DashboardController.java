package com.avantdream.paytrack.dashboard.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avantdream.paytrack.dashboard.dto.DashboardResponse;
import com.avantdream.paytrack.dashboard.service.DashboardService;
import com.avantdream.paytrack.shared.exception.BadRequestException;
import com.avantdream.paytrack.shared.workspace.WorkspaceAccess;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

	private static final String COMPANY_HEADER = "X-Company-Id";

	private final DashboardService dashboardService;
	private final WorkspaceAccess workspaceAccess;

	public DashboardController(DashboardService dashboardService, WorkspaceAccess workspaceAccess) {
		this.dashboardService = dashboardService;
		this.workspaceAccess = workspaceAccess;
	}

	@Secured("ROLE_USER")
	@GetMapping
	public ResponseEntity<DashboardResponse> getDashboard(
			@AuthenticationPrincipal UserDetails principal,
			@RequestHeader(value = COMPANY_HEADER, required = false) Long companyHeader,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

		if (from != null && to != null && from.isAfter(to)) {
			throw new BadRequestException("'from' date must not be after 'to' date");
		}

		long companyId = workspaceAccess.resolve(principal, companyHeader);
		return ResponseEntity.ok(dashboardService.getDashboard(companyId, from, to));
	}
}
