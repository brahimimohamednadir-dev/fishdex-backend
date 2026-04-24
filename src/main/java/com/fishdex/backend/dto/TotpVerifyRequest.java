package com.fishdex.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TotpVerifyRequest {

    @NotBlank(message = "Le code TOTP est obligatoire")
    private String code;
}
