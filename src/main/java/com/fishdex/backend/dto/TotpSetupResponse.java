package com.fishdex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotpSetupResponse {

    /** Secret Base32 à afficher en texte si l'app ne peut pas scanner le QR */
    private String secret;

    /** URI otpauth:// pour générer le QR code côté frontend */
    private String qrCodeUri;

    /** 10 codes de secours à usage unique — à sauvegarder immédiatement */
    private List<String> backupCodes;
}
