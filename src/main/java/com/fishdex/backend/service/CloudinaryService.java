package com.fishdex.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fishdex.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final long   MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final Map<String, String> EXT_MAP = Map.of(
            "image/jpeg", ".jpg",
            "image/png",  ".png",
            "image/webp", ".webp"
    );

    private final Cloudinary cloudinary;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${app.local-upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ── Upload ────────────────────────────────────────────────────────────────

    public String uploadPhoto(MultipartFile file) throws IOException {
        validateFile(file);
        if (isCloudinaryConfigured()) {
            return uploadToCloudinary(file);
        }
        return saveLocally(file);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deletePhoto(String photoUrl) throws IOException {
        if (photoUrl == null || photoUrl.isBlank()) return;

        if (isCloudinaryConfigured() && photoUrl.contains("cloudinary.com")) {
            String publicId = extractPublicId(photoUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } else if (photoUrl.startsWith(baseUrl)) {
            // Local file — extract path and delete
            String relativePath = photoUrl.substring(baseUrl.length());
            if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
            Path filePath = Paths.get(relativePath);
            Files.deleteIfExists(filePath);
        }
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private boolean isCloudinaryConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String uploadToCloudinary(MultipartFile file) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "fishdex/captures", "resource_type", "image")
        );
        return (String) result.get("secure_url");
    }

    private String saveLocally(MultipartFile file) throws IOException {
        // Resolve to absolute path (relative paths via transferTo() go to Tomcat's work dir)
        Path dir = Paths.get(uploadDir).isAbsolute()
                ? Paths.get(uploadDir, "captures")
                : Paths.get(System.getProperty("user.dir"), uploadDir, "captures");
        Files.createDirectories(dir);

        String ext = EXT_MAP.getOrDefault(file.getContentType(), ".jpg");
        String filename = UUID.randomUUID() + ext;
        Path dest = dir.resolve(filename).toAbsolutePath().normalize();

        Files.write(dest, file.getBytes()); // getBytes() évite le problème de résolution de transferTo()
        log.info("Photo sauvegardée localement : {}", dest);

        return baseUrl + "/uploads/captures/" + filename;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Le fichier est vide", HttpStatus.BAD_REQUEST);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(
                    "Type de fichier non supporté. Formats acceptés : JPEG, PNG, WEBP",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException(
                    "Le fichier dépasse 5 Mo", HttpStatus.BAD_REQUEST
            );
        }
    }

    private String extractPublicId(String photoUrl) {
        int uploadIndex = photoUrl.indexOf("/upload/");
        if (uploadIndex == -1) return photoUrl;
        String after = photoUrl.substring(uploadIndex + "/upload/".length());
        if (after.startsWith("v") && after.indexOf('/') > 0) {
            String ver = after.substring(1, after.indexOf('/'));
            if (ver.matches("\\d+")) after = after.substring(after.indexOf('/') + 1);
        }
        int dot = after.lastIndexOf('.');
        return dot != -1 ? after.substring(0, dot) : after;
    }
}
