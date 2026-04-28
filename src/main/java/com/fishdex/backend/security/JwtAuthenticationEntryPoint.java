package com.fishdex.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishdex.backend.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Retourne un JSON ApiResponse 401 au lieu de la page d'erreur HTML de Tomcat.
 * Ajoute les headers CORS pour que le frontend Angular reçoive bien le 401 (et non
 * une CORS error opaque qui empêche l'intercepteur de relancer le refresh token).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Garantit que le header CORS est présent même sur les erreurs 401
        // (le CorsFilter a déjà tourné, mais certains chemins court-circuitent la chaîne)
        String origin = request.getHeader("Origin");
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Vary", "Origin");
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                ApiResponse.error("Authentification requise. Veuillez vous connecter."));
    }
}
