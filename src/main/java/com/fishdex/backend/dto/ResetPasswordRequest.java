package com.fishdex.backend.dto;

import com.fishdex.backend.dto.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Le token est obligatoire")
    private String token;

    /** Nommé "password" pour correspondre au frontend Angular */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @ValidPassword
    private String password;
}
