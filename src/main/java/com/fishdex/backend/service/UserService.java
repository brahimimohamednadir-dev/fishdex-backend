package com.fishdex.backend.service;

import com.fishdex.backend.dto.UpdateUsernameRequest;
import com.fishdex.backend.dto.UserResponse;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
}
