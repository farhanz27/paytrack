package com.avantdream.paytrack.settings.backup.service;

import com.avantdream.paytrack.settings.backup.dto.BackupData;
import org.springframework.web.multipart.MultipartFile;

public interface BackupService {
    BackupData export(Long companyId);
    void restore(Long companyId, MultipartFile file);
}
