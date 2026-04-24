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
 * Aligné sur le modèle TypeScript Species du frontend Angular.
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

            s("Brochet", "Esox lucius", "Esocidae",
              "Prédateur emblématique des eaux douces françaises. Reconnaissable à son museau allongé et ses dents acérées.",
              "https://upload.wikimedia.org/wikipedia/commons/6/6e/Esox_lucius_2009.jpg",
              0.3, 15.0, 50.0, 130.0, 50,
              "FRESHWATER", "ADVANCED",
              "Rivières et lacs à végétation", "Zones littorales avec herbiers", "0-5 m", "10-22°C"),

            s("Carpe commune", "Cyprinus carpio", "Cyprinidae",
              "Poisson robuste très prisé des carpistes. Peut dépasser 30 kg dans de bonnes conditions.",
              null,
              1.0, 35.0, 30.0, 120.0, null,
              "FRESHWATER", "INTERMEDIATE",
              "Étangs, lacs et cours d'eau lents", "Fonds vaseux, zones calmes", "0-6 m", "15-28°C"),

            s("Sandre", "Sander lucioperca", "Percidae",
              "Prédateur nocturne aux yeux caractéristiques. Excellente chair appréciée des pêcheurs au leurre.",
              null,
              0.5, 12.0, 35.0, 100.0, 40,
              "FRESHWATER", "ADVANCED",
              "Lacs, plans d'eau et grands fleuves", "Profondeurs et fonds durs", "3-15 m", "10-25°C"),

            s("Perche", "Perca fluviatilis", "Percidae",
              "Petit prédateur rayé très commun. Apprécie les eaux fraîches et bien oxygénées.",
              null,
              0.1, 3.0, 15.0, 50.0, null,
              "FRESHWATER", "BEGINNER",
              "Lacs, rivières et étangs", "Zones ombragées et herbiers", "1-8 m", "8-23°C"),

            s("Silure glane", "Silurus glanis", "Siluridae",
              "Le plus grand poisson d'eau douce d'Europe. Peut dépasser 2 m et 100 kg en France.",
              null,
              5.0, 100.0, 60.0, 250.0, null,
              "FRESHWATER", "EXPERT",
              "Grands fleuves et plans d'eau profonds", "Profondeurs, anfractuosités", "5-30 m", "18-28°C"),

            s("Truite fario", "Salmo trutta fario", "Salmonidae",
              "Salmonidé emblématique des rivières de première catégorie. Très sensible à la qualité de l'eau.",
              null,
              0.1, 5.0, 15.0, 80.0, 23,
              "FRESHWATER", "EXPERT",
              "Rivières froides et rapides de montagne", "Courants rapides et fonds de gravier", "0-3 m", "5-16°C"),

            s("Barbeau fluviatile", "Barbus barbus", "Cyprinidae",
              "Poisson de fond des rivières courantes. Reconnaissable à ses quatre barbillons.",
              null,
              0.3, 8.0, 25.0, 90.0, 30,
              "FRESHWATER", "INTERMEDIATE",
              "Rivières à courant moyen", "Fonds de gravier et galets", "0-4 m", "12-22°C"),

            s("Black-bass", "Micropterus salmoides", "Centrarchidae",
              "Prédateur d'origine nord-américaine, populaire pour la pêche aux leurres de surface.",
              null,
              0.2, 5.0, 25.0, 60.0, null,
              "FRESHWATER", "INTERMEDIATE",
              "Étangs et lacs chauds avec végétation", "Herbiers et bois immergés", "0-6 m", "18-28°C"),

            s("Chevaine", "Squalius cephalus", "Cyprinidae",
              "Cyprinidé opportuniste à grosse tête. Présent dans presque tous les cours d'eau français.",
              null,
              0.1, 4.0, 20.0, 60.0, null,
              "FRESHWATER", "BEGINNER",
              "Rivières et ruisseaux variés", "Surface et mi-eau, zones calmes", "0-3 m", "10-24°C"),

            s("Tanche", "Tinca tinca", "Cyprinidae",
              "Poisson lent et massif des étangs vaseux. Sa peau est couverte d'un mucus caractéristique.",
              null,
              0.2, 4.0, 20.0, 60.0, null,
              "FRESHWATER", "INTERMEDIATE",
              "Étangs et marais à fond vaseux", "Vase et végétation dense", "0-4 m", "15-28°C"),

            s("Brème commune", "Abramis brama", "Cyprinidae",
              "Grand cyprinidé aplati latéralement. Vit en bancs dans les plans d'eau calmes.",
              null,
              0.3, 5.0, 20.0, 75.0, null,
              "FRESHWATER", "BEGINNER",
              "Lacs, canaux et cours d'eau lents", "Fonds vaseux en eau calme", "1-8 m", "12-24°C"),

            s("Rotengle", "Scardinius erythrophthalmus", "Cyprinidae",
              "Cousin du gardon, reconnaissable à ses nageoires rouges vif et ses yeux orange.",
              null,
              0.05, 1.5, 12.0, 35.0, null,
              "FRESHWATER", "BEGINNER",
              "Étangs et plans d'eau calmes", "Surface et herbiers", "0-2 m", "16-26°C"),

            s("Ablette", "Alburnus alburnus", "Cyprinidae",
              "Petit poisson argenté vivant en bancs en surface. Indicateur d'une bonne qualité des eaux.",
              null,
              0.01, 0.1, 8.0, 20.0, null,
              "FRESHWATER", "BEGINNER",
              "Rivières et lacs à eaux claires", "Surface libre, eaux courantes", "0-1 m", "12-24°C"),

            s("Gardon", "Rutilus rutilus", "Cyprinidae",
              "L'un des poissons les plus communs de France. Yeux rouges caractéristiques, très grégaire.",
              null,
              0.05, 0.5, 10.0, 35.0, null,
              "FRESHWATER", "BEGINNER",
              "Lacs, rivières et canaux", "Zones calmes et herbiers", "0-5 m", "10-24°C"),

            s("Ide mélanote", "Leuciscus idus", "Cyprinidae",
              "Grand cyprinidé au corps fuselé, surtout présent dans les grands fleuves du Nord-Est.",
              null,
              0.3, 4.0, 25.0, 60.0, null,
              "FRESHWATER", "INTERMEDIATE",
              "Grands fleuves et rivières larges", "Mi-eau et surface", "0-5 m", "10-22°C"),

            s("Goujon", "Gobio gobio", "Cyprinidae",
              "Petit poisson de fond à deux barbillons. Indicateur d'eaux claires et bien oxygénées.",
              null,
              0.01, 0.15, 6.0, 18.0, null,
              "FRESHWATER", "BEGINNER",
              "Rivières et ruisseaux à courant", "Fonds de gravier et sable", "0-2 m", "8-22°C"),

            s("Vairon", "Phoxinus phoxinus", "Cyprinidae",
              "Minuscule cyprinidé des torrents de montagne. Vit en bancs serrés dans les eaux fraîches.",
              null,
              0.005, 0.05, 4.0, 12.0, null,
              "FRESHWATER", "BEGINNER",
              "Torrents et ruisseaux de montagne", "Courants rapides, galets", "0-1 m", "4-18°C"),

            s("Lotte de rivière", "Lota lota", "Gadidae",
              "Seul gadidé d'eau douce d'Europe. Mœurs nocturnes, apprécie les eaux froides.",
              null,
              0.2, 4.0, 25.0, 70.0, null,
              "FRESHWATER", "ADVANCED",
              "Rivières et lacs froids du Nord", "Profondeurs et fonds rocheux", "5-30 m", "4-14°C"),

            s("Omble chevalier", "Salvelinus alpinus", "Salmonidae",
              "Salmonidé des lacs de montagne aux reflets multicolores. Très sensible à la température de l'eau.",
              null,
              0.1, 5.0, 15.0, 60.0, null,
              "FRESHWATER", "EXPERT",
              "Lacs de montagne profonds et froids", "Profondeurs, eaux très froides", "10-50 m", "4-12°C"),

            s("Ombre commun", "Thymallus thymallus", "Thymallidae",
              "Salmonidé reconnaissable à sa grande nageoire dorsale. Habitant des rivières rapides et fraîches.",
              null,
              0.1, 3.0, 20.0, 55.0, 30,
              "FRESHWATER", "EXPERT",
              "Rivières rapides et claires", "Courants vifs, fonds graveleux", "0-3 m", "5-16°C")

        ));

        log.info("Catalogue OK — 20 espèces insérées.");
    }

    private Species s(String commonName, String latinName, String family,
                      String description, String imageUrl,
                      Double minWeightKg, Double maxWeightKg,
                      Double minLengthCm, Double maxLengthCm, Integer minLegalSize,
                      String waterTypes, String difficulty,
                      String habitat, String habitatDetail, String preferredDepth, String temperature) {
        return Species.builder()
                .commonName(commonName)
                .latinName(latinName)
                .family(family)
                .description(description)
                .imageUrl(imageUrl)
                .minWeightKg(minWeightKg)
                .maxWeightKg(maxWeightKg)
                .minLengthCm(minLengthCm)
                .maxLengthCm(maxLengthCm)
                .minLegalSize(minLegalSize)
                .waterTypes(waterTypes)
                .difficulty(difficulty)
                .habitat(habitat)
                .habitatDetail(habitatDetail)
                .preferredDepth(preferredDepth)
                .temperature(temperature)
                .build();
    }
}
