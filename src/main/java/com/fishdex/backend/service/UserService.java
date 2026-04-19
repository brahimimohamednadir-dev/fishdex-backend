package com.fishdex.backend.service;

import com.fishdex.backend.dto.CaptureResponse;
import com.fishdex.backend.dto.UpdateUsernameRequest;
import com.fishdex.backend.dto.UserResponse;
import com.fishdex.backend.dto.UserStatsResponse;
import com.fishdex.backend.entity.Capture;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.GroupMemberRepository;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CaptureRepository captureRepository;
    private final GroupMemberRepository groupMemberRepository;

    public User loadUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(String email) {
        return UserResponse.from(loadUserByEmail(email));
    }

    @Transactional
    public UserResponse updateUsername(String email, UpdateUsernameRequest request) {
        User user = loadUserByEmail(email);
        String newUsername = request.getUsername().trim();

        if (!user.getUsername().equals(newUsername) && userRepository.existsByUsername(newUsername)) {
            throw new BusinessException("Ce nom d'utilisateur est déjà pris", HttpStatus.CONFLICT);
        }

        user.setUsername(newUsername);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserStatsResponse getStats(String email) {
        User user = loadUserByEmail(email);
        List<Capture> captures = captureRepository.findByUserId(user.getId());

        int totalCaptures = captures.size();

        Optional<Capture> biggestCaptureOpt = captures.stream()
                .filter(c -> c.getWeight() != null)
                .max(Comparator.comparingDouble(Capture::getWeight));
        CaptureResponse biggestCatch = biggestCaptureOpt.map(CaptureResponse::from).orElse(null);

        Double totalWeight = captures.stream()
                .filter(c -> c.getWeight() != null)
                .mapToDouble(Capture::getWeight)
                .sum();
        if (captures.stream().noneMatch(c -> c.getWeight() != null)) {
            totalWeight = null;
        }

        Map<String, Long> capturesBySpecies = captures.stream()
                .filter(c -> c.getSpeciesName() != null)
                .collect(Collectors.groupingBy(Capture::getSpeciesName, Collectors.counting()));

        // Calcul du mois le plus actif en Java (compatible H2 et MySQL)
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String mostActiveMonth = captures.stream()
                .filter(c -> c.getCaughtAt() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getCaughtAt().format(monthFormatter),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        int joinedGroupsCount = (int) groupMemberRepository.countByUserId(user.getId());

        return UserStatsResponse.builder()
                .totalCaptures(totalCaptures)
                .biggestCatch(biggestCatch)
                .totalWeight(totalWeight)
                .capturesBySpecies(capturesBySpecies)
                .mostActiveMonth(mostActiveMonth)
                .joinedGroupsCount(joinedGroupsCount)
                .build();
    }
}
