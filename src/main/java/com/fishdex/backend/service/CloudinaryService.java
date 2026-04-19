package com.fishdex.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fishdex.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp"
    );

    private final Cloudinary cloudinary;

    public String uploadPhoto(MultipartFile file) throws IOException {
        validateFile(file);

        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "fishdex/captures",
                        "resource_type", "image"
                )
        );
        return (String) result.get("secure_url");
    }

    public void deletePhoto(String photoUrl) throws IOException {
        if (photoUrl == null || photoUrl.isBlank()) {
            return;
        }
        String publicId = extractPublicId(photoUrl);
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
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
                    "Le fichier dépasse la taille maximale autorisée (5MB)",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private String extractPublicId(String photoUrl) {
        // URL format: https://res.cloudinary.com/{cloud}/image/upload/v{version}/{folder}/{name}.{ext}
        // publicId = folder/name (without extension)
        int uploadIndex = photoUrl.indexOf("/upload/");
        if (uploadIndex == -1) {
            return photoUrl;
        }
        String afterUpload = photoUrl.substring(uploadIndex + "/upload/".length());
        // Remove version segment if present (v1234567890/)
        if (afterUpload.startsWith("v") && afterUpload.indexOf('/') > 0) {
            String possibleVersion = afterUpload.substring(1, afterUpload.indexOf('/'));
            if (possibleVersion.matches("\\d+")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
            }
        }
        // Remove extension
        int dotIndex = afterUpload.lastIndexOf('.');
        if (dotIndex != -1) {
            afterUpload = afterUpload.substring(0, dotIndex);
        }
        return afterUpload;
    }
}
