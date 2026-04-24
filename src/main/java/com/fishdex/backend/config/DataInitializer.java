package com.fishdex.backend.config;

import com.fishdex.backend.entity.Species;
import com.fishdex.backend.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seed du catalogue des 20 espèces de poissons d'eau douce françaises.
 * Champs alignés sur le modèle frontend Angular (minWeightKg, maxWeightKg, habitat).
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final SpeciesRepository speciesRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (speciesRepository.count() > 0) {
            log.info("Catalogue espèces déjà présent — skip.");
            return;
        }

        log.info("Insertion du catalogue des espèces...");

        speciesRepository.saveAll(List.of(
            s("Brochet",             "Esox lucius",
              "Prédateur emblématique des eaux douces françaises. Reconnaissable à son museau allongé et ses dents acérées.",
              null, "https://upload.wikimedia.org/wikipedia/commons/6/6e/Esox_lucius_2009.jpg",
              0.3, 15.0, "Rivières et lacs à végétation", 50, "Esocidae"),

            s("Carpe commune",       "Cyprinus carpio",
              "Poisson robuste très prisé des carpistes. Peut dépasser 30 kg dans de bonnes conditions.",
              null, null,
              1.0, 35.0, "Étangs, lacs et cours d'eau lents", null, "Cyprinidae"),

            s("Sandre",              "Sander lucioperca",
              "Prédateur nocturne aux yeux caractéristiques. Excellente chair appréciée des pêcheurs au leurre.",
              null, null,
              0.5, 12.0, "Lacs, plans d'eau et grands fleuves", 40, "Percidae"),

            s("Perche",              "Perca fluviatilis",
              "Petit prédateur rayé très commun. Apprécie les eaux fraîches et bien oxygénées.",
              null, null,
              0.1, 3.0, "Lacs, rivières et étangs", null, "Percidae"),

            s("Silure glane",        "Silurus glanis",
              "Le plus grand poisson d'eau douce d'Europe. Peut dépasser 2 m et 100 kg en France.",
              null, null,
              5.0, 100.0, "Grands fleuves et plans d'eau profonds", null, "Siluridae"),

            s("Truite fario",        "Salmo trutta fario",
              "Salmonidé emblématique des rivières de première catégorie. Très sensible à la qualité de l'eau.",
              null, null,
              0.1, 5.0, "Rivières froides et rapides de montagne", 23, "Salmonidae"),

            s("Barbeau fluviatile",  "Barbus barbus",
              "Poisson de fond des rivières courantes. Reconnaissable à ses quatre barbillons.",
              null, null,
              0.3, 8.0, "Rivières à courant moyen", 30, "Cyprinidae"),

            s("Black-bass",          "Micropterus salmoides",
              "Prédateur d'origine nord-américaine, populaire pour la pêche aux leurres de surface.",
              null, null,
              0.2, 5.0, "Étangs et lacs chauds avec végétation", null, "Centrarchidae"),

            s("Chevaine",            "Squalius cephalus",
              "Cyprinidé opportuniste à grosse tête. Présent dans presque tous les cours d'eau français.",
              null, null,
              0.1, 4.0, "Rivières et ruisseaux variés", null, "Cyprinidae"),

            s("Tanche",              "Tinca tinca",
              "Poisson lent et massif des étangs vaseux. Sa peau est couverte d'un mucus caractéristique.",
              null, null,
              0.2, 4.0, "Étangs et marais à fond vaseux", null, "Cyprinidae"),

            s("Brème commune",       "Abramis brama",
              "Grand cyprinidé aplati latéralement. Vit en bancs dans les plans d'eau calmes.",
              null, null,
              0.3, 5.0, "Lacs, canaux et cours d'eau lents", null, "Cyprinidae"),

            s("Rotengle",            "Scardinius erythrophthalmus",
              "Cousin du gardon, reconnaissable à ses nageoires rouges vif et ses yeux orange.",
              null, null,
              0.05, 1.5, "Étangs et plans d'eau calmes", null, "Cyprinidae"),

            s("Ablette",             "Alburnus alburnus",
              "Petit poisson argenté vivant en bancs en surface. Indicateur d'une bonne qualité des eaux.",
              null, null,
              0.01, 0.1, "Rivières et lacs à eaux claires", null, "Cyprinidae"),

            s("Gardon",              "Rutilus rutilus",
              "L'un des poissons les plus communs de France. Yeux rouges caractéristiques, très grégaire.",
              null, null,
              0.05, 0.5, "Lacs, rivières et canaux", null, "Cyprinidae"),

            s("Ide mélanote",        "Leuciscus idus",
              "Grand cyprinidé au corps fuselé, surtout présent dans les grands fleuves du Nord-Est.",
              null, null,
              0.3, 4.0, "Grands fleuves et rivières larges", null, "Cyprinidae"),

            s("Goujon",              "Gobio gobio",
              "Petit poisson de fond à deux barbillons. Indicateur d'eaux claires et bien oxygénées.",
              null, null,
              0.01, 0.15, "Rivières et ruisseaux à courant", null, "Cyprinidae"),

            s("Vairon",              "Phoxinus phoxinus",
              "Minuscule cyprinidé des torrents de montagne. Vit en bancs serrés dans les eaux fraîches.",
              null, null,
              0.005, 0.05, "Torrents et ruisseaux de montagne", null, "Cyprinidae"),

            s("Lotte de rivière",    "Lota lota",
              "Seul gadidé d'eau douce d'Europe. Mœurs nocturnes, apprécie les eaux froides.",
              null, null,
              0.2, 4.0, "Rivières et lacs froids du Nord", null, "Gadidae"),

            s("Omble chevalier",     "Salvelinus alpinus",
              "Salmonidé des lacs de montagne aux reflets multicolores. Très sensible à la température de l'eau.",
              null, null,
              0.1, 5.0, "Lacs de montagne profonds et froids", null, "Salmonidae"),

            s("Ombre commun",        "Thymallus thymallus",
              "Salmonidé reconnaissable à sa grande nageoire dorsale. Habitant des rivières rapides et fraîches.",
              null, null,
              0.1, 3.0, "Rivières rapides et claires", 30, "Thymallidae")
        ));

        log.info("Catalogue OK — 20 espèces insérées.");
    }

    private Species s(String commonName, String latinName, String description, String imageUrl,
                      String imageUrlOverride, Double minWeightKg, Double maxWeightKg,
                      String habitat, Integer minLegalSize, String family) {
        return Species.builder()
                .commonName(commonName)
                .latinName(latinName)
                .description(description)
                .imageUrl(imageUrlOverride)
                .minWeightKg(minWeightKg)
                .maxWeightKg(maxWeightKg)
                .habitat(habitat)
                .minLegalSize(minLegalSize)
                .family(family)
                .build();
    }
}
