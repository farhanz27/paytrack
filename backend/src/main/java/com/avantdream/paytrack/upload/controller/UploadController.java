package com.avantdream.paytrack.upload.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.avantdream.paytrack.upload.service.UploadFileService;

@RestController
public class UploadController {

	private final UploadFileService uploadFileService;

	public UploadController(UploadFileService uploadFileService) {
		this.uploadFileService = uploadFileService;
	}

	@Secured("ROLE_USER")
	@PostMapping(value = "/api/uploads/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, String>> uploadReceipt(
			@RequestParam("file") MultipartFile file) throws IOException {
		String filename = uploadFileService.copy(file);
		String url = "/uploads/" + filename;
		return ResponseEntity.ok(Map.of("url", url));
	}

	@Secured("ROLE_USER")
	@GetMapping("/uploads/{filename:.+}")
	public ResponseEntity<Resource> loadFile(@PathVariable String filename) throws MalformedURLException {
		Resource resource = uploadFileService.load(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

}
