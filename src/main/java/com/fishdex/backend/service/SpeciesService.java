package com.fishdex.backend.service;

import com.fishdex.backend.dto.SpeciesResponse;
import com.fishdex.backend.dto.SpeciesResponse.SpeciesPersonalStatsDto;
import com.fishdex.backend.dto.SpeciesResponse.PersonalRecordDto;
import com.fishdex.backend.dto.SpeciesResponse.SpeciesRecordDto;
import com.fishdex.backend.entity.*;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpeciesService {

    private final SpeciesRepository       speciesRepository;
    private final SpeciesTipRepository    tipRepository;
    private final SpeciesTipUpvoteRepository upvoteRepository;
    private final CaptureRepository       captureRepository;
    private final UserRepository          userRepository;

    // ── Liste ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<SpeciesResponse> getSpecies(String search, Pageable pageable, String userEmail) {
        Page<Species> page = (search != null && !search.isBlank())
                ? speciesRepository.findByCommonNameContainingIgnoreCase(search.trim(), pageable)
                : speciesRepository.findAll(pageable);

        User user = resolveUser(userEmail);
        return page.map(s -> buildResponse(s, user, false));
    }

    // ── Détail ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SpeciesResponse getSpeciesById(Long id, String userEmail) {
        Species species = speciesRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Espèce introuvable", HttpStatus.NOT_FOUND));
        User user = resolveUser(userEmail);
        return buildResponse(species, user, true);
    }

    // ── Tips ──────────────────────────────────────────────────────────────

    @Transactional
    public SpeciesResponse.CommunityTipDto addTip(Long speciesId, String content, String userEmail) {
        Species species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new BusinessException("Espèce introuvable", HttpStatus.NOT_FOUND));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable", HttpStatus.NOT_FOUND));

        if (content == null || content.isBlank() || content.length() > 500) {
            throw new BusinessException("Le conseil doit faire entre 1 et 500 caractères", HttpStatus.BAD_REQUEST);
        }

        SpeciesTip tip = tipRepository.save(SpeciesTip.builder()
                .species(species)
                .user(user)
                .content(content.trim())
                .upvotes(0)
                .build());

        log.info("Conseil ajouté pour {} par {}", species.getCommonName(), userEmail);
        return toTipDto(tip, false);
    }

    @Transactional
    public SpeciesResponse.CommunityTipDto upvoteTip(Long tipId, String userEmail) {
        SpeciesTip tip = tipRepository.findById(tipId)
                .orElseThrow(() -> new BusinessException("Conseil introuvable", HttpStatus.NOT_FOUND));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable", HttpStatus.NOT_FOUND));

        boolean alreadyUpvoted = upvoteRepository.existsByTipIdAndUserId(tipId, user.getId());
        if (alreadyUpvoted) {
            // Toggle : retirer le upvote
            upvoteRepository.findByTipIdAndUserId(tipId, user.getId())
                    .ifPresent(upvoteRepository::delete);
            tip.setUpvotes(Math.max(0, tip.getUpvotes() - 1));
            tip = tipRepository.save(tip);
            return toTipDto(tip, false);
        } else {
            upvoteRepository.save(SpeciesTipUpvote.builder().tip(tip).user(user).build());
            tip.setUpvotes(tip.getUpvotes() + 1);
            tip = tipRepository.save(tip);
            return toTipDto(tip, true);
        }
    }

    // ── Helpers privés ─────────────────────────────────────────────────────

    private SpeciesResponse buildResponse(Species species, User user, boolean withTips) {
        List<SpeciesTip> tips = withTips
                ? tipRepository.findBySpeciesIdOrderByUpvotesDescCreatedAtDesc(species.getId())
                : Collections.emptyList();

        boolean caught = false;
        SpeciesPersonalStatsDto personalStats = null;
        SpeciesRecordDto fishDexRecord = null;
        long totalCaptures = 0L;

        if (species.getId() != null) {
            totalCaptures = captureRepository.countBySpeciesId(species.getId());

            // FishDex record (communauté)
            Optional<Capture> fishDexCapture = captureRepository
                    .findFishDexRecordBySpeciesId(species.getId(), PageRequest.of(0, 1))
                    .get().findFirst();
            if (fishDexCapture.isPresent()) {
                Capture rec = fishDexCapture.get();
                fishDexRecord = SpeciesRecordDto.builder()
                        .weight(rec.getWeight() != null ? rec.getWeight() : 0)
                        .length(rec.getLength())
                        .username(rec.getUser().getUsername())
                        .date(rec.getCaughtAt() != null ? rec.getCaughtAt().toLocalDate().toString() : null)
                        .build();
            }

            if (user != null) {
                caught = captureRepository.existsByUserIdAndSpeciesId(user.getId(), species.getId());

                if (caught) {
                    long total = captureRepository.countByUserIdAndSpeciesId(user.getId(), species.getId());
                    Double avgWeight = captureRepository.findAvgWeightByUserIdAndSpeciesId(user.getId(), species.getId());
                    LocalDateTime lastCatchDt = captureRepository.findLastCatchByUserIdAndSpeciesId(user.getId(), species.getId());
                    long thisYear = captureRepository.countByUserIdAndSpeciesIdAndYear(
                            user.getId(), species.getId(), LocalDate.now().getYear());

                    PersonalRecordDto prDto = null;
                    Optional<Capture> pr = captureRepository
                            .findPersonalRecordByUserIdAndSpeciesId(user.getId(), species.getId(), PageRequest.of(0, 1))
                            .get().findFirst();
                    if (pr.isPresent()) {
                        Capture r = pr.get();
                        prDto = PersonalRecordDto.builder()
                                .weight(r.getWeight())
                                .length(r.getLength())
                                .date(r.getCaughtAt() != null ? r.getCaughtAt().toLocalDate().toString() : null)
                                .build();
                    }

                    personalStats = SpeciesPersonalStatsDto.builder()
                            .totalCatches(total)
                            .personalRecord(prDto)
                            .averageWeight(avgWeight)
                            .lastCatch(lastCatchDt != null ? lastCatchDt.toString() : null)
                            .caughtThisYear(thisYear)
                            .build();
                }
            }
        }

        // Enrichir hasUpvoted sur les tips si user connecté
        List<SpeciesTip> finalTips = tips;
        if (user != null && !tips.isEmpty()) {
            final User u = user;
            finalTips = tips.stream()
                    .peek(t -> { /* hasUpvoted géré dans toTipDto */ })
                    .collect(java.util.stream.Collectors.toList());
        }

        SpeciesResponse response = SpeciesResponse.fromWithContext(
                species, user != null ? user.getEmail() : null,
                finalTips, caught, personalStats, fishDexRecord, totalCaptures);

        // Enrichir hasUpvoted maintenant qu'on a le user
        if (user != null && !response.getCommunityTips().isEmpty()) {
            final User u = user;
            response.getCommunityTips().forEach(tipDto ->
                    tipDto.setHasUpvoted(upvoteRepository.existsByTipIdAndUserId(tipDto.getId(), u.getId())));
        }

        return response;
    }

    private User resolveUser(String email) {
        if (email == null || email.isBlank()) return null;
        return userRepository.findByEmail(email).orElse(null);
    }

    private SpeciesResponse.CommunityTipDto toTipDto(SpeciesTip tip, boolean hasUpvoted) {
        return SpeciesResponse.CommunityTipDto.builder()
                .id(tip.getId())
                .content(tip.getContent())
                .authorUsername(tip.getUser().getUsername())
                .upvotes(tip.getUpvotes())
                .hasUpvoted(hasUpvoted)
                .createdAt(tip.getCreatedAt() != null ? tip.getCreatedAt().toString() : null)
                .build();
    }
}
