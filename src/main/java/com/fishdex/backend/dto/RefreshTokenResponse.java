package com.fishdex.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenResponse {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;
}
