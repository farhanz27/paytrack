package com.avantdream.paytrack.company.dto;

import java.util.Date;

import com.avantdream.paytrack.company.entity.Membership;

public class MemberResponse {

	private Long userId;
	private String name;
	private String email;
	private String role;
	private boolean active;
	private Date joinedAt;

	public static MemberResponse from(Membership m) {
		MemberResponse r = new MemberResponse();
		r.setUserId(m.getUser().getId());
		r.setName(m.getUser().getName());
		r.setEmail(m.getUser().getEmail());
		r.setRole(m.getRole().getName());
		r.setActive(m.isActive());
		r.setJoinedAt(m.getCreatedAt());
		return r;
	}

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }

	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }

	public Date getJoinedAt() { return joinedAt; }
	public void setJoinedAt(Date joinedAt) { this.joinedAt = joinedAt; }

}
