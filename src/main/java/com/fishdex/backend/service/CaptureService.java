package com.fishdex.backend.service;

import com.fishdex.backend.dto.CaptureRequest;
import com.fishdex.backend.dto.CaptureResponse;
import com.fishdex.backend.entity.Capture;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CaptureService {

    private static final int FREEMIUM_CAPTURE_LIMIT = 50;

    private final CaptureRepository captureRepository;
    private final UserRepository userRepository;

    @Transactional
    public CaptureResponse createCapture(CaptureRequest request, User user) {
        if (!user.getIsPremium() && captureRepository.countByUserId(user.getId()) >= FREEMIUM_CAPTURE_LIMIT) {
            throw new BusinessException(
                    "Limite de " + FREEMIUM_CAPTURE_LIMIT + " captures atteinte. Passez à Premium pour continuer.",
                    HttpStatus.FORBIDDEN
            );
        }

        Capture capture = Capture.builder()
                .user(user)
                .speciesName(request.getSpeciesName().trim())
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

        capture.setSpeciesName(request.getSpeciesName().trim());
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

    private Capture findAndCheckOwner(Long id, User user) {
        Capture capture = captureRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Capture introuvable", HttpStatus.NOT_FOUND));

        if (!capture.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Accès refusé à cette capture", HttpStatus.FORBIDDEN);
        }

        return capture;
    }
}
