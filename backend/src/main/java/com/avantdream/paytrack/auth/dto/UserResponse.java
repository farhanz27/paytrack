package com.avantdream.paytrack.auth.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.avantdream.paytrack.auth.entity.Role;
import com.avantdream.paytrack.auth.entity.User;
import com.avantdream.paytrack.company.entity.Membership;

public class UserResponse {

	public static class WorkspaceRef {
		private Long companyId;
		private String name;
		private String role;
		private boolean active;
		private java.util.Date joinedAt;

		public WorkspaceRef() {
		}

		public WorkspaceRef(Long companyId, String name, String role, boolean active, java.util.Date joinedAt) {
			this.companyId = companyId;
			this.name = name;
			this.role = role;
			this.active = active;
			this.joinedAt = joinedAt;
		}

		public Long getCompanyId() { return companyId; }
		public void setCompanyId(Long companyId) { this.companyId = companyId; }

		public String getName() { return name; }
		public void setName(String name) { this.name = name; }

		public String getRole() { return role; }
		public void setRole(String role) { this.role = role; }

		public boolean isActive() { return active; }
		public void setActive(boolean active) { this.active = active; }

		public java.util.Date getJoinedAt() { return joinedAt; }
		public void setJoinedAt(java.util.Date joinedAt) { this.joinedAt = joinedAt; }
	}

	private Long id;
	private String email;
	private String name;
	private java.util.Date createdAt;
	private List<String> roles;
	private List<WorkspaceRef> workspaces;

	public UserResponse() {
	}

	public static UserResponse fromUser(User user, List<Membership> memberships) {
		UserResponse r = new UserResponse();
		r.setId(user.getId());
		r.setEmail(user.getEmail());
		r.setName(user.getName());
		r.setCreatedAt(user.getCreatedAt());
		r.setRoles(user.getRoles().stream().map(Role::getAuthority).collect(Collectors.toList()));
		r.setWorkspaces(memberships.stream()
				.map(m -> new WorkspaceRef(m.getCompany().getId(), m.getCompany().getName(),
						m.getRole().getName(), m.isActive(), m.getCreatedAt()))
				.collect(Collectors.toList()));
		return r;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public java.util.Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(java.util.Date createdAt) { this.createdAt = createdAt; }

	public List<String> getRoles() { return roles; }
	public void setRoles(List<String> roles) { this.roles = roles; }

	public List<WorkspaceRef> getWorkspaces() { return workspaces; }
	public void setWorkspaces(List<WorkspaceRef> workspaces) { this.workspaces = workspaces; }

}
