package com.fishdex.backend.util;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation TOTP (RFC 6238) depuis zéro.
 * Utilise HMAC-SHA1, pas de dépendance externe au-delà d'Apache Commons Codec pour Base32.
 */
@Component
public class TotpUtil {

    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int WINDOW = 1; // ±1 pas = tolérance de 30s
    private static final int SECRET_BYTES = 20; // 160 bits
    private static final int BACKUP_CODE_COUNT = 10;
    private static final int BACKUP_CODE_LENGTH = 8;

    private static final Base32 BASE32 = new Base32();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ── Secret generation ─────────────────────────────────────────────────

    /** Génère un secret Base32 aléatoire (20 octets = 160 bits) */
    public String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return BASE32.encodeToString(bytes).replace("=", "");
    }

    // ── TOTP computation ──────────────────────────────────────────────────

    /**
     * Génère le code TOTP pour un secret et un timestamp donnés.
     *
     * @param secret    secret Base32
     * @param timeMillis timestamp en ms (System.currentTimeMillis())
     * @return code à 6 chiffres (avec zéros en tête si nécessaire)
     */
    public String generateTotp(String secret, long timeMillis) {
        long timeStep = timeMillis / 1000 / TIME_STEP_SECONDS;
        return computeTotp(decodeBase32(secret), timeStep);
    }

    /**
     * Vérifie un code TOTP avec une fenêtre de ±WINDOW pas.
     *
     * @param secret  secret Base32
     * @param code    code soumis par l'utilisateur
     * @return true si le code est valide
     */
    public boolean verifyTotp(String secret, String code) {
        if (code == null || code.length() != CODE_DIGITS) return false;
        byte[] secretBytes = decodeBase32(secret);
        long currentStep = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
        for (int i = -WINDOW; i <= WINDOW; i++) {
            if (computeTotp(secretBytes, currentStep + i).equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String computeTotp(byte[] secretBytes, long timeStep) {
        byte[] message = ByteBuffer.allocate(8).putLong(timeStep).array();
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA1"));
            byte[] hash = mac.doFinal(message);

            // Dynamic truncation (RFC 4226 §5.4)
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % (int) Math.pow(10, CODE_DIGITS);

            return String.format("%0" + CODE_DIGITS + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Erreur TOTP : " + e.getMessage(), e);
        }
    }

    private byte[] decodeBase32(String secret) {
        // Padder à un multiple de 8
        String padded = secret.toUpperCase();
        int padding = (8 - padded.length() % 8) % 8;
        padded = padded + "=".repeat(padding);
        return BASE32.decode(padded);
    }

    // ── otpauth URI ───────────────────────────────────────────────────────

    /**
     * Génère l'URI otpauth:// pour créer le QR code dans l'app d'authentification.
     */
    public String generateTotpUri(String secret, String email, String issuer) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                encode(issuer), encode(email), secret, encode(issuer), CODE_DIGITS, TIME_STEP_SECONDS
        );
    }

    private String encode(String value) {
        return value.replace(" ", "%20").replace(":", "%3A").replace("@", "%40");
    }

    // ── Backup codes ──────────────────────────────────────────────────────

    /**
     * Génère 10 codes de secours alphanumériques à usage unique.
     * À stocker hashés en BCrypt côté serveur, à afficher en clair une seule fois.
     */
    public List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>(BACKUP_CODE_COUNT);
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sans I, O, 0, 1 (ambiguïté)
        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            StringBuilder code = new StringBuilder(BACKUP_CODE_LENGTH);
            for (int j = 0; j < BACKUP_CODE_LENGTH; j++) {
                code.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
            }
            codes.add(code.toString());
        }
        return codes;
    }
}
