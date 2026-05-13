package com.avantdream.paytrack.settings.backup.controller;

import java.time.LocalDate;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.avantdream.paytrack.settings.backup.dto.BackupData;
import com.avantdream.paytrack.settings.backup.service.BackupService;
import com.avantdream.paytrack.shared.workspace.WorkspaceAccess;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/settings/backup")
public class BackupController {

    private static final String COMPANY_HEADER = "X-Company-Id";

    private final BackupService backupService;
    private final WorkspaceAccess workspaceAccess;
    private final ObjectMapper objectMapper;

    public BackupController(BackupService backupService, WorkspaceAccess workspaceAccess, ObjectMapper objectMapper) {
        this.backupService = backupService;
        this.workspaceAccess = workspaceAccess;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> export(
            @RequestHeader(value = COMPANY_HEADER, required = false) Long companyId,
            @AuthenticationPrincipal UserDetails principal) throws Exception {

        Long resolvedCompanyId = workspaceAccess.resolve(principal, companyId);
        BackupData data = backupService.export(resolvedCompanyId);

        byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
        String filename = "paytrack-backup-" + data.companyName.replaceAll("[^a-zA-Z0-9]", "-") + "-" + LocalDate.now() + ".json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(json.length)
                .body(new ByteArrayResource(json));
    }

    @PostMapping("/restore")
    public ResponseEntity<Void> restore(
            @RequestHeader(value = COMPANY_HEADER, required = false) Long companyId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails principal) {

        Long resolvedCompanyId = workspaceAccess.resolve(principal, companyId);
        backupService.restore(resolvedCompanyId, file);
        return ResponseEntity.ok().build();
    }
}
