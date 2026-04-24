package com.fishdex.backend.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    /**
     * Au moins :
     * - 8 caractères
     * - 1 lettre majuscule
     * - 1 chiffre
     * - 1 caractère spécial (non alphanumérique)
     */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
