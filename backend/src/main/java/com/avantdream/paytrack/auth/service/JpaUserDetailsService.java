package com.avantdream.paytrack.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avantdream.paytrack.auth.repository.UserRepository;
import com.avantdream.paytrack.auth.entity.Role;
import com.avantdream.paytrack.auth.entity.User;

@Service("jpaUserDetailsService")
public class JpaUserDetailsService implements UserDetailsService {

	private final Logger logger = LoggerFactory.getLogger(JpaUserDetailsService.class);

	private final UserRepository userRepository;

	public JpaUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
		String key = emailOrUsername == null ? ""
				: emailOrUsername.trim().toLowerCase(Locale.ROOT);

		User dbUser = userRepository.findByEmailIgnoreCase(key).orElseThrow(() -> {
			logger.error("Login error: no account for {}", key);
			return new UsernameNotFoundException("Invalid credentials");
		});

		List<GrantedAuthority> authorities = new ArrayList<>();

		for (Role role : dbUser.getRoles()) {
			logger.debug("Authority: {}", role.getAuthority());
			authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
		}

		if (authorities.isEmpty()) {
			logger.error("Login error: user {} has no roles assigned!", key);
			throw new UsernameNotFoundException("Login error: user has no roles assigned!");
		}

		return org.springframework.security.core.userdetails.User.withUsername(dbUser.getEmail())
				.password(dbUser.getPassword())
				.disabled(!Boolean.TRUE.equals(dbUser.getEnabled()))
				.accountExpired(false)
				.credentialsExpired(false)
				.accountLocked(false)
				.authorities(authorities)
				.build();
	}

}
