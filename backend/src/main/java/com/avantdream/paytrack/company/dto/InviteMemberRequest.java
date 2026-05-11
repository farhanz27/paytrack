package com.avantdream.paytrack.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class InviteMemberRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	@Pattern(regexp = "ADMIN|MEMBER", message = "Role must be ADMIN or MEMBER")
	private String role;

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }

}
