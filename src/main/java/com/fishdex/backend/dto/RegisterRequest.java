package com.fishdex.backend.dto;

import com.fishdex.backend.dto.validation.ValidPassword;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 150, message = "L'email ne doit pas dépasser 150 caractères")
    private String email;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @ValidPassword
    private String password;

    /** RGPD — Article 7 : consentement explicite requis */
    @NotNull(message = "L'acceptation des conditions est obligatoire")
    @AssertTrue(message = "Vous devez accepter les conditions d'utilisation et la politique de confidentialité")
    private Boolean privacyAccepted;
}
