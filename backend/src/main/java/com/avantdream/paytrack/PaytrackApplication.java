package com.avantdream.paytrack;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.avantdream.paytrack.upload.service.UploadFileService;

@SpringBootApplication
public class PaytrackApplication implements CommandLineRunner {

	private final UploadFileService uploadService;

	public PaytrackApplication(UploadFileService uploadService) {
		this.uploadService = uploadService;
	}

	public static void main(String[] args) {
		SpringApplication.run(PaytrackApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		uploadService.deleteAll();
		uploadService.init();
	}

}
