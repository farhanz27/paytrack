package com.avantdream.paytrack.auth.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avantdream.paytrack.auth.dto.ChangePasswordRequest;
import com.avantdream.paytrack.auth.dto.LoginRequest;
import com.avantdream.paytrack.auth.dto.RegisterRequest;
import com.avantdream.paytrack.auth.dto.UpdateProfileRequest;
import com.avantdream.paytrack.auth.dto.UserResponse;
import com.avantdream.paytrack.auth.entity.User;
import com.avantdream.paytrack.auth.repository.UserRepository;
import com.avantdream.paytrack.auth.service.UserRegistrationService;
import com.avantdream.paytrack.company.entity.Membership;
import com.avantdream.paytrack.company.repository.MembershipRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;
	private final UserRepository userRepository;
	private final MembershipRepository membershipRepository;
	private final UserRegistrationService userRegistrationService;
	private final PasswordEncoder passwordEncoder;

	public AuthController(AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository,
			UserRepository userRepository,
			MembershipRepository membershipRepository,
			UserRegistrationService userRegistrationService,
			PasswordEncoder passwordEncoder) {
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
		this.userRepository = userRepository;
		this.membershipRepository = membershipRepository;
		this.userRegistrationService = userRegistrationService;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/csrf")
	public ResponseEntity<Void> csrf() {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/login")
	public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request,
			HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		Authentication auth = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail().trim().toLowerCase(Locale.ROOT),
						request.getPassword()));

		SecurityContext ctx = SecurityContextHolder.createEmptyContext();
		ctx.setAuthentication(auth);
		SecurityContextHolder.setContext(ctx);
		securityContextRepository.saveContext(ctx, servletRequest, servletResponse);

		String email = auth.getName();
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
		List<Membership> memberships = membershipRepository.findByUser_EmailIgnoreCase(email);
		return ResponseEntity.ok(UserResponse.fromUser(user, memberships));
	}

	@PostMapping("/register")
	public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
		userRegistrationService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponse> me(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		String email = authentication.getName();
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
		List<Membership> memberships = membershipRepository.findByUser_EmailIgnoreCase(email);
		return ResponseEntity.ok(UserResponse.fromUser(user, memberships));
	}

	@Secured("ROLE_USER")
	@PutMapping("/profile")
	public ResponseEntity<UserResponse> updateProfile(
			Authentication authentication,
			@Valid @RequestBody UpdateProfileRequest request) {
		String email = authentication.getName();
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
		user.setName(request.getName().trim());
		userRepository.save(user);
		List<Membership> memberships = membershipRepository.findByUser_EmailIgnoreCase(email);
		return ResponseEntity.ok(UserResponse.fromUser(user, memberships));
	}

	@Secured("ROLE_USER")
	@PutMapping("/password")
	public ResponseEntity<Void> changePassword(
			Authentication authentication,
			@Valid @RequestBody ChangePasswordRequest request) {
		String email = authentication.getName();
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
		if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
					"Current password is incorrect");
		}
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
		return ResponseEntity.noContent().build();
	}

}
