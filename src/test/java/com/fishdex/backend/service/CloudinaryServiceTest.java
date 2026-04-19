package com.fishdex.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.fishdex.backend.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CloudinaryServiceTest {

    private CloudinaryService cloudinaryService;
    private Cloudinary cloudinary;
    private Uploader uploader;

    @BeforeEach
    void setUp() {
        cloudinary = Mockito.mock(Cloudinary.class);
        uploader = Mockito.mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        cloudinaryService = new CloudinaryService(cloudinary);
    }

    @Test
    void uploadPhotoSuccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "photo", "brochet.jpg", "image/jpeg", new byte[100]
        );
        when(uploader.upload(any(), any())).thenReturn(
                Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/v123/fishdex/captures/brochet.jpg")
        );

        String url = cloudinaryService.uploadPhoto(file);

        assertThat(url).contains("cloudinary.com");
    }

    @Test
    void uploadPhotoInvalidFormat_throwsBusinessException() {
        MockMultipartFile file = new MockMultipartFile(
                "photo", "doc.pdf", "application/pdf", new byte[100]
        );

        assertThatThrownBy(() -> cloudinaryService.uploadPhoto(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Type de fichier non supporté");
    }

    @Test
    void uploadPhotoTooLarge_throwsBusinessException() {
        byte[] bigFile = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
                "photo", "gros.jpg", "image/jpeg", bigFile
        );

        assertThatThrownBy(() -> cloudinaryService.uploadPhoto(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("5MB");
    }

    @Test
    void deletePhotoSuccess() throws IOException {
        when(uploader.destroy(any(), any())).thenReturn(Map.of("result", "ok"));

        String photoUrl = "https://res.cloudinary.com/demo/image/upload/v123/fishdex/captures/brochet.jpg";
        cloudinaryService.deletePhoto(photoUrl);

        Mockito.verify(uploader).destroy("fishdex/captures/brochet", ObjectUtils.emptyMap());
    }
}
