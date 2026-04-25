package com.fishdex.backend.service;

import com.fishdex.backend.dto.SpeciesResponse;
import com.fishdex.backend.entity.*;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpeciesService {

    private final SpeciesRepository speciesRepository;
    private final SpeciesTipRepository tipRepository;
    private final SpeciesTipUpvoteRepository upvoteRepository;
    private final UserRepository userRepository;

    // ── Liste ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<SpeciesResponse> getSpecies(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return speciesRepository
                    .searchByNameOrFamily(search.trim(), pageable)
                    .map(SpeciesResponse::from);
        }
        return speciesRepository.findAll(pageable).map(SpeciesResponse::from);
    }

    // ── Détail ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SpeciesResponse getSpeciesById(Long id) {
        return getSpeciesById(id, null);
    }

    @Transactional(readOnly = true)
    public SpeciesResponse getSpeciesById(Long id, String currentUserEmail) {
        Species species = speciesRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Espèce introuvable", HttpStatus.NOT_FOUND));

        User currentUser = currentUserEmail != null
                ? userRepository.findByEmail(currentUserEmail).orElse(null)
                : null;

        List<SpeciesTip> tips = tipRepository.findBySpeciesIdOrderByUpvoteCountDescCreatedAtDesc(id);
        List<SpeciesResponse.CommunityTipDto> tipDtos = tips.stream()
                .map(t -> {
                    boolean upvoted = currentUser != null
                            && upvoteRepository.existsByTipIdAndUserId(t.getId(), currentUser.getId());
                    return SpeciesResponse.CommunityTipDto.from(t, upvoted);
                })
                .toList();

        return SpeciesResponse.from(species, tipDtos);
    }

    // ── Tips ──────────────────────────────────────────────────────────────

    @Transactional
    public void addTip(Long speciesId, String content, String userEmail) {
        Species species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new BusinessException("Espèce introuvable", HttpStatus.NOT_FOUND));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable", HttpStatus.NOT_FOUND));

        SpeciesTip tip = SpeciesTip.builder()
                .species(species).user(user).content(content.trim()).build();
        tipRepository.save(tip);
    }

    @Transactional
    public void upvoteTip(Long tipId, String userEmail) {
        SpeciesTip tip = tipRepository.findById(tipId)
                .orElseThrow(() -> new BusinessException("Conseil introuvable", HttpStatus.NOT_FOUND));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable", HttpStatus.NOT_FOUND));

        if (upvoteRepository.existsByTipIdAndUserId(tipId, user.getId())) {
            // Toggle off
            upvoteRepository.findByTipIdAndUserId(tipId, user.getId())
                    .ifPresent(upvoteRepository::delete);
            tip.setUpvoteCount(Math.max(0, tip.getUpvoteCount() - 1));
        } else {
            upvoteRepository.save(SpeciesTipUpvote.builder().tip(tip).user(user).build());
            tip.setUpvoteCount(tip.getUpvoteCount() + 1);
        }
        tipRepository.save(tip);
    }
}
