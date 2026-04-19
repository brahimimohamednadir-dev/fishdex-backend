package com.fishdex.backend.config;

import com.fishdex.backend.entity.Species;
import com.fishdex.backend.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SpeciesRepository speciesRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (speciesRepository.count() > 0) return;

        speciesRepository.saveAll(List.of(
            Species.builder().commonName("Brochet").latinName("Esox lucius")
                .description("Grand prédateur d'eau douce, reconnaissable à son museau allongé.")
                .minWeightKg(0.5).maxWeightKg(20.0).habitat("Eau douce").build(),
            Species.builder().commonName("Carpe commune").latinName("Cyprinus carpio")
                .description("Poisson robuste très apprécié des carpistes. Peut vivre plus de 40 ans.")
                .minWeightKg(1.0).maxWeightKg(40.0).habitat("Eau douce").build(),
            Species.builder().commonName("Sandre").latinName("Sander lucioperca")
                .description("Prédateur nocturne aux yeux caractéristiques, chair délicate.")
                .minWeightKg(0.3).maxWeightKg(12.0).habitat("Eau douce").build(),
            Species.builder().commonName("Perche").latinName("Perca fluviatilis")
                .description("Poisson rayé emblématique, très combatif au gramme près.")
                .minWeightKg(0.05).maxWeightKg(3.0).habitat("Eau douce").build(),
            Species.builder().commonName("Truite fario").latinName("Salmo trutta fario")
                .description("Poisson de rivière aux taches rouges, symbole de la pêche à la mouche.")
                .minWeightKg(0.1).maxWeightKg(5.0).habitat("Eau douce - rivière froide").build(),
            Species.builder().commonName("Truite arc-en-ciel").latinName("Oncorhynchus mykiss")
                .description("Originaire d'Amérique du Nord, introduite et élevée en France.")
                .minWeightKg(0.1).maxWeightKg(8.0).habitat("Eau douce").build(),
            Species.builder().commonName("Silure glane").latinName("Silurus glanis")
                .description("Le plus grand poisson d'eau douce d'Europe. Peut dépasser 2 mètres.")
                .minWeightKg(5.0).maxWeightKg(150.0).habitat("Eau douce - grands fleuves").build(),
            Species.builder().commonName("Barbeau fluviatile").latinName("Barbus barbus")
                .description("Poisson de fond des eaux courantes, combatif et musclé.")
                .minWeightKg(0.2).maxWeightKg(8.0).habitat("Eau douce - rivière").build(),
            Species.builder().commonName("Ablette").latinName("Alburnus alburnus")
                .description("Petit poisson argenté vivant en bancs, excellent poisson d'appât.")
                .minWeightKg(0.01).maxWeightKg(0.1).habitat("Eau douce").build(),
            Species.builder().commonName("Gardon").latinName("Rutilus rutilus")
                .description("Poisson très commun aux nageoires rouges, facile à pêcher.")
                .minWeightKg(0.05).maxWeightKg(0.8).habitat("Eau douce").build(),
            Species.builder().commonName("Brème commune").latinName("Abramis brama")
                .description("Poisson plat argenté des eaux calmes, pêché au coup.")
                .minWeightKg(0.1).maxWeightKg(5.0).habitat("Eau douce - étang et lac").build(),
            Species.builder().commonName("Tanche").latinName("Tinca tinca")
                .description("Poisson trapu aux teintes dorées-verdâtres, discret et résistant.")
                .minWeightKg(0.1).maxWeightKg(3.0).habitat("Eau douce - étang").build(),
            Species.builder().commonName("Rotengle").latinName("Scardinius erythrophthalmus")
                .description("Proche du gardon, avec des nageoires rouge vif et des yeux dorés.")
                .minWeightKg(0.05).maxWeightKg(1.0).habitat("Eau douce").build(),
            Species.builder().commonName("Carpe miroir").latinName("Cyprinus carpio specularis")
                .description("Variante de la carpe commune à écailles irrégulières et peu nombreuses.")
                .minWeightKg(1.0).maxWeightKg(40.0).habitat("Eau douce").build(),
            Species.builder().commonName("Black-bass").latinName("Micropterus salmoides")
                .description("Originaire d'Amérique du Nord, très apprécié en pêche aux leurres.")
                .minWeightKg(0.1).maxWeightKg(4.0).habitat("Eau douce").build(),
            Species.builder().commonName("Chevaine").latinName("Squalius cephalus")
                .description("Poisson de surface omnivore, opportuniste et curieux.")
                .minWeightKg(0.1).maxWeightKg(4.0).habitat("Eau douce - rivière").build(),
            Species.builder().commonName("Anguille européenne").latinName("Anguilla anguilla")
                .description("Poisson migrateur serpentiforme, espèce menacée et protégée.")
                .minWeightKg(0.05).maxWeightKg(3.0).habitat("Eau douce et mer").build(),
            Species.builder().commonName("Omble chevalier").latinName("Salvelinus alpinus")
                .description("Salmonidé des lacs alpins et subalpins, chair savoureuse.")
                .minWeightKg(0.1).maxWeightKg(5.0).habitat("Eau douce - lac d'altitude").build(),
            Species.builder().commonName("Blageon").latinName("Telestes souffia")
                .description("Petit cyprinidé des eaux vives du Rhône et du bassin méditérranéen.")
                .minWeightKg(0.02).maxWeightKg(0.15).habitat("Eau douce - rivière").build(),
            Species.builder().commonName("Vandoise").latinName("Leuciscus leuciscus")
                .description("Poisson élancé des eaux courantes claires et bien oxygénées.")
                .minWeightKg(0.05).maxWeightKg(0.5).habitat("Eau douce - rivière").build()
        ));
    }
}
