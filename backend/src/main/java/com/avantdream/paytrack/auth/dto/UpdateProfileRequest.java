package com.avantdream.paytrack.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

	@NotBlank
	@Size(min = 2, max = 128)
	private String name;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

}
