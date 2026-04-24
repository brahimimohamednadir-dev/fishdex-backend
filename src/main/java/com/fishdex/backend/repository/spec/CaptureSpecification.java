package com.fishdex.backend.repository.spec;

import com.fishdex.backend.entity.Capture;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Specifications JPA pour les filtres dynamiques sur les captures.
 */
public final class CaptureSpecification {

    private CaptureSpecification() {}

    public static Specification<Capture> belongsToUser(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Capture> hasSpecies(Long speciesId) {
        return (root, query, cb) ->
                cb.equal(root.get("species").get("id"), speciesId);
    }

    public static Specification<Capture> caughtAfter(LocalDateTime from) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("caughtAt"), from);
    }

    public static Specification<Capture> caughtBefore(LocalDateTime to) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("caughtAt"), to);
    }
}
