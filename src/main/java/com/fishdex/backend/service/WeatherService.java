package com.fishdex.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Récupère les conditions météo via OpenWeatherMap API.
 * Clé configurable via openweathermap.api-key (vide → service désactivé).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openweathermap.api-key:}")
    private String apiKey;

    private static final String OWM_URL =
            "https://api.openweathermap.org/data/2.5/weather" +
            "?lat={lat}&lon={lon}&appid={key}&units=metric&lang=fr";

    /**
     * Récupère les conditions météo pour une position donnée.
     * Retourne un Optional vide si la clé n'est pas configurée ou en cas d'erreur.
     */
    public Optional<WeatherData> fetchWeather(double lat, double lon) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("OpenWeatherMap API key non configurée — météo désactivée");
            return Optional.empty();
        }

        try {
            String url = OWM_URL
                    .replace("{lat}", String.valueOf(lat))
                    .replace("{lon}", String.valueOf(lon))
                    .replace("{key}", apiKey);

            String json = restTemplate.getForObject(url, String.class);
            if (json == null) return Optional.empty();

            JsonNode root = objectMapper.readTree(json);

            double temp     = root.path("main").path("temp").asDouble(0);
            double wind     = root.path("wind").path("speed").asDouble(0);
            double pressure = root.path("main").path("pressure").asDouble(0);
            int    clouds   = root.path("clouds").path("all").asInt(0);
            String desc     = root.path("weather").get(0).path("description").asText("");
            String icon     = root.path("weather").get(0).path("icon").asText("");

            return Optional.of(new WeatherData(temp, wind, pressure, clouds, desc, icon));

        } catch (Exception e) {
            log.warn("Impossible de récupérer la météo pour ({}, {}): {}", lat, lon, e.getMessage());
            return Optional.empty();
        }
    }

    public record WeatherData(
            double temperatureC,
            double windSpeedMs,
            double pressureHpa,
            int    cloudCoverage,
            String description,
            String icon
    ) {}
}
