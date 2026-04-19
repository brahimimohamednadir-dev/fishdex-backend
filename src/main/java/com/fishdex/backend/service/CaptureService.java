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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CaptureService {

    private static final int FREEMIUM_CAPTURE_LIMIT = 50;

    private final CaptureRepository captureRepository;
    private final UserRepository userRepository;
    private final SpeciesRepository speciesRepository;
    private final CloudinaryService cloudinaryService;
    private final BadgeService badgeService;

    @Transactional
    public CaptureResponse createCapture(CaptureRequest request, User user) {
        if (!user.getIsPremium() && captureRepository.countByUserId(user.getId()) >= FREEMIUM_CAPTURE_LIMIT) {
            throw new BusinessException(
                    "Limite de " + FREEMIUM_CAPTURE_LIMIT + " captures atteinte. Passez à Premium pour continuer.",
                    HttpStatus.FORBIDDEN
            );
        }

        Species species = resolveSpecies(request.getSpeciesId());

        Capture capture = Capture.builder()
                .user(user)
                .speciesName(request.getSpeciesName().trim())
                .species(species)
                .weight(request.getWeight())
                .length(request.getLength())
                .photoUrl(request.getPhotoUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .note(request.getNote())
                .caughtAt(request.getCaughtAt())
                .build();

        Capture saved = captureRepository.save(capture);

        user.setCaptureCount(user.getCaptureCount() + 1);
        userRepository.save(user);

        badgeService.checkAndAwardBadges(user);

        return CaptureResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<CaptureResponse> getMyCaptures(User user, Pageable pageable) {
        return captureRepository
                .findByUserIdOrderByCaughtAtDesc(user.getId(), pageable)
                .map(CaptureResponse::from);
    }

    @Transactional(readOnly = true)
    public CaptureResponse getCaptureById(Long id, User user) {
        Capture capture = findAndCheckOwner(id, user);
        return CaptureResponse.from(capture);
    }

    @Transactional
    public CaptureResponse updateCapture(Long id, CaptureRequest request, User user) {
        Capture capture = findAndCheckOwner(id, user);

        Species species = resolveSpecies(request.getSpeciesId());

        capture.setSpeciesName(request.getSpeciesName().trim());
        capture.setSpecies(species);
        capture.setWeight(request.getWeight());
        capture.setLength(request.getLength());
        capture.setPhotoUrl(request.getPhotoUrl());
        capture.setLatitude(request.getLatitude());
        capture.setLongitude(request.getLongitude());
        capture.setNote(request.getNote());
        capture.setCaughtAt(request.getCaughtAt());

        return CaptureResponse.from(captureRepository.save(capture));
    }

    @Transactional
    public void deleteCapture(Long id, User user) {
        findAndCheckOwner(id, user);
        captureRepository.deleteById(id);

        user.setCaptureCount(Math.max(0, user.getCaptureCount() - 1));
        userRepository.save(user);
    }

    @Transactional
    public CaptureResponse addPhoto(Long captureId, MultipartFile photo, User user) {
        Capture capture = findAndCheckOwner(captureId, user);
        try {
            String url = cloudinaryService.uploadPhoto(photo);
            capture.setPhotoUrl(url);
            return CaptureResponse.from(captureRepository.save(capture));
        } catch (IOException e) {
            throw new BusinessException("Erreur lors de l'upload de la photo : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void deletePhoto(Long captureId, User user) {
        Capture capture = findAndCheckOwner(captureId, user);
        String photoUrl = capture.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isBlank()) {
            try {
                cloudinaryService.deletePhoto(photoUrl);
            } catch (IOException e) {
                throw new BusinessException("Erreur lors de la suppression de la photo : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        capture.setPhotoUrl(null);
        captureRepository.save(capture);
    }

    private Capture findAndCheckOwner(Long id, User user) {
        Capture capture = captureRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Capture introuvable", HttpStatus.NOT_FOUND));

        if (!capture.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Accès refusé à cette capture", HttpStatus.FORBIDDEN);
        }

        return capture;
    }

    private Species resolveSpecies(Long speciesId) {
        if (speciesId == null) {
            return null;
        }
        return speciesRepository.findById(speciesId)
                .orElseThrow(() -> new BusinessException("Espèce introuvable avec l'id " + speciesId, HttpStatus.NOT_FOUND));
    }
}
