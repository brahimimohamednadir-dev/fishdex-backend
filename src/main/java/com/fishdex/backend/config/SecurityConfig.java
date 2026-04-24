package com.fishdex.backend.config;

import com.fishdex.backend.security.JwtAccessDeniedHandler;
import com.fishdex.backend.security.JwtAuthFilter;
import com.fishdex.backend.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // ── CSRF désactivé (API stateless JWT) ───────────────────────
                .csrf(AbstractHttpConfigurer::disable)

                // ── CORS auto-détecte le bean CorsConfigurationSource ─────────
                .cors(cors -> cors.configure(http))

                // ── Headers de sécurité ───────────────────────────────────────
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; img-src 'self' data: https:; " +
                                                  "frame-ancestors 'none'"))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31_536_000))
                )

                // ── Règles d'autorisation ─────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        // Auth — public
                        .requestMatchers("/api/auth/**").permitAll()
                        // 2FA verify step 2 — public (non authentifié, utilise tempToken)
                        .requestMatchers(HttpMethod.POST, "/api/2fa/verify").permitAll()
                        // Catalogue espèces — public en lecture seule
                        .requestMatchers(HttpMethod.GET, "/api/species", "/api/species/**").permitAll()
                        // Actuator health
                        .requestMatchers("/actuator/health").permitAll()
                        // 2FA setup/enable/disable/status — authentifié (géré par anyRequest)
                        // Sessions — authentifié (géré par anyRequest)
                        // Tout le reste requiert une authentification
                        .anyRequest().authenticated()
                )

                // ── Sessions stateless (JWT) ──────────────────────────────────
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ── Gestion des erreurs — réponses JSON ───────────────────────
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // ── Provider d'authentification ───────────────────────────────
                .authenticationProvider(authenticationProvider())

                // ── Filtre JWT avant UsernamePasswordAuthenticationFilter ──────
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
