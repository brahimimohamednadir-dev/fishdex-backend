package com.fishdex.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service d'envoi d'emails — fault-tolerant.
 * Si le serveur SMTP n'est pas configuré, les erreurs sont simplement loguées.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@fishdex.fr}")
    private String fromAddress;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    // ── Email verification ────────────────────────────────────────────────

    @Async
    public void sendVerificationEmail(String to, String username, String token) {
        String link = frontendUrl + "/auth/verify-email?token=" + token;
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #1565C0;">🎣 Bienvenue sur FishDex, %s !</h2>
                    <p>Merci de vous être inscrit. Veuillez confirmer votre adresse email en cliquant sur le bouton ci-dessous.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background: #1565C0; color: white; padding: 14px 28px;
                           text-decoration: none; border-radius: 6px; font-size: 16px;">
                            Confirmer mon email
                        </a>
                    </div>
                    <p style="color: #666; font-size: 13px;">Ce lien expire dans 24 heures. Si vous n'avez pas créé de compte, ignorez cet email.</p>
                </div>
                """.formatted(username, link);

        sendHtmlEmail(to, "Confirmez votre adresse email — FishDex", html);
    }

    // ── Password reset ────────────────────────────────────────────────────

    @Async
    public void sendPasswordResetEmail(String to, String username, String token) {
        String link = frontendUrl + "/auth/reset-password?token=" + token;
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #1565C0;">🔐 Réinitialisation de votre mot de passe</h2>
                    <p>Bonjour %s,</p>
                    <p>Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le bouton ci-dessous.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background: #C62828; color: white; padding: 14px 28px;
                           text-decoration: none; border-radius: 6px; font-size: 16px;">
                            Réinitialiser mon mot de passe
                        </a>
                    </div>
                    <p style="color: #666; font-size: 13px;">Ce lien expire dans 1 heure. Si vous n'avez pas fait cette demande, ignorez cet email.</p>
                </div>
                """.formatted(username, link);

        sendHtmlEmail(to, "Réinitialisation de mot de passe — FishDex", html);
    }

    // ── Security alert ────────────────────────────────────────────────────

    @Async
    public void sendPasswordChangedAlert(String to, String username) {
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #E65100;">⚠️ Votre mot de passe a été modifié</h2>
                    <p>Bonjour %s,</p>
                    <p>Votre mot de passe FishDex vient d'être modifié avec succès.</p>
                    <p>Si vous n'êtes pas à l'origine de cette modification, contactez-nous immédiatement.</p>
                    <p style="color: #666; font-size: 13px;">Toutes vos sessions actives ont été révoquées.</p>
                </div>
                """.formatted(username);

        sendHtmlEmail(to, "Votre mot de passe a été modifié — FishDex", html);
    }

    @Async
    public void sendAccountLockedAlert(String to, String username, int minutesLocked) {
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #C62828;">🔒 Compte temporairement bloqué</h2>
                    <p>Bonjour %s,</p>
                    <p>Plusieurs tentatives de connexion échouées ont été détectées sur votre compte.</p>
                    <p>Par mesure de sécurité, votre compte est bloqué pendant <strong>%d minutes</strong>.</p>
                    <p style="color: #666; font-size: 13px;">Si vous n'êtes pas à l'origine de ces tentatives, pensez à changer votre mot de passe.</p>
                </div>
                """.formatted(username, minutesLocked);

        sendHtmlEmail(to, "Compte temporairement bloqué — FishDex", html);
    }

    // ── Core send ─────────────────────────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.debug("Email envoyé à {} : {}", to, subject);
        } catch (MailException | MessagingException e) {
            // Fault-tolerant : ne bloque jamais le flux principal
            log.warn("Impossible d'envoyer l'email à {} ({}): {}", to, subject, e.getMessage());
        }
    }
}
