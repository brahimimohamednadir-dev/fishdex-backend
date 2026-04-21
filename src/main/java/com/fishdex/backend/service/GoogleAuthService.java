package com.fishdex.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishdex.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Vérifie un ID token Google via l'endpoint tokeninfo de Google.
 * Aucune dépendance externe — utilise java.net.http.HttpClient (Java 11+).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Vérifie l'ID token Google et retourne les informations de l'utilisateur.
     *
     * @param idToken l'ID token fourni par le frontend
     * @return GoogleUserInfo avec email, name, googleId
     * @throws BusinessException si le token est invalide
     */
    public GoogleUserInfo verifyIdToken(String idToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKENINFO_URL + idToken))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Token Google invalide — statut HTTP {}", response.statusCode());
                throw new BusinessException("Token Google invalide", HttpStatus.UNAUTHORIZED);
            }

            JsonNode body = objectMapper.readTree(response.body());

            String email = body.path("email").asText();
            String googleId = body.path("sub").asText();
            boolean emailVerified = "true".equalsIgnoreCase(body.path("email_verified").asText());
            String name = body.path("name").asText(null);
            String picture = body.path("picture").asText(null);

            if (email.isBlank() || googleId.isBlank()) {
                throw new BusinessException("Token Google invalide : email ou sub manquant", HttpStatus.UNAUTHORIZED);
            }

            if (!emailVerified) {
                throw new BusinessException("L'email Google n'est pas vérifié", HttpStatus.UNAUTHORIZED);
            }

            log.debug("Token Google vérifié pour {}", email);
            return new GoogleUserInfo(googleId, email, name, picture);

        } catch (BusinessException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erreur lors de la vérification du token Google", e);
            throw new BusinessException("Impossible de vérifier le token Google", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public record GoogleUserInfo(String googleId, String email, String name, String picture) {}
}
