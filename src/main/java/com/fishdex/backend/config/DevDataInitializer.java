package com.fishdex.backend.config;

import com.fishdex.backend.entity.*;
import com.fishdex.backend.entity.Badge.BadgeType;
import com.fishdex.backend.entity.Group.GroupVisibility;
import com.fishdex.backend.entity.Group.GroupCategory;
import com.fishdex.backend.entity.GroupMember.MemberRole;
import com.fishdex.backend.repository.*;
import com.fishdex.backend.service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seed de données de démonstration — actif uniquement si app.dev-data.enabled=true
 * Ne tourne jamais en tests (application.properties de test ne définit pas cette propriété).
 */
@Component
@ConditionalOnProperty(name = "app.dev-data.enabled", havingValue = "true")
@Order(2) // après DataInitializer (species)
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CaptureRepository captureRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final BadgeRepository badgeRepository;
    private final SpeciesRepository speciesRepository;
    private final PasswordEncoder passwordEncoder;
    private final BadgeService badgeService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Dev data déjà présent — skip.");
            return;
        }

        log.info("Insertion des données de démonstration...");

        // ── UTILISATEURS ─────────────────────────────────────────────
        User marc = userRepository.save(User.builder()
                .email("marc@fishdex.fr")
                .username("MarcCarpiste")
                .password(passwordEncoder.encode("password123"))
                .isPremium(true)
                .captureCount(0)
                .build());

        User sophie = userRepository.save(User.builder()
                .email("sophie@fishdex.fr")
                .username("SophieLeurres")
                .password(passwordEncoder.encode("password123"))
                .isPremium(false)
                .captureCount(0)
                .build());

        User pierre = userRepository.save(User.builder()
                .email("pierre@fishdex.fr")
                .username("PierreFluvial")
                .password(passwordEncoder.encode("password123"))
                .isPremium(false)
                .captureCount(0)
                .build());

        // ── ESPÈCES (récupérées depuis le catalogue seedé) ────────────
        Species brochet   = findByName("Brochet");
        Species carpe     = findByName("Carpe commune");
        Species sandre    = findByName("Sandre");
        Species perche    = findByName("Perche");
        Species silure    = findByName("Silure");
        Species truite    = findByName("Truite fario");
        Species barbeau   = findByName("Barbeau");
        Species blackbass = findByName("Black-bass");
        Species chevaine  = findByName("Chevaine");

        // ── CAPTURES DE MARC (12 captures — premium, carpiste) ───────
        List<Capture> marcCaptures = captureRepository.saveAll(List.of(
            capture(marc, "Carpe commune",        carpe,    14.2, 82.0,  "Étang de Saclay",    48.7128,  2.1600, "Bouillette fraise, fond 4m",          daysAgo(2)),
            capture(marc, "Carpe commune",        carpe,    18.6, 91.0,  "Étang de Saclay",    48.7128,  2.1600, "Record perso ! Pêche de nuit",        daysAgo(5)),
            capture(marc, "Carpe commune",        carpe,     9.1, 74.0,  "Lac de Créteil",     48.7745,  2.4570, "Zig-rig surface",                     daysAgo(8)),
            capture(marc, "Carpe miroir",         null,     22.4, 98.0,  "Lac de Créteil",     48.7745,  2.4570, "Énorme miroir, combat de 20 min",     daysAgo(12)),
            capture(marc, "Carpe commune",        carpe,     7.3, 68.0,  "Seine — Conflans",   49.0000,  2.0980, null,                                  daysAgo(18)),
            capture(marc, "Brochet",              brochet,   3.8, 78.0,  "Marne — Joinville",  48.8200,  2.4770, "Gros brochet, leurre souple",         daysAgo(22)),
            capture(marc, "Brochet",              brochet,   2.1, 65.0,  "Marne — Joinville",  48.8200,  2.4770, null,                                  daysAgo(30)),
            capture(marc, "Sandre",               sandre,    1.9, 52.0,  "Seine — Paris",      48.8534,  2.3488, "Soirée post-tempête, eau trouble",    daysAgo(35)),
            capture(marc, "Sandre",               sandre,    3.2, 61.0,  "Lac de Créteil",     48.7745,  2.4570, "Drop shot fond de gravière",          daysAgo(40)),
            capture(marc, "Silure glane",         silure,   31.0,155.0,  "Seine — Melun",      48.5400,  2.6600, "Combat 45min, relâché",               daysAgo(45)),
            capture(marc, "Perche",               perche,    0.7, 32.0,  "Étang de Saclay",    48.7128,  2.1600, null,                                  daysAgo(50)),
            capture(marc, "Brochet",              brochet,   4.6, 83.0,  "Canal de l'Ourcq",   48.8800,  2.5100, "Énorme attaque en surface, spinner",  daysAgo(55))
        ));
        marc.setCaptureCount(marcCaptures.size());
        userRepository.save(marc);

        // ── CAPTURES DE SOPHIE (7 captures — leurres) ────────────────
        List<Capture> sophieCaptures = captureRepository.saveAll(List.of(
            capture(sophie, "Brochet",    brochet,   2.4, 68.0, "Lac de Soustons",     43.7500, -1.3400, "Jerkbait, eau claire",              daysAgo(3)),
            capture(sophie, "Sandre",     sandre,    1.6, 49.0, "Gironde — Bordeaux",  44.8400, -0.5800, "Drop shot en bordure",              daysAgo(7)),
            capture(sophie, "Black-bass", blackbass, 1.1, 38.0, "Lac de Cazaux",       44.5200, -1.1600, "Top water à l'aube, explosif !",   daysAgo(14)),
            capture(sophie, "Brochet",    brochet,   5.1, 88.0, "Lac de Soustons",     43.7500, -1.3400, "Gros brochet côtier, swimbaiter",  daysAgo(20)),
            capture(sophie, "Perche",     perche,    0.4, 27.0, "Gironde — Bordeaux",  44.8400, -0.5800, null,                                daysAgo(28)),
            capture(sophie, "Sandre",     sandre,    2.8, 58.0, "Lac de Cazaux",       44.5200, -1.1600, "Soirée, grub 10cm",                daysAgo(38)),
            capture(sophie, "Brochet",    brochet,   1.9, 60.0, "Lac de Soustons",     43.7500, -1.3400, null,                                daysAgo(60))
        ));
        sophie.setCaptureCount(sophieCaptures.size());
        userRepository.save(sophie);

        // ── CAPTURES DE PIERRE (5 captures — pêche rivière) ──────────
        List<Capture> pierreCaptures = captureRepository.saveAll(List.of(
            capture(pierre, "Truite fario",       truite,  0.6, 31.0, "Ain — Ambérieu",    45.9600,  5.3500, "Sèche Adams taille 14, parfait !", daysAgo(4)),
            capture(pierre, "Truite fario",       truite,  1.2, 42.0, "Ain — Ambérieu",    45.9600,  5.3500, "Nymphe tungstène",                 daysAgo(9)),
            capture(pierre, "Barbeau fluviatile", barbeau, 2.1, 55.0, "Rhône — Lyon",      45.7600,  4.8300, "Pelote de vers, fond courant",     daysAgo(16)),
            capture(pierre, "Chevaine",           chevaine,0.8, 36.0, "Rhône — Lyon",      45.7600,  4.8300, "Surface, mouche sèche",            daysAgo(24)),
            capture(pierre, "Truite fario",       truite,  0.9, 37.0, "Ain — Pont d'Ain", 46.0500,  5.3300, null,                               daysAgo(33))
        ));
        pierre.setCaptureCount(pierreCaptures.size());
        userRepository.save(pierre);

        // ── GROUPES ───────────────────────────────────────────────────
        Group carpistes = groupRepository.save(Group.builder()
                .name("Carpistes Île-de-France")
                .description("Le groupe des passionnés de carpe en région parisienne. Sorties collectives, bons plans étangs et conseils montages.")
                .visibility(GroupVisibility.PUBLIC)
                .category(GroupCategory.CLUB)
                .creator(marc)
                .build());

        Group rhone = groupRepository.save(Group.builder()
                .name("Pêcheurs du Rhône")
                .description("Association de pêche sur le bassin du Rhône. Truites, barbeaux, chevaines — la pêche en rivière dans toute sa splendeur.")
                .visibility(GroupVisibility.PUBLIC)
                .category(GroupCategory.ASSOCIATION)
                .creator(pierre)
                .build());

        // ── MEMBRES ───────────────────────────────────────────────────
        // Carpistes IDF : Marc (admin), Sophie (member)
        groupMemberRepository.save(GroupMember.builder().group(carpistes).user(marc).role(MemberRole.ADMIN).build());
        groupMemberRepository.save(GroupMember.builder().group(carpistes).user(sophie).role(MemberRole.MEMBER).build());

        // Pêcheurs du Rhône : Pierre (admin), Marc (member)
        groupMemberRepository.save(GroupMember.builder().group(rhone).user(pierre).role(MemberRole.ADMIN).build());
        groupMemberRepository.save(GroupMember.builder().group(rhone).user(marc).role(MemberRole.MEMBER).build());

        // ── BADGES ───────────────────────────────────────────────────
        badgeService.checkAndAwardBadges(marc);
        badgeService.checkAndAwardBadges(sophie);
        badgeService.checkAndAwardBadges(pierre);
        badgeService.awardFirstGroup(sophie); // sophie a rejoint Carpistes IDF
        badgeService.awardFirstGroup(marc);   // marc a rejoint Pêcheurs du Rhône

        log.info("Dev data OK — 3 users, {} captures, 2 groupes, badges distribués.",
                marcCaptures.size() + sophieCaptures.size() + pierreCaptures.size());
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Species findByName(String partialName) {
        return speciesRepository
                .searchByNameOrFamily(partialName, org.springframework.data.domain.Pageable.unpaged())
                .getContent()
                .get(0);
    }

    private Capture capture(User user, String speciesName, Species species,
                             double weight, double length, String location,
                             double lat, double lng, String note, LocalDateTime caughtAt) {
        return Capture.builder()
                .user(user)
                .speciesName(speciesName)
                .species(species)
                .weight(weight)
                .length(length)
                .latitude(lat)
                .longitude(lng)
                .note(note)
                .caughtAt(caughtAt)
                .build();
    }

    private LocalDateTime daysAgo(int days) {
        return LocalDateTime.now().minusDays(days).withHour(8).withMinute(30);
    }
}
