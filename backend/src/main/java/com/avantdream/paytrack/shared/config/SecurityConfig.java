package com.avantdream.paytrack.shared.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@EnableMethodSecurity(securedEnabled = true)
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));
		configuration.setAllowedMethods(
				Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public SecurityFilterChain configure(HttpSecurity http) throws Exception {
		// CsrfTokenRequestAttributeHandler: cookie token matches axios X-XSRF-TOKEN (Spring 6 default XOR does not).
		http.cors(Customizer.withDefaults()).csrf(csrf -> csrf
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));

		// Spring Security 6 requires explicit context save; wire the session repository so AuthController can call saveContext().
		http.securityContext(ctx -> ctx.securityContextRepository(securityContextRepository()));

		http.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
				.requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**", "/vite.svg", "/favicon.ico").permitAll()
				.requestMatchers(HttpMethod.GET, "/sign-in", "/sign-up", "/customers", "/customers/**",
						"/invoices", "/invoices/**", "/app", "/app/**").permitAll()
				// Swagger UI (dev / internal only)
				.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()
				// Actuator: health & info are public; restrict others to authenticated users
				.requestMatchers("/actuator/health", "/actuator/info").permitAll()
				.requestMatchers("/actuator/**").authenticated()
				.anyRequest().authenticated());

		http.formLogin(AbstractHttpConfigurer::disable);
		http.httpBasic(AbstractHttpConfigurer::disable);

		http.exceptionHandling(ex -> ex
				.authenticationEntryPoint(new AuthenticationEntryPoint() {
					@Override
					public void commence(HttpServletRequest request, HttpServletResponse response,
							AuthenticationException authException) throws IOException {
						writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
					}
				})
				.accessDeniedHandler(new AccessDeniedHandler() {
					@Override
					public void handle(HttpServletRequest request, HttpServletResponse response,
							org.springframework.security.access.AccessDeniedException ex) throws IOException {
						writeError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
					}
				}));

		http.logout(logout -> logout
				.logoutUrl("/api/auth/logout")
				.logoutSuccessHandler((req, res, auth) -> res.setStatus(204))
				.deleteCookies("JSESSIONID")
				.invalidateHttpSession(true)
				.clearAuthentication(true));

		return http.build();
	}

	private void writeError(HttpServletResponse response, int statusCode, String message) throws IOException {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		response.setStatus(statusCode);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(
				"{\"timestamp\":\"" + timestamp + "\",\"status\":" + statusCode +
				",\"message\":\"" + message + "\",\"errors\":{}}");
	}

}
