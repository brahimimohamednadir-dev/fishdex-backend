package com.fishdex.backend.service;

import com.fishdex.backend.dto.CaptureRequest;
import com.fishdex.backend.dto.CaptureResponse;
import com.fishdex.backend.entity.Capture;
import com.fishdex.backend.entity.Species;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.SpeciesRepository;
import com.fishdex.backend.repository.UserRepository;
import com.fishdex.backend.repository.spec.CaptureSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaptureService {

    private static final int FREEMIUM_CAPTURE_LIMIT = 50;
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("caughtAt", "weight", "length", "createdAt");

    @Value("${cloudinary.cloud-name:demo}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.api-key:}")
    private String cloudinaryApiKey;

    @Value("${cloudinary.api-secret:}")
    private String cloudinaryApiSecret;

    private final CaptureRepository captureRepository;
    private final SpeciesRepository speciesRepository;
    private final UserRepository userRepository;

    // ── Création ──────────────────────────────────────────────────────────

    @Transactional
    public CaptureResponse createCapture(CaptureRequest request, User user) {
        if (!user.getIsPremium() &&
                captureRepository.countByUserId(user.getId()) >= FREEMIUM_CAPTURE_LIMIT) {
            throw new BusinessException(
                    "Limite de " + FREEMIUM_CAPTURE_LIMIT +
                    " captures atteinte. Passez à Premium pour continuer.",
                    HttpStatus.FORBIDDEN);
        }

        Species species = resolveSpecies(request);
        String speciesName = resolveSpeciesName(request, species);

        com.fishdex.backend.entity.Capture.Visibility visibility =
                com.fishdex.backend.entity.Capture.Visibility.PUBLIC;
        if (request.getVisibility() != null) {
            try { visibility = com.fishdex.backend.entity.Capture.Visibility.valueOf(request.getVisibility()); }
            catch (IllegalArgumentException ignored) {}
        }

        Capture capture = Capture.builder()
                .user(user)
                .speciesName(speciesName)
                .species(species)
                .weight(request.getWeight())
                .length(request.getLength())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .note(request.getNote())
                .caughtAt(request.getCaughtAt())
                .visibility(visibility)
                .build();

        Capture saved = captureRepository.save(capture);

        user.setCaptureCount(user.getCaptureCount() + 1);
        userRepository.save(user);

        log.info("Capture créée — user={}, espèce={}, poids={}kg",
                user.getEmail(), speciesName, request.getWeight());
        return CaptureResponse.from(saved);
    }

    // ── Lecture paginée avec filtres ──────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<CaptureResponse> getMyCaptures(User user,
                                               Long speciesId,
                                               LocalDate from,
                                               LocalDate to,
                                               String sortBy,
                                               String sortDir,
                                               Pageable pageable) {
        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "caughtAt";
        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), sort);

        Specification<Capture> spec = Specification
                .where(CaptureSpecification.belongsToUser(user.getId()));

        if (speciesId != null) spec = spec.and(CaptureSpecification.hasSpecies(speciesId));
        if (from != null)      spec = spec.and(CaptureSpecification.caughtAfter(from.atStartOfDay()));
        if (to != null)        spec = spec.and(CaptureSpecification.caughtBefore(to.plusDays(1).atStartOfDay()));

        return captureRepository.findAll(spec, sortedPageable).map(CaptureResponse::from);
    }

    // ── Lecture par ID ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CaptureResponse getCaptureById(Long id, User user) {
        return CaptureResponse.from(findAndCheckOwner(id, user));
    }

    // ── Mise à jour ───────────────────────────────────────────────────────

    @Transactional
    public CaptureResponse updateCapture(Long id, CaptureRequest request, User user) {
        Capture capture = findAndCheckOwner(id, user);
        Species species = resolveSpecies(request);
        String speciesName = resolveSpeciesName(request, species);

        capture.setSpeciesName(speciesName);
        capture.setSpecies(species);
        capture.setWeight(request.getWeight());
        capture.setLength(request.getLength());
        capture.setLatitude(request.getLatitude());
        capture.setLongitude(request.getLongitude());
        capture.setNote(request.getNote());
        capture.setCaughtAt(request.getCaughtAt());
        if (request.getVisibility() != null) {
            try { capture.setVisibility(com.fishdex.backend.entity.Capture.Visibility.valueOf(request.getVisibility())); }
            catch (IllegalArgumentException ignored) {}
        }

        return CaptureResponse.from(captureRepository.save(capture));
    }

    // ── Suppression ───────────────────────────────────────────────────────

    @Transactional
    public void deleteCapture(Long id, User user) {
        findAndCheckOwner(id, user);
        captureRepository.deleteById(id);
        user.setCaptureCount(Math.max(0, user.getCaptureCount() - 1));
        userRepository.save(user);
        log.info("Capture {} supprimée par {}", id, user.getEmail());
    }

    // ── Photos ────────────────────────────────────────────────────────────

    /**
     * Upload d'une photo via Cloudinary (si configuré) ou stockage local en fallback.
     * Le frontend envoie un FormData avec le champ "photo".
     */
    @Transactional
    public CaptureResponse uploadPhoto(Long id, MultipartFile file, User user) {
        Capture capture = findAndCheckOwner(id, user);

        if (file == null || file.isEmpty()) {
            throw new BusinessException("Le fichier photo est vide", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("Seules les images sont acceptées", HttpStatus.BAD_REQUEST);
        }

        String photoUrl = uploadToCloudinary(file);
        capture.setPhotoUrl(photoUrl);
        Capture saved = captureRepository.save(capture);

        log.info("Photo uploadée pour capture {} par {}", id, user.getEmail());
        return CaptureResponse.from(saved);
    }

    @Transactional
    public void deletePhoto(Long id, User user) {
        Capture capture = findAndCheckOwner(id, user);
        capture.setPhotoUrl(null);
        captureRepository.save(capture);
        log.info("Photo supprimée pour capture {} par {}", id, user.getEmail());
    }

    // ── Helpers privés ────────────────────────────────────────────────────

    private Capture findAndCheckOwner(Long id, User user) {
        Capture capture = captureRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Capture introuvable", HttpStatus.NOT_FOUND));
        if (!capture.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Accès refusé à cette capture", HttpStatus.FORBIDDEN);
        }
        return capture;
    }

    private Species resolveSpecies(CaptureRequest request) {
        if (request.getSpeciesId() == null) return null;
        return speciesRepository.findById(request.getSpeciesId())
                .orElseThrow(() -> new BusinessException(
                        "Espèce du catalogue introuvable : id=" + request.getSpeciesId(),
                        HttpStatus.BAD_REQUEST));
    }

    private String resolveSpeciesName(CaptureRequest request, Species species) {
        if (request.getSpeciesName() != null && !request.getSpeciesName().isBlank()) {
            return request.getSpeciesName().trim();
        }
        if (species != null) {
            return species.getCommonName();
        }
        throw new BusinessException(
                "Veuillez renseigner le nom de l'espèce ou choisir une espèce du catalogue.",
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Upload vers Cloudinary si les credentials sont configurés.
     * Sinon génère un nom de fichier unique (placeholder pour le dev local).
     */
    private String uploadToCloudinary(MultipartFile file) {
        if (cloudinaryApiKey == null || cloudinaryApiKey.isBlank()) {
            // Mode dev sans Cloudinary : retourne un nom fictif
            String ext = getExtension(file.getOriginalFilename());
            String fakeName = "local_" + UUID.randomUUID() + ext;
            log.warn("Cloudinary non configuré — photo non persistée : {}", fakeName);
            return fakeName;
        }

        try {
            // Cloudinary upload via leur REST API (sans SDK pour éviter une dépendance)
            String url = "https://api.cloudinary.com/v1_1/" + cloudinaryCloudName + "/image/upload";
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String boundary = UUID.randomUUID().toString();

            // Construire le multipart body manuellement
            byte[] fileBytes = file.getBytes();
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String signature = sha1Hex("timestamp=" + timestamp + cloudinaryApiSecret);

            String bodyStart = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"photo\"\r\n" +
                    "Content-Type: " + file.getContentType() + "\r\n\r\n";
            String bodyMiddle = "\r\n--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"timestamp\"\r\n\r\n" + timestamp +
                    "\r\n--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"api_key\"\r\n\r\n" + cloudinaryApiKey +
                    "\r\n--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"signature\"\r\n\r\n" + signature +
                    "\r\n--" + boundary + "--\r\n";

            byte[] bodyStartBytes  = bodyStart.getBytes();
            byte[] bodyMiddleBytes = bodyMiddle.getBytes();
            byte[] fullBody = new byte[bodyStartBytes.length + fileBytes.length + bodyMiddleBytes.length];
            System.arraycopy(bodyStartBytes, 0, fullBody, 0, bodyStartBytes.length);
            System.arraycopy(fileBytes, 0, fullBody, bodyStartBytes.length, fileBytes.length);
            System.arraycopy(bodyMiddleBytes, 0, fullBody, bodyStartBytes.length + fileBytes.length, bodyMiddleBytes.length);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(fullBody))
                    .build();

            java.net.http.HttpResponse<String> response =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            return om.readTree(response.body()).path("secure_url").asText();

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erreur upload Cloudinary", e);
            throw new BusinessException("Impossible d'uploader la photo", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }

    private String sha1Hex(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 unavailable", e);
        }
    }
}
