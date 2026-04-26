-- =============================================================================
--  FishDex — Seed Beta v1.0
--  Base : MySQL 8  |  Mot de passe de tous les comptes : Fishdex2026!
--  Hashé BCrypt $2a$10$... (généré hors-ligne)
--  Exécution : mysql -u root fishdex < seed_beta.sql
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE species_tip_upvotes;
TRUNCATE TABLE species_tips;
TRUNCATE TABLE comment_likes;
TRUNCATE TABLE post_reports;
TRUNCATE TABLE post_reactions;
TRUNCATE TABLE post_comments;
TRUNCATE TABLE posts;
TRUNCATE TABLE notifications;
TRUNCATE TABLE group_join_requests;
TRUNCATE TABLE group_members;
TRUNCATE TABLE fishing_groups;
TRUNCATE TABLE badges;
TRUNCATE TABLE captures;
TRUNCATE TABLE totp_secrets;
TRUNCATE TABLE pre_auth_tokens;
TRUNCATE TABLE password_reset_tokens;
TRUNCATE TABLE email_verification_tokens;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE users;
TRUNCATE TABLE species;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
--  ESPÈCES (20 espèces d'eau douce françaises)
-- =============================================================================

INSERT INTO species (id, common_name, latin_name, description, image_url, min_weight_kg, max_weight_kg, habitat, min_legal_size, family) VALUES
(1,  'Brochet',             'Esox lucius',           'Prédateur emblématique des eaux douces françaises. Reconnaissable à son museau allongé en forme de bec de canard et ses dents acérées. Chasseur en embuscade parmi les herbiers.',                                               'https://upload.wikimedia.org/wikipedia/commons/6/6e/Esox_lucius_2009.jpg', 0.3,  15.0, 'Rivières à végétation dense, lacs et étangs',       50,   'Esocidae'),
(2,  'Carpe commune',       'Cyprinus carpio',        'Poisson robuste très prisé des carpistes du monde entier. Peut atteindre des poids impressionnants dans les grands lacs français. Omnivore opportuniste se nourrissant de végétaux et d''invertébrés.',                            NULL,                                                                      1.0,  35.0, 'Étangs, lacs et cours d''eau lents et profonds',    NULL, 'Cyprinidae'),
(3,  'Sandre',              'Sander lucioperca',      'Prédateur nocturne aux grands yeux caractéristiques adaptés à la faible luminosité. Excellente chair blanche très appréciée. Pêche au leurre souple ou au vif.',                                                                  NULL,                                                                      0.5,  12.0, 'Lacs profonds, grands fleuves et retenues',         40,   'Percidae'),
(4,  'Perche',              'Perca fluviatilis',      'Petit prédateur aux flancs rayés de bandes verticales sombres. Très commun, vit en bancs dans les eaux fraîches. Cible idéale pour les débutants au leurre souple ou aux vers.',                                                  NULL,                                                                      0.05, 3.0,  'Lacs, rivières et étangs bien oxygénés',            NULL, 'Percidae'),
(5,  'Silure glane',        'Silurus glanis',         'Le plus grand poisson d''eau douce d''Europe. Peut dépasser 2,5 m et 130 kg. Prédateur nocturne opportuniste, parfois observé en surface. Pêche au vif ou à la bouillette.',                                                      NULL,                                                                      10.0, 100.0,'Grands fleuves, retenues hydroélectriques',         NULL, 'Siluridae'),
(6,  'Truite fario',        'Salmo trutta',           'Salmonidé mythique des rivières de montagne. Apprécie les eaux froides, rapides et très oxygénées. Pêche à la mouche sèche ou aux nymphes. Très sensible à la qualité de l''eau.',                                               NULL,                                                                      0.1,  8.0,  'Rivières et torrents de montagne, eaux froides',    23,   'Salmonidae'),
(7,  'Truite arc-en-ciel',  'Oncorhynchus mykiss',   'Introduite d''Amérique du Nord, la truite arc-en-ciel est la plus pêchée en France en pisciculture et plans d''eau. Reconnaissable à sa bande irisée sur le flanc.',                                                              NULL,                                                                      0.2,  10.0, 'Plans d''eau de pêche, rivières aménagées',         23,   'Salmonidae'),
(8,  'Tanche',              'Tinca tinca',            'Poisson aux écailles dorées couvertes de mucus. Vit dans la vase des étangs envasés. Très résistante au manque d''oxygène. Pêche classique au coup avec des vers.',                                                               NULL,                                                                      0.2,  4.0,  'Étangs envasés et eaux lentes herbeuses',           NULL, 'Cyprinidae'),
(9,  'Barbeau fluviatile',  'Barbus barbus',          'Poisson rhéophile typique des rivières à courant vif. Museau proéminent avec 4 barbillons. Pêche au fond dans les radiers et courants.',                                                                                          NULL,                                                                      0.5,  8.0,  'Rivières à courant fort et fond graveleux',         30,   'Cyprinidae'),
(10, 'Black-bass',          'Micropterus salmoides',  'Introduit d''Amérique du Nord, le black-bass est devenu une cible majeure des pêcheurs au leurre. Combatif et agressif, il attaque les leurres de surface avec violence.',                                                         NULL,                                                                      0.3,  5.0,  'Lacs chauds, retenues, eaux calmes et herbues',     NULL, 'Centrarchidae'),
(11, 'Chevaine',            'Squalius cephalus',      'Cyprinidé opportuniste aux écailles argentées bordées de noir. Vit en bancs dans les rivières. Pêche à la mouche, au pain ou aux insectes en surface.',                                                                           NULL,                                                                      0.1,  4.0,  'Rivières à courant modéré, cours d''eau variés',    NULL, 'Cyprinidae'),
(12, 'Gardon',              'Rutilus rutilus',         'Le poisson le plus abondant des eaux douces françaises. Pêche au coup classique avec amorçage. Vit en grands bancs dans les étangs et rivières calmes.',                                                                         NULL,                                                                      0.02, 0.8,  'Étangs, lacs et rivières calmes',                   NULL, 'Cyprinidae'),
(13, 'Brème commune',       'Abramis brama',          'Grand cyprinidé aplati aux flancs argentés. Vit en bancs dans les eaux profondes et calmes. Pêche au coup avec longue canne et amorçage généreux.',                                                                               NULL,                                                                      0.2,  6.0,  'Grands lacs, rivières lentes et profondes',         NULL, 'Cyprinidae'),
(14, 'Carpe miroir',        'Cyprinus carpio (miroir)','Variante écaillée de façon irrégulière de la carpe commune. Très convoitée par les carpistes pour ses formes atypiques et ses poids records. Même comportement que la carpe commune.',                                           NULL,                                                                      2.0,  40.0, 'Étangs, lacs profonds et grands cours d''eau',      NULL, 'Cyprinidae'),
(15, 'Ombre commun',        'Thymallus thymallus',    'Salmonidé reconnaissable à sa grande nageoire dorsale. Vit dans les rivières froides et claires de montagne. Pêche à la mouche sèche. Odeur de thym caractéristique.',                                                          NULL,                                                                      0.1,  3.5,  'Rivières froides, rapides et bien oxygénées',       30,   'Thymallidae'),
(16, 'Omble chevalier',     'Salvelinus alpinus',     'Salmonidé des lacs alpins profonds et froids. Chair rosée d''excellente qualité. Pêche à la cuillère tournante ou aux leurres souples dans les grandes profondeurs.',                                                              NULL,                                                                      0.2,  5.0,  'Lacs alpins profonds et froids',                    NULL, 'Salmonidae'),
(17, 'Carpe koï',           'Cyprinus carpio (koï)',   'Variété ornementale de la carpe commune, parfois présente dans certains plans d''eau. Colorations spectaculaires. Pêche en étang privé principalement.',                                                                         NULL,                                                                      1.0,  20.0, 'Étangs privés et plans d''eau aménagés',            NULL, 'Cyprinidae'),
(18, 'Anguille européenne', 'Anguilla anguilla',      'Poisson migrateur catadrome en voie d''extinction. Reconnaissable à son corps serpentiforme. Pêche traditionnelle mais très réglementée. Espèce protégée dans de nombreux départements.',                                        NULL,                                                                      0.1,  3.0,  'Tous milieux d''eau douce, milieux côtiers',        NULL, 'Anguillidae'),
(19, 'Rotengle',            'Scardinius erythrophthalmus','Cyprinidé aux nageoires rouge vif facilement reconnaissable. Apprécie les eaux chaudes et ensoleiillées. Pêche en surface avec du pain ou des insectes.',                                                                   NULL,                                                                      0.05, 1.5,  'Étangs, lacs et cours d''eau lents et herbus',      NULL, 'Cyprinidae'),
(20, 'Blageon',             'Telestes souffia',       'Petit cyprinidé des rivières méditerranéennes. Espèce endémique protégée dans certaines zones. Pêche légère à l''ultra-léger.',                                                                                                   NULL,                                                                      0.02, 0.3,  'Rivières claires et fraîches du Midi',              NULL, 'Cyprinidae');

-- =============================================================================
--  UTILISATEURS  (mot de passe : Fishdex2026!)
--  Hash BCrypt rounds=10 de "Fishdex2026!"
-- =============================================================================

INSERT INTO users (id, email, username, password, is_premium, capture_count, email_verified, email_verified_at, failed_login_attempts, two_factor_enabled, created_at) VALUES
-- Comptes premium (beta testeurs confirmés)
(1,  'marc.dupont@gmail.com',       'MarcCarpiste',     '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRFbe3UlIJSbRoTzBpMJ5wMg3vqIW', TRUE,  24, TRUE, NOW() - INTERVAL 120 DAY, 0, FALSE, NOW() - INTERVAL 125 DAY),
(2,  'sophie.martin@yahoo.fr',      'SophieLeurres',    '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRFbe3UlIJSbRoTzBpMJ5wMg3vqIW', TRUE,  18, TRUE, NOW() - INTERVAL 90 DAY,  0, FALSE, NOW() - INTERVAL 95 DAY),
(3,  'pierre.bernard@orange.fr',    'PierreFluvial',    '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRFbe3UlIJSbRoTzBpMJ5wMg3vqIW', FALSE, 12, TRUE, NOW() - INTERVAL 60 DAY,  0, FALSE, NOW() - INTERVAL 65 DAY),
(4,  'emilie.rousseau@hotmail.com', 'EmilieMouche',     '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRFbe3UlIJSbRoTzBpMJ5wMg3vqIW', TRUE,  31, TRUE, NOW() - INTERVAL 80 DAY,  0, FALSE, NOW() - INTERVAL 85 DAY),
(5,  'julien.moreau@gmail.com',     'JulienSilure',     '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRFbe3UlIJSbRoTzBpMJ5wMg3vqIW', TRUE,  9,  TRUE, NOW() - INTERVAL 45 DAY,  0, FALSE, NOW() - INTERVAL 50 DAY),
-- Comptes freemium
(6,  'lucie.petit@gmail.com',       'LuciePeche',       '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRFbe3UlIJSbRoTzBpMJ5wMg3vqIW', FALSE, 7,  TRUE, NOW() - INTERVAL 30 DAY,  0, FALSE, NOW() - INTERVAL 35 DAY),
(7,  'thomas.leroy@gmail.com',      'ThomasCarpeur',    '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRFbe3UlIJSbRoTzBpMJ5wMg3vqIW', FALSE, 5,  TRUE, NOW() - INTERVAL 20 DAY,  0, FALSE, NOW() - INTERVAL 25 DAY),
(8,  'camille.durand@gmail.com',    'CamilleTruite',    '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRFbe3UlIJSbRoTzBpMJ5wMg3vqIW', FALSE, 3,  TRUE, NOW() - INTERVAL 15 DAY,  0, FALSE, NOW() - INTERVAL 18 DAY),
(9,  'antoine.simon@gmail.com',     'AntoinePredateur', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRFbe3UlIJSbRoTzBpMJ5wMg3vqIW', FALSE, 2,  FALSE, NULL,                     0, FALSE, NOW() - INTERVAL 8 DAY),
(10, 'chloe.michel@gmail.com',      'ChloePecheMouche', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRFbe3UlIJSbRoTzBpMJ5wMg3vqIW', TRUE,  15, TRUE, NOW() - INTERVAL 55 DAY,  0, FALSE, NOW() - INTERVAL 60 DAY);

-- =============================================================================
--  CAPTURES
-- =============================================================================

INSERT INTO captures (id, user_id, species_id, species_name, weight, length, photo_url, latitude, longitude, note, caught_at, created_at) VALUES

-- ── Marc (carpiste IDF, 24 captures) ────────────────────────────────────────
(1,  1, 2,  'Carpe commune',   14.2, 82.0, NULL, 48.7128,  2.1600, 'Bouillette fraise, fond 4m. Ferrage puissant à 2h du matin.',         NOW() - INTERVAL 2 DAY,   NOW() - INTERVAL 2 DAY),
(2,  1, 2,  'Carpe commune',   18.6, 91.0, NULL, 48.7128,  2.1600, 'Record perso ! Pêche de nuit, vent du sud. Combat 25 min.',            NOW() - INTERVAL 5 DAY,   NOW() - INTERVAL 5 DAY),
(3,  1, 2,  'Carpe commune',    9.1, 74.0, NULL, 48.7745,  2.4570, 'Zig-rig surface par 30°C. Relâchée en parfait état.',                  NOW() - INTERVAL 8 DAY,   NOW() - INTERVAL 8 DAY),
(4,  1, 14, 'Carpe miroir',    22.4, 98.0, NULL, 48.7745,  2.4570, 'Énorme miroir avec 3 grandes écailles. Combat de 20 min dans les nénuphars.', NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY),
(5,  1, 2,  'Carpe commune',    7.3, 68.0, NULL, 49.0000,  2.0980, NULL,                                                                  NOW() - INTERVAL 18 DAY,  NOW() - INTERVAL 18 DAY),
(6,  1, 1,  'Brochet',          3.8, 78.0, NULL, 48.8200,  2.4770, 'Gros brochet sorti au leurre souple 15cm. Attaque en lisière de joncs.', NOW() - INTERVAL 22 DAY, NOW() - INTERVAL 22 DAY),
(7,  1, 1,  'Brochet',          2.1, 65.0, NULL, 48.8200,  2.4770, NULL,                                                                  NOW() - INTERVAL 30 DAY,  NOW() - INTERVAL 30 DAY),
(8,  1, 3,  'Sandre',           1.9, 52.0, NULL, 48.8534,  2.3488, 'Soirée post-tempête, eau trouble. Souple texan.',                     NOW() - INTERVAL 35 DAY,  NOW() - INTERVAL 35 DAY),
(9,  1, 3,  'Sandre',           3.2, 61.0, NULL, 48.7745,  2.4570, 'Drop shot fond de gravière, 6m. Belle capture.',                      NOW() - INTERVAL 40 DAY,  NOW() - INTERVAL 40 DAY),
(10, 1, 5,  'Silure glane',    31.0,155.0, NULL, 48.5400,  2.6600, 'Combat 45min. Ferré à la bouillette. Relâché avec soin.',              NOW() - INTERVAL 45 DAY,  NOW() - INTERVAL 45 DAY),
(11, 1, 4,  'Perche',           0.7, 32.0, NULL, 48.7128,  2.1600, NULL,                                                                  NOW() - INTERVAL 50 DAY,  NOW() - INTERVAL 50 DAY),
(12, 1, 1,  'Brochet',          4.6, 83.0, NULL, 48.8800,  2.5100, 'Énorme attaque en surface, spinner. Vue avant l''attaque !',          NOW() - INTERVAL 55 DAY,  NOW() - INTERVAL 55 DAY),
(13, 1, 2,  'Carpe commune',   11.5, 78.0, NULL, 48.7128,  2.1600, 'Session 48h, premier départ à l''aube. Temps nuageux idéal.',         NOW() - INTERVAL 62 DAY,  NOW() - INTERVAL 62 DAY),
(14, 1, 14, 'Carpe miroir',    15.8, 89.0, NULL, 48.7745,  2.4570, 'Miroir très vieille cicatrice, certainement une ancienne connaissance.', NOW() - INTERVAL 70 DAY, NOW() - INTERVAL 70 DAY),
(15, 1, 2,  'Carpe commune',    6.4, 65.0, NULL, 49.0000,  2.0980, NULL,                                                                  NOW() - INTERVAL 75 DAY,  NOW() - INTERVAL 75 DAY),
(16, 1, 5,  'Silure glane',    48.0,168.0, NULL, 48.5400,  2.6600, 'Monstre ! Plus grand silure de ma vie. Pêche au wels.',               NOW() - INTERVAL 80 DAY,  NOW() - INTERVAL 80 DAY),
(17, 1, 3,  'Sandre',           2.5, 55.0, NULL, 48.8534,  2.3488, NULL,                                                                  NOW() - INTERVAL 85 DAY,  NOW() - INTERVAL 85 DAY),
(18, 1, 2,  'Carpe commune',   25.1,103.0, NULL, 48.7128,  2.1600, 'Vingt-cinq kilos ! Bouillette pop-up vanille. Nuit parfaite.',         NOW() - INTERVAL 90 DAY,  NOW() - INTERVAL 90 DAY),
(19, 1, 1,  'Brochet',          5.9, 90.0, NULL, 48.8200,  2.4770, 'Beau brochet au swimbaiter 25cm. Attaque visible en surface.',        NOW() - INTERVAL 95 DAY,  NOW() - INTERVAL 95 DAY),
(20, 1, 4,  'Perche',           1.2, 38.0, NULL, 48.7128,  2.1600, NULL,                                                                  NOW() - INTERVAL 100 DAY, NOW() - INTERVAL 100 DAY),
(21, 1, 2,  'Carpe commune',    8.8, 72.0, NULL, 48.7745,  2.4570, NULL,                                                                  NOW() - INTERVAL 105 DAY, NOW() - INTERVAL 105 DAY),
(22, 1, 10, 'Black-bass',       1.4, 40.0, NULL, 48.8534,  2.3488, 'Session découverte. Top water à l''aube, explosif !',                 NOW() - INTERVAL 110 DAY, NOW() - INTERVAL 110 DAY),
(23, 1, 14, 'Carpe miroir',    19.3, 94.0, NULL, 48.7128,  2.1600, NULL,                                                                  NOW() - INTERVAL 115 DAY, NOW() - INTERVAL 115 DAY),
(24, 1, 2,  'Carpe commune',   12.0, 80.0, NULL, 48.7745,  2.4570, 'Capture en pêche de pleine eau.',                                    NOW() - INTERVAL 120 DAY, NOW() - INTERVAL 120 DAY),

-- ── Sophie (chasseuse de leurres, 18 captures) ──────────────────────────────
(25, 2, 1,  'Brochet',          2.4, 68.0, NULL, 43.7500, -1.3400, 'Jerkbait couleur perche, eau claire à 19°C.',                         NOW() - INTERVAL 3 DAY,   NOW() - INTERVAL 3 DAY),
(26, 2, 3,  'Sandre',           1.6, 49.0, NULL, 44.8400, -0.5800, 'Drop shot en bordure de quai. Classique mais efficace.',              NOW() - INTERVAL 7 DAY,   NOW() - INTERVAL 7 DAY),
(27, 2, 10, 'Black-bass',       1.1, 38.0, NULL, 44.5200, -1.1600, 'Top water à l''aube — attaque explosive ! Superbe combat.',           NOW() - INTERVAL 14 DAY,  NOW() - INTERVAL 14 DAY),
(28, 2, 1,  'Brochet',          5.1, 88.0, NULL, 43.7500, -1.3400, 'Gros brochet côtier au swimbaiter. Record perso !',                   NOW() - INTERVAL 20 DAY,  NOW() - INTERVAL 20 DAY),
(29, 2, 4,  'Perche',           0.4, 27.0, NULL, 44.8400, -0.5800, NULL,                                                                  NOW() - INTERVAL 28 DAY,  NOW() - INTERVAL 28 DAY),
(30, 2, 3,  'Sandre',           2.8, 58.0, NULL, 44.5200, -1.1600, 'Soirée grub 10cm, fond vase à 4m.',                                  NOW() - INTERVAL 38 DAY,  NOW() - INTERVAL 38 DAY),
(31, 2, 1,  'Brochet',          1.9, 60.0, NULL, 43.7500, -1.3400, NULL,                                                                  NOW() - INTERVAL 60 DAY,  NOW() - INTERVAL 60 DAY),
(32, 2, 10, 'Black-bass',       0.8, 33.0, NULL, 44.5200, -1.1600, 'Petit bass, gros combat !',                                           NOW() - INTERVAL 65 DAY,  NOW() - INTERVAL 65 DAY),
(33, 2, 4,  'Perche',           0.6, 29.0, NULL, 43.7500, -1.3400, NULL,                                                                  NOW() - INTERVAL 70 DAY,  NOW() - INTERVAL 70 DAY),
(34, 2, 1,  'Brochet',          3.3, 73.0, NULL, 44.8400, -0.5800, 'Brochet au minnow suspending dans les îlots de joncs.',              NOW() - INTERVAL 75 DAY,  NOW() - INTERVAL 75 DAY),
(35, 2, 3,  'Sandre',           1.2, 44.0, NULL, 44.5200, -1.1600, NULL,                                                                  NOW() - INTERVAL 78 DAY,  NOW() - INTERVAL 78 DAY),
(36, 2, 10, 'Black-bass',       2.1, 46.0, NULL, 43.7500, -1.3400, 'Super combat dans les herbiers. Leurre surface popper.',             NOW() - INTERVAL 82 DAY,  NOW() - INTERVAL 82 DAY),
(37, 2, 1,  'Brochet',          4.0, 80.0, NULL, 44.8400, -0.5800, NULL,                                                                  NOW() - INTERVAL 86 DAY,  NOW() - INTERVAL 86 DAY),
(38, 2, 3,  'Sandre',           3.5, 63.0, NULL, 44.5200, -1.1600, 'Très beau sandre, pêche de nuit à la jig tête plombée.',             NOW() - INTERVAL 89 DAY,  NOW() - INTERVAL 89 DAY),
(39, 2, 4,  'Perche',           0.9, 35.0, NULL, 43.7500, -1.3400, NULL,                                                                  NOW() - INTERVAL 92 DAY,  NOW() - INTERVAL 92 DAY),
(40, 2, 10, 'Black-bass',       1.7, 43.0, NULL, 44.5200, -1.1600, NULL,                                                                  NOW() - INTERVAL 95 DAY,  NOW() - INTERVAL 95 DAY),
(41, 2, 1,  'Brochet',          2.6, 70.0, NULL, 44.8400, -0.5800, NULL,                                                                  NOW() - INTERVAL 98 DAY,  NOW() - INTERVAL 98 DAY),
(42, 2, 3,  'Sandre',           2.0, 51.0, NULL, 44.5200, -1.1600, NULL,                                                                  NOW() - INTERVAL 101 DAY, NOW() - INTERVAL 101 DAY),

-- ── Emilie (pêcheuse à la mouche, 31 captures) ──────────────────────────────
(43, 4, 6,  'Truite fario',     0.6, 31.0, NULL, 45.9600,  5.3500, 'Sèche Adams taille 14. Eau cristalline, visibilité parfaite.',       NOW() - INTERVAL 4 DAY,   NOW() - INTERVAL 4 DAY),
(44, 4, 6,  'Truite fario',     1.2, 42.0, NULL, 45.9600,  5.3500, 'Nymphe tungstène à contre-courant. Belle fario dorée.',               NOW() - INTERVAL 9 DAY,   NOW() - INTERVAL 9 DAY),
(45, 4, 9,  'Barbeau fluviatile',2.1,55.0, NULL, 45.7600,  4.8300, 'Pelote de vers, fond courant. Barbeau de belle taille.',              NOW() - INTERVAL 16 DAY,  NOW() - INTERVAL 16 DAY),
(46, 4, 11, 'Chevaine',         0.8, 36.0, NULL, 45.7600,  4.8300, 'Surface, mouche sèche imitant un insecte tombé. Prise visible !',    NOW() - INTERVAL 24 DAY,  NOW() - INTERVAL 24 DAY),
(47, 4, 6,  'Truite fario',     0.9, 37.0, NULL, 46.0500,  5.3300, NULL,                                                                  NOW() - INTERVAL 33 DAY,  NOW() - INTERVAL 33 DAY),
(48, 4, 15, 'Ombre commun',     0.5, 34.0, NULL, 45.9600,  5.3500, 'Première ombre de ma vie ! Nageoire dorsale magnifique.',            NOW() - INTERVAL 40 DAY,  NOW() - INTERVAL 40 DAY),
(49, 4, 6,  'Truite fario',     1.8, 50.0, NULL, 46.0500,  5.3300, 'Belle fario tachetée. Streamer olive en soirée.',                    NOW() - INTERVAL 45 DAY,  NOW() - INTERVAL 45 DAY),
(50, 4, 6,  'Truite fario',     0.4, 27.0, NULL, 45.9600,  5.3500, NULL,                                                                  NOW() - INTERVAL 50 DAY,  NOW() - INTERVAL 50 DAY),
(51, 4, 9,  'Barbeau fluviatile',1.5,48.0, NULL, 45.7600,  4.8300, NULL,                                                                  NOW() - INTERVAL 54 DAY,  NOW() - INTERVAL 54 DAY),
(52, 4, 6,  'Truite fario',     2.3, 55.0, NULL, 46.0500,  5.3300, 'Record ! Superbe fario de 55cm. Dry fly elk hair caddis.',           NOW() - INTERVAL 58 DAY,  NOW() - INTERVAL 58 DAY),
(53, 4, 11, 'Chevaine',         1.2, 42.0, NULL, 45.7600,  4.8300, NULL,                                                                  NOW() - INTERVAL 62 DAY,  NOW() - INTERVAL 62 DAY),
(54, 4, 6,  'Truite fario',     0.7, 33.0, NULL, 45.9600,  5.3500, NULL,                                                                  NOW() - INTERVAL 65 DAY,  NOW() - INTERVAL 65 DAY),
(55, 4, 15, 'Ombre commun',     0.8, 38.0, NULL, 46.0500,  5.3300, 'Bel ombre au nymphe. Eau froide et transparente.',                   NOW() - INTERVAL 69 DAY,  NOW() - INTERVAL 69 DAY),
(56, 4, 6,  'Truite fario',     1.1, 40.0, NULL, 45.9600,  5.3500, NULL,                                                                  NOW() - INTERVAL 73 DAY,  NOW() - INTERVAL 73 DAY),
(57, 4, 6,  'Truite fario',     0.5, 30.0, NULL, 46.0500,  5.3300, NULL,                                                                  NOW() - INTERVAL 77 DAY,  NOW() - INTERVAL 77 DAY),
(58, 4, 9,  'Barbeau fluviatile',3.0,62.0, NULL, 45.7600,  4.8300, 'Énorme barbeau en courant vif. Combat intense !',                    NOW() - INTERVAL 80 DAY,  NOW() - INTERVAL 80 DAY),
(59, 4, 6,  'Truite fario',     0.8, 35.0, NULL, 45.9600,  5.3500, NULL,                                                                  NOW() - INTERVAL 84 DAY,  NOW() - INTERVAL 84 DAY),
(60, 4, 11, 'Chevaine',         0.6, 32.0, NULL, 45.7600,  4.8300, NULL,                                                                  NOW() - INTERVAL 87 DAY,  NOW() - INTERVAL 87 DAY),
(61, 4, 6,  'Truite fario',     1.5, 46.0, NULL, 46.0500,  5.3300, 'Belle fario en soirée.',                                             NOW() - INTERVAL 91 DAY,  NOW() - INTERVAL 91 DAY),
(62, 4, 15, 'Ombre commun',     0.3, 28.0, NULL, 45.9600,  5.3500, NULL,                                                                  NOW() - INTERVAL 95 DAY,  NOW() - INTERVAL 95 DAY),
(63, 4, 6,  'Truite fario',     0.9, 37.0, NULL, 46.0500,  5.3300, NULL,                                                                  NOW() - INTERVAL 98 DAY,  NOW() - INTERVAL 98 DAY),
(64, 4, 6,  'Truite fario',     0.6, 31.0, NULL, 45.9600,  5.3500, NULL,                                                                  NOW() - INTERVAL 102 DAY, NOW() - INTERVAL 102 DAY),
(65, 4, 9,  'Barbeau fluviatile',1.8,50.0, NULL, 45.7600,  4.8300, NULL,                                                                  NOW() - INTERVAL 106 DAY, NOW() - INTERVAL 106 DAY),
(66, 4, 6,  'Truite fario',     1.3, 44.0, NULL, 46.0500,  5.3300, NULL,                                                                  NOW() - INTERVAL 110 DAY, NOW() - INTERVAL 110 DAY),
(67, 4, 11, 'Chevaine',         0.9, 37.0, NULL, 45.7600,  4.8300, NULL,                                                                  NOW() - INTERVAL 114 DAY, NOW() - INTERVAL 114 DAY),
(68, 4, 6,  'Truite fario',     0.4, 26.0, NULL, 45.9600,  5.3500, NULL,                                                                  NOW() - INTERVAL 117 DAY, NOW() - INTERVAL 117 DAY),
(69, 4, 15, 'Ombre commun',     0.6, 33.0, NULL, 46.0500,  5.3300, NULL,                                                                  NOW() - INTERVAL 120 DAY, NOW() - INTERVAL 120 DAY),
(70, 4, 6,  'Truite fario',     2.0, 52.0, NULL, 45.9600,  5.3500, 'Deuxième plus belle fario.',                                         NOW() - INTERVAL 123 DAY, NOW() - INTERVAL 123 DAY),
(71, 4, 6,  'Truite fario',     0.7, 33.0, NULL, 46.0500,  5.3300, NULL,                                                                  NOW() - INTERVAL 126 DAY, NOW() - INTERVAL 126 DAY),
(72, 4, 9,  'Barbeau fluviatile',2.4,57.0, NULL, 45.7600,  4.8300, NULL,                                                                  NOW() - INTERVAL 129 DAY, NOW() - INTERVAL 129 DAY),
(73, 4, 6,  'Truite fario',     1.0, 39.0, NULL, 45.9600,  5.3500, NULL,                                                                  NOW() - INTERVAL 132 DAY, NOW() - INTERVAL 132 DAY),

-- ── Pierre (pêcheur rivière, 12 captures) ────────────────────────────────────
(74, 3, 9,  'Barbeau fluviatile',2.1,55.0, NULL, 45.7600,  4.8300, 'Pelote de vers, fond courant vif.',                                  NOW() - INTERVAL 4 DAY,   NOW() - INTERVAL 4 DAY),
(75, 3, 11, 'Chevaine',         0.8, 36.0, NULL, 45.7600,  4.8300, 'Surface, mouche sèche.',                                             NOW() - INTERVAL 9 DAY,   NOW() - INTERVAL 9 DAY),
(76, 3, 6,  'Truite fario',     0.5, 29.0, NULL, 44.3500,  5.0500, NULL,                                                                  NOW() - INTERVAL 16 DAY,  NOW() - INTERVAL 16 DAY),
(77, 3, 9,  'Barbeau fluviatile',1.6,48.0, NULL, 45.7600,  4.8300, NULL,                                                                  NOW() - INTERVAL 22 DAY,  NOW() - INTERVAL 22 DAY),
(78, 3, 11, 'Chevaine',         1.1, 40.0, NULL, 45.7600,  4.8300, 'Beau chevaine pris au pain flottant.',                               NOW() - INTERVAL 28 DAY,  NOW() - INTERVAL 28 DAY),
(79, 3, 6,  'Truite fario',     0.8, 34.0, NULL, 44.3500,  5.0500, 'Nymphe tungstène dans une fosse.',                                   NOW() - INTERVAL 35 DAY,  NOW() - INTERVAL 35 DAY),
(80, 3, 9,  'Barbeau fluviatile',3.4,65.0, NULL, 45.7600,  4.8300, 'Gros barbeau ! Combat dans le courant fort.',                        NOW() - INTERVAL 42 DAY,  NOW() - INTERVAL 42 DAY),
(81, 3, 12, 'Gardon',           0.2, 22.0, NULL, 45.7600,  4.8300, NULL,                                                                  NOW() - INTERVAL 50 DAY,  NOW() - INTERVAL 50 DAY),
(82, 3, 6,  'Truite fario',     1.3, 43.0, NULL, 44.3500,  5.0500, 'Streamer noir en soirée. Belle fario.',                              NOW() - INTERVAL 57 DAY,  NOW() - INTERVAL 57 DAY),
(83, 3, 11, 'Chevaine',         0.7, 33.0, NULL, 45.7600,  4.8300, NULL,                                                                  NOW() - INTERVAL 64 DAY,  NOW() - INTERVAL 64 DAY),
(84, 3, 9,  'Barbeau fluviatile',1.9,52.0, NULL, 45.7600,  4.8300, NULL,                                                                  NOW() - INTERVAL 70 DAY,  NOW() - INTERVAL 70 DAY),
(85, 3, 6,  'Truite fario',     0.6, 30.0, NULL, 44.3500,  5.0500, NULL,                                                                  NOW() - INTERVAL 77 DAY,  NOW() - INTERVAL 77 DAY),

-- ── Julien (pêche silure, 9 captures) ───────────────────────────────────────
(86, 5, 5,  'Silure glane',    52.0,172.0, NULL, 43.6047,  1.4442, 'Record ! Silure de 52kg sur la Garonne. Combat 1h10.',                NOW() - INTERVAL 5 DAY,   NOW() - INTERVAL 5 DAY),
(87, 5, 5,  'Silure glane',    28.0,148.0, NULL, 43.6047,  1.4442, 'Wels au boilies taille 28mm. Session de nuit.',                      NOW() - INTERVAL 12 DAY,  NOW() - INTERVAL 12 DAY),
(88, 5, 5,  'Silure glane',    15.0,130.0, NULL, 44.0000,  1.2500, NULL,                                                                  NOW() - INTERVAL 20 DAY,  NOW() - INTERVAL 20 DAY),
(89, 5, 5,  'Silure glane',    38.0,160.0, NULL, 43.6047,  1.4442, 'Très beau silure, 3 ferrades raté avant.',                           NOW() - INTERVAL 30 DAY,  NOW() - INTERVAL 30 DAY),
(90, 5, 5,  'Silure glane',    20.0,138.0, NULL, 44.0000,  1.2500, NULL,                                                                  NOW() - INTERVAL 40 DAY,  NOW() - INTERVAL 40 DAY),
(91, 5, 1,  'Brochet',          2.8, 71.0, NULL, 43.6047,  1.4442, 'Brochet sorti en cherchant les silures !',                           NOW() - INTERVAL 50 DAY,  NOW() - INTERVAL 50 DAY),
(92, 5, 5,  'Silure glane',    41.0,163.0, NULL, 43.6047,  1.4442, 'Beau silure à la pellet. Long combat.',                              NOW() - INTERVAL 60 DAY,  NOW() - INTERVAL 60 DAY),
(93, 5, 5,  'Silure glane',     9.0,110.0, NULL, 44.0000,  1.2500, NULL,                                                                  NOW() - INTERVAL 70 DAY,  NOW() - INTERVAL 70 DAY),
(94, 5, 5,  'Silure glane',    60.0,180.0, NULL, 43.6047,  1.4442, 'MONSTRE ! 60kg ! Plus grand poisson que j''aurai jamais vu.',         NOW() - INTERVAL 80 DAY,  NOW() - INTERVAL 80 DAY),

-- ── Lucie, Thomas, Camille, Antoine, Chloé ──────────────────────────────────
(95,  6, 12, 'Gardon',          0.1, 18.0, NULL, 48.8566,  2.3522, 'Premier gardon au coup !',                                           NOW() - INTERVAL 2 DAY,   NOW() - INTERVAL 2 DAY),
(96,  6, 12, 'Gardon',          0.2, 21.0, NULL, 48.8566,  2.3522, NULL,                                                                  NOW() - INTERVAL 7 DAY,   NOW() - INTERVAL 7 DAY),
(97,  6, 13, 'Brème commune',   0.8, 35.0, NULL, 48.8566,  2.3522, 'Belle brème au coup avec amorçage.',                                 NOW() - INTERVAL 14 DAY,  NOW() - INTERVAL 14 DAY),
(98,  6, 8,  'Tanche',          0.6, 30.0, NULL, 48.7128,  2.1600, NULL,                                                                  NOW() - INTERVAL 22 DAY,  NOW() - INTERVAL 22 DAY),
(99,  6, 12, 'Gardon',          0.15,19.0, NULL, 48.8566,  2.3522, NULL,                                                                  NOW() - INTERVAL 30 DAY,  NOW() - INTERVAL 30 DAY),
(100, 6, 4,  'Perche',          0.3, 24.0, NULL, 48.7745,  2.4570, NULL,                                                                  NOW() - INTERVAL 38 DAY,  NOW() - INTERVAL 38 DAY),
(101, 6, 12, 'Gardon',          0.1, 17.0, NULL, 48.8566,  2.3522, NULL,                                                                  NOW() - INTERVAL 45 DAY,  NOW() - INTERVAL 45 DAY),
(102, 7, 2,  'Carpe commune',   5.2, 62.0, NULL, 47.2184,  2.3700, 'Première carpe ! Bouillette ananas.',                                NOW() - INTERVAL 3 DAY,   NOW() - INTERVAL 3 DAY),
(103, 7, 2,  'Carpe commune',   8.1, 70.0, NULL, 47.2184,  2.3700, 'Deuxième session, belle amélioration !',                             NOW() - INTERVAL 10 DAY,  NOW() - INTERVAL 10 DAY),
(104, 7, 14, 'Carpe miroir',    3.5, 52.0, NULL, 47.2184,  2.3700, NULL,                                                                  NOW() - INTERVAL 18 DAY,  NOW() - INTERVAL 18 DAY),
(105, 7, 2,  'Carpe commune',   6.9, 67.0, NULL, 47.2184,  2.3700, NULL,                                                                  NOW() - INTERVAL 28 DAY,  NOW() - INTERVAL 28 DAY),
(106, 7, 4,  'Perche',          0.4, 26.0, NULL, 47.2184,  2.3700, NULL,                                                                  NOW() - INTERVAL 38 DAY,  NOW() - INTERVAL 38 DAY),
(107, 8, 7,  'Truite arc-en-ciel',0.8,35.0,NULL, 45.1885,  5.7245, 'Plan d''eau de pêche. Super journée !',                              NOW() - INTERVAL 5 DAY,   NOW() - INTERVAL 5 DAY),
(108, 8, 7,  'Truite arc-en-ciel',1.2,42.0,NULL, 45.1885,  5.7245, NULL,                                                                  NOW() - INTERVAL 14 DAY,  NOW() - INTERVAL 14 DAY),
(109, 8, 6,  'Truite fario',    0.4, 26.0, NULL, 45.1885,  5.7245, 'Première fario sauvage !',                                           NOW() - INTERVAL 25 DAY,  NOW() - INTERVAL 25 DAY),
(110, 9, 3,  'Sandre',          0.9, 40.0, NULL, 47.3220,  5.0415, 'Début à la pêche aux leurres.',                                      NOW() - INTERVAL 6 DAY,   NOW() - INTERVAL 6 DAY),
(111, 9, 4,  'Perche',          0.3, 22.0, NULL, 47.3220,  5.0415, NULL,                                                                  NOW() - INTERVAL 15 DAY,  NOW() - INTERVAL 15 DAY),
(112, 10, 1, 'Brochet',         3.1, 72.0, NULL, 43.2965,  5.3698, 'Brochet au minnow dans une crique herbeuse.',                        NOW() - INTERVAL 4 DAY,   NOW() - INTERVAL 4 DAY),
(113, 10, 3, 'Sandre',          2.4, 54.0, NULL, 43.2965,  5.3698, 'Soirée drop shot. Très beau sandre.',                                NOW() - INTERVAL 11 DAY,  NOW() - INTERVAL 11 DAY),
(114, 10, 10,'Black-bass',       1.6, 42.0, NULL, 43.2965,  5.3698, NULL,                                                                 NOW() - INTERVAL 18 DAY,  NOW() - INTERVAL 18 DAY),
(115, 10, 1, 'Brochet',         4.8, 86.0, NULL, 43.2965,  5.3698, 'Beau brochet de Méditerranée.',                                      NOW() - INTERVAL 26 DAY,  NOW() - INTERVAL 26 DAY),
(116, 10, 3, 'Sandre',          1.8, 48.0, NULL, 43.2965,  5.3698, NULL,                                                                  NOW() - INTERVAL 34 DAY,  NOW() - INTERVAL 34 DAY),
(117, 10, 10,'Black-bass',       0.9, 36.0, NULL, 43.2965,  5.3698, NULL,                                                                 NOW() - INTERVAL 42 DAY,  NOW() - INTERVAL 42 DAY),
(118, 10, 1, 'Brochet',         2.2, 64.0, NULL, 43.2965,  5.3698, NULL,                                                                  NOW() - INTERVAL 50 DAY,  NOW() - INTERVAL 50 DAY),
(119, 10, 4, 'Perche',          0.5, 28.0, NULL, 43.2965,  5.3698, NULL,                                                                  NOW() - INTERVAL 58 DAY,  NOW() - INTERVAL 58 DAY),
(120, 10, 3, 'Sandre',          3.0, 60.0, NULL, 43.2965,  5.3698, 'Beau sandre pris en verticale.',                                     NOW() - INTERVAL 65 DAY,  NOW() - INTERVAL 65 DAY),
(121, 10, 10,'Black-bass',       1.3, 40.0, NULL, 43.2965,  5.3698, NULL,                                                                 NOW() - INTERVAL 73 DAY,  NOW() - INTERVAL 73 DAY),
(122, 10, 1, 'Brochet',         5.4, 91.0, NULL, 43.2965,  5.3698, 'Record perso ! Brochet au swimbaiter.',                              NOW() - INTERVAL 80 DAY,  NOW() - INTERVAL 80 DAY),
(123, 10, 3, 'Sandre',          1.5, 46.0, NULL, 43.2965,  5.3698, NULL,                                                                  NOW() - INTERVAL 87 DAY,  NOW() - INTERVAL 87 DAY),
(124, 10, 10,'Black-bass',       2.0, 45.0, NULL, 43.2965,  5.3698, 'Gros bass en surface au dawn patrol !',                             NOW() - INTERVAL 94 DAY,  NOW() - INTERVAL 94 DAY),
(125, 10, 4, 'Perche',          0.8, 32.0, NULL, 43.2965,  5.3698, NULL,                                                                  NOW() - INTERVAL 100 DAY, NOW() - INTERVAL 100 DAY);

-- =============================================================================
--  GROUPES
-- =============================================================================

INSERT INTO fishing_groups (id, name, description, visibility, category, cover_photo_url, rules, post_count, creator_id, created_at) VALUES
(1, 'Carpistes Île-de-France',
    'Le groupe des passionnés de carpe en région parisienne. Sorties collectives, bons plans étangs et conseils montages. Rejoignez-nous pour partager vos captures et vos expériences !',
    'PUBLIC', 'CLUB', NULL,
    '1. Respecter les périodes de fermeture\n2. Catch & release encouragé\n3. Photos obligatoires pour les records\n4. Partager les spots dans le respect',
    0, 1, NOW() - INTERVAL 110 DAY),

(2, 'Pêcheurs du Rhône',
    'Association de pêche sur le bassin du Rhône et de l''Ain. Truites, barbeaux, chevaines et ombres — la pêche en rivière dans toute sa splendeur.',
    'PUBLIC', 'ASSOCIATION', NULL,
    '1. Respecter la réglementation locale\n2. Taille légale strictement respectée\n3. Pêche à la mouche recommandée mais non obligatoire\n4. Signaler tout incident environnemental',
    0, 4, NOW() - INTERVAL 75 DAY),

(3, 'Team Silure Garonne',
    'Les chasseurs de géants de la Garonne. Sessions nocturnes, spots secrets et techniques de pêche au silure partagés entre passionnés.',
    'PRIVATE', 'FRIENDS', NULL,
    '1. Membres cooptés uniquement\n2. Spots confidentiels — ne pas divulguer\n3. Remise à l''eau systématique\n4. Partage obligatoire des techniques',
    0, 5, NOW() - INTERVAL 40 DAY),

(4, 'Les Leuristes du Sud',
    'Brochet, sandre, black-bass et perche — tout sur la pêche aux leurres dans le Sud de la France. Ouvert à tous niveaux.',
    'PUBLIC', 'FRIENDS', NULL,
    '1. Partage bienvenu\n2. Pas de spam\n3. Photos de qualité appréciées',
    0, 2, NOW() - INTERVAL 30 DAY),

(5, 'Club Mouche Alpin',
    'Club de pêche à la mouche dans les rivières des Alpes et du Jura. Sorties accompagnées, formation débutants et concours amicaux.',
    'PUBLIC', 'CLUB', NULL,
    '1. Pêche no-kill sur les parcours désignés\n2. Usage d''hameçons sans ardillon recommandé\n3. Cotisation annuelle 30€',
    0, 4, NOW() - INTERVAL 20 DAY);

-- =============================================================================
--  MEMBRES DES GROUPES
-- =============================================================================

INSERT INTO group_members (id, group_id, user_id, role, joined_at) VALUES
-- Carpistes IDF
(1,  1, 1, 'OWNER',     NOW() - INTERVAL 110 DAY),
(2,  1, 2, 'MEMBER',    NOW() - INTERVAL 100 DAY),
(3,  1, 7, 'MEMBER',    NOW() - INTERVAL 85 DAY),
(4,  1, 6, 'MEMBER',    NOW() - INTERVAL 60 DAY),

-- Pêcheurs du Rhône
(5,  2, 4, 'OWNER',     NOW() - INTERVAL 75 DAY),
(6,  2, 3, 'ADMIN',     NOW() - INTERVAL 70 DAY),
(7,  2, 1, 'MEMBER',    NOW() - INTERVAL 65 DAY),
(8,  2, 8, 'MEMBER',    NOW() - INTERVAL 50 DAY),

-- Team Silure Garonne (privé)
(9,  3, 5, 'OWNER',     NOW() - INTERVAL 40 DAY),
(10, 3, 1, 'MEMBER',    NOW() - INTERVAL 35 DAY),

-- Les Leuristes du Sud
(11, 4, 2, 'OWNER',     NOW() - INTERVAL 30 DAY),
(12, 4, 10,'ADMIN',     NOW() - INTERVAL 28 DAY),
(13, 4, 9, 'MEMBER',    NOW() - INTERVAL 20 DAY),

-- Club Mouche Alpin
(14, 5, 4, 'OWNER',     NOW() - INTERVAL 20 DAY),
(15, 5, 3, 'MEMBER',    NOW() - INTERVAL 18 DAY),
(16, 5, 8, 'MEMBER',    NOW() - INTERVAL 15 DAY);

-- =============================================================================
--  DEMANDES D'ADHÉSION (groupe privé Team Silure)
-- =============================================================================

INSERT INTO group_join_requests (id, group_id, user_id, message, status, requested_at) VALUES
(1, 3, 2, 'Passionné de silure depuis 5 ans sur la Garonne. Je connais bien les secteurs Toulouse/Agen.', 'PENDING',  NOW() - INTERVAL 5 DAY),
(2, 3, 6, 'Je débute le silure mais très motivée ! Je suis prête à apprendre.',                           'PENDING',  NOW() - INTERVAL 3 DAY),
(3, 3, 7, 'Déjà pêché quelques silures avec des amis, aimerais rejoindre un groupe sérieux.',             'REJECTED', NOW() - INTERVAL 20 DAY);

-- =============================================================================
--  POSTS (feed des groupes)
-- =============================================================================

INSERT INTO posts (id, group_id, user_id, content, capture_id, pinned, edited_at, created_at) VALUES

-- Carpistes IDF
(1,  1, 1, 'Belle session ce week-end à l''étang de Saclay ! Carpe miroir de 22kg décrochée après 20 min de combat dans les nénuphars. La technique bouillette pop-up vanille est vraiment redoutable par ces températures. Qui est partant pour une session nocturne le week-end prochain ?', 4,  TRUE,  NULL, NOW() - INTERVAL 12 DAY),
(2,  1, 2, 'Premier brochet de l''étang ce matin ! Petit mais le combat était sympa 🎣 Vous recommandez quoi comme leurre pour ce spot ? Le fond est très herbeux.', NULL, FALSE, NULL, NOW() - INTERVAL 10 DAY),
(3,  1, 7, 'Question pour les anciens : vous utilisez quoi comme bas de ligne pour les grosses carpes ? J''ai eu deux décrochages hier, j''aimerais comprendre pourquoi.', NULL, FALSE, NULL, NOW() - INTERVAL 8 DAY),
(4,  1, 1, 'Compte-rendu de la session du week-end : 3 carpes dont une miroir de 15kg. Conditions parfaites — vent du sud, ciel couvert. La pression atmosphérique basse les a rendues actives. Prochaine sortie collective le 15 du mois, inscrivez-vous en commentaire !', 14, FALSE, NULL, NOW() - INTERVAL 6 DAY),
(5,  1, 6, 'Première capture dans le groupe ! Un gardon... je sais c''est pas une carpe mais c''est un début 😅 Merci pour les conseils sur l''amorçage !', 95, FALSE, NULL, NOW() - INTERVAL 4 DAY),
(6,  1, 2, 'Astuce du jour : par forte chaleur, cherchez les carpes en eau profonde le matin et en surface le soir. J''ai capturé cette belle commune en zig-rig à 20h hier soir.', 3,  FALSE, NULL, NOW() - INTERVAL 2 DAY),

-- Pêcheurs du Rhône
(7,  2, 4, 'Magnifique matinée sur l''Ain ! Fario de 55cm à la mouche sèche elk hair caddis. L''eau était cristalline, on voyait la montée depuis 5m. Ces moments sont irremplaçables 🪁', 52, TRUE,  NULL, NOW() - INTERVAL 9 DAY),
(8,  2, 3, 'Le barbeau fluviatile du Rhône est en pleine forme en ce moment ! J''en ai sorti un de 3,4kg hier dans le courant vif à Givors. Pêche au fond avec pelote de vers, ça marche à tous les coups.', 80, FALSE, NULL, NOW() - INTERVAL 7 DAY),
(9,  2, 1, 'Premier ombre commun de ma vie la semaine dernière grâce aux conseils d''Emilie ! La nageoire dorsale de ce poisson est vraiment spectaculaire. Merci pour les recommandations de mouches.', 48, FALSE, NULL, NOW() - INTERVAL 5 DAY),
(10, 2, 4, 'Rappel : sortie collective samedi matin sur l''Ain à Poncin. Rdv 6h30 au pont de pierre. Mouches fournies pour les débutants. N''oubliez pas votre carte de pêche !', NULL, FALSE, NULL, NOW() - INTERVAL 3 DAY),
(11, 2, 8, 'Ma première truite fario sauvage ! Prise au plan d''eau de Sassenage avec une arc-en-ciel achetée. Je sais que c''est pas pareil mais c''était magique quand même 😊', 109,FALSE, NULL, NOW() - INTERVAL 1 DAY),

-- Les Leuristes du Sud
(12, 4, 2,  'Brochet de 5kg au swimbaiter ce matin au lac de Soustons ! L''attaque était visible depuis la surface, mon cœur a failli lâcher quand j''ai vu le poisson charger le leurre. Quelle adrénaline !', 28, TRUE,  NULL, NOW() - INTERVAL 20 DAY),
(13, 4, 10, 'Session matinale productive : brochet 4,8kg + sandre 2,4kg + bass 1,6kg. Le minnow suspending en couleur silver flash est une vraie machine ici. Eau à 18°C, parfait pour les carnassiers.', 115,FALSE, NULL, NOW() - INTERVAL 15 DAY),
(14, 4, 9,  'Mon premier sandre ! 900g seulement mais pour un débutant c''est déjà énorme. Drop shot avec une petite shad en couleur naturelle. Merci pour les conseils de la communauté 🙏', 110,FALSE, NULL, NOW() - INTERVAL 10 DAY),
(15, 4, 2,  'Tips pour les leurres de surface par chaleur : pêchez absolument tôt le matin avant 8h ou après 20h. Les carnassiers sont en léthargie dans la journée quand l''eau dépasse 22°C.', NULL, FALSE, NULL, NOW() - INTERVAL 5 DAY);

-- =============================================================================
--  RÉACTIONS AUX POSTS
-- =============================================================================

INSERT INTO post_reactions (id, post_id, user_id, type) VALUES
(1,  1, 2, 'TROPHY'),
(2,  1, 7, 'FIRE'),
(3,  1, 6, 'WOW'),
(4,  2, 1, 'LIKE'),
(5,  2, 7, 'LIKE'),
(6,  4, 2, 'TROPHY'),
(7,  4, 6, 'FIRE'),
(8,  4, 7, 'LIKE'),
(9,  7, 3, 'WOW'),
(10, 7, 1, 'TROPHY'),
(11, 7, 8, 'FIRE'),
(12, 8, 4, 'LIKE'),
(13, 8, 1, 'FIRE'),
(14, 9, 4, 'LIKE'),
(15, 9, 3, 'TROPHY'),
(16, 12,10,'FIRE'),
(17, 12, 9,'WOW'),
(18, 13, 2,'TROPHY'),
(19, 13, 9,'FIRE');

-- =============================================================================
--  COMMENTAIRES
-- =============================================================================

INSERT INTO post_comments (id, post_id, user_id, content, parent_id, like_count, edited_at, created_at) VALUES
-- Post 1 : miroir de Marc
(1,  1, 2,  'Incroyable ! 22kg c''est un monstre. Tu l''as remis à l''eau ?',                                                      NULL, 3, NULL, NOW() - INTERVAL 11 DAY + INTERVAL 2 HOUR),
(2,  1, 1,  'Oui, photo et remise à l''eau immédiate. Elle était en parfaite santé !',                                              1,    1, NULL, NOW() - INTERVAL 11 DAY + INTERVAL 3 HOUR),
(3,  1, 7,  'Quel beau poisson. Quelle bouillette tu utilisais exactement ? Marque et parfum ?',                                    NULL, 2, NULL, NOW() - INTERVAL 10 DAY + INTERVAL 8 HOUR),
(4,  1, 1,  'Mainline Cell 20mm. Parfum fraise-vanille. Je les achète chez Carpe Dreams à Paris.',                                  3,    2, NULL, NOW() - INTERVAL 10 DAY + INTERVAL 9 HOUR),

-- Post 2 : brochet de Sophie
(5,  2, 1,  'Pour les fonds herbeux essaie un leurre weedless type Texas rig, tu éviteras les accrochages.',                       NULL, 4, NULL, NOW() - INTERVAL 9 DAY + INTERVAL 1 HOUR),
(6,  2, 7,  'Ou une tête plombée à palette anti-herbe, c''est très efficace aussi.',                                               5,    1, NULL, NOW() - INTERVAL 9 DAY + INTERVAL 2 HOUR),
(7,  2, 2,  'Merci ! Je vais tester le Texas rig ce week-end.',                                                                    5,    1, NULL, NOW() - INTERVAL 9 DAY + INTERVAL 4 HOUR),

-- Post 3 : question de Thomas
(8,  3, 1,  'Pour les gros poissons je recommande un fluoro 35/100 minimum. Moins visible et plus résistant à l''abrasion.',       NULL, 5, NULL, NOW() - INTERVAL 7 DAY + INTERVAL 3 HOUR),
(9,  3, 2,  'Et vérifie les nœuds surtout ! Un mauvais nœud palomar c''est la principale cause de décrochage.',                   NULL, 3, NULL, NOW() - INTERVAL 7 DAY + INTERVAL 5 HOUR),
(10, 3, 7,  'Merci les gars, je vais tester le fluoro 40/100. Je vous dirai si ça change quelque chose.',                         8,    1, NULL, NOW() - INTERVAL 7 DAY + INTERVAL 7 HOUR),

-- Post 7 : fario d'Emilie
(11, 7, 3,  'Magnifique poisson Emilie ! La fario de l''Ain est vraiment belle avec ses points rouges.',                          NULL, 4, NULL, NOW() - INTERVAL 8 DAY + INTERVAL 2 HOUR),
(12, 7, 1,  'Quelle taille ! 55cm c''est une belle fario. Tu pêchais à quelle heure ?',                                           NULL, 2, NULL, NOW() - INTERVAL 8 DAY + INTERVAL 4 HOUR),
(13, 7, 4,  'Vers 8h du matin. L''activité était intense, les éclosions de trichoptères créaient une vraie tempête de surface.',   12,   3, NULL, NOW() - INTERVAL 8 DAY + INTERVAL 6 HOUR),
(14, 7, 8,  'Je rêve de cette rivière. Tu organises des sorties accompagnées pour débutants ?',                                    NULL, 1, NULL, NOW() - INTERVAL 7 DAY + INTERVAL 9 HOUR),
(15, 7, 4,  'Oui ! La prochaine sortie débutants c''est le 1er du mois. Inscris-toi via le post d''organisation.',                14,   2, NULL, NOW() - INTERVAL 7 DAY + INTERVAL 10 HOUR),

-- Post 12 : brochet de Sophie dans leuristes
(16, 12,10, 'Excellent ! 5kg en surface c''est un spectacle. Quel swimbaiter exactement ?',                                        NULL, 3, NULL, NOW() - INTERVAL 19 DAY + INTERVAL 1 HOUR),
(17, 12, 2, 'Illex Giga Jointed Sardine 190mm. La meilleure imitation de poisson fourrage que j''aie jamais utilisée.',           16,   2, NULL, NOW() - INTERVAL 19 DAY + INTERVAL 2 HOUR),
(18, 12, 9, 'Je note ! Je cherchais justement un bon swimbaiter pour ce type d''attaque.',                                         16,   1, NULL, NOW() - INTERVAL 19 DAY + INTERVAL 3 HOUR);

-- =============================================================================
--  LIKES DE COMMENTAIRES
-- =============================================================================

INSERT INTO comment_likes (id, comment_id, user_id) VALUES
(1,  1,  1),  (2,  1,  7),  (3,  1,  6),
(4,  5,  2),  (5,  5,  7),  (6,  5,  6),  (7,  5, 1),
(8,  8,  2),  (9,  8,  7),  (10, 8,  6),  (11, 8, 4), (12, 8, 10),
(13, 11, 4),  (14, 11, 8),  (15, 11, 1),  (16, 11, 2),
(17, 16, 2),  (18, 16, 9),  (19, 16, 10);

-- =============================================================================
--  BADGES
-- =============================================================================

INSERT INTO badges (id, user_id, type, awarded_at) VALUES
-- Marc (24 captures, 8 espèces, 2 groupes)
(1,  1, 'FIRST_CAPTURE', NOW() - INTERVAL 120 DAY),
(2,  1, 'CAPTURE_5',     NOW() - INTERVAL 100 DAY),
(3,  1, 'CAPTURE_10',    NOW() - INTERVAL 80 DAY),
(4,  1, 'SPECIES_3',     NOW() - INTERVAL 90 DAY),
(5,  1, 'SPECIES_5',     NOW() - INTERVAL 70 DAY),
(6,  1, 'FIRST_GROUP',   NOW() - INTERVAL 110 DAY),
-- Sophie (18 captures, 3 espèces, 2 groupes)
(7,  2, 'FIRST_CAPTURE', NOW() - INTERVAL 95 DAY),
(8,  2, 'CAPTURE_5',     NOW() - INTERVAL 80 DAY),
(9,  2, 'CAPTURE_10',    NOW() - INTERVAL 65 DAY),
(10, 2, 'SPECIES_3',     NOW() - INTERVAL 75 DAY),
(11, 2, 'FIRST_GROUP',   NOW() - INTERVAL 90 DAY),
-- Emilie (31 captures, 3 espèces, 2 groupes)
(12, 4, 'FIRST_CAPTURE', NOW() - INTERVAL 85 DAY),
(13, 4, 'CAPTURE_5',     NOW() - INTERVAL 70 DAY),
(14, 4, 'CAPTURE_10',    NOW() - INTERVAL 55 DAY),
(15, 4, 'SPECIES_3',     NOW() - INTERVAL 60 DAY),
(16, 4, 'FIRST_GROUP',   NOW() - INTERVAL 75 DAY),
-- Pierre (12 captures, 3 espèces)
(17, 3, 'FIRST_CAPTURE', NOW() - INTERVAL 65 DAY),
(18, 3, 'CAPTURE_5',     NOW() - INTERVAL 55 DAY),
(19, 3, 'CAPTURE_10',    NOW() - INTERVAL 45 DAY),
(20, 3, 'SPECIES_3',     NOW() - INTERVAL 50 DAY),
(21, 3, 'FIRST_GROUP',   NOW() - INTERVAL 60 DAY),
-- Julien (9 captures)
(22, 5, 'FIRST_CAPTURE', NOW() - INTERVAL 80 DAY),
(23, 5, 'CAPTURE_5',     NOW() - INTERVAL 60 DAY),
(24, 5, 'FIRST_GROUP',   NOW() - INTERVAL 40 DAY),
-- Lucie (7 captures)
(25, 6, 'FIRST_CAPTURE', NOW() - INTERVAL 38 DAY),
(26, 6, 'CAPTURE_5',     NOW() - INTERVAL 25 DAY),
(27, 6, 'FIRST_GROUP',   NOW() - INTERVAL 28 DAY),
-- Thomas (5 captures)
(28, 7, 'FIRST_CAPTURE', NOW() - INTERVAL 28 DAY),
(29, 7, 'CAPTURE_5',     NOW() - INTERVAL 15 DAY),
(30, 7, 'FIRST_GROUP',   NOW() - INTERVAL 20 DAY),
-- Camille (3 captures)
(31, 8, 'FIRST_CAPTURE', NOW() - INTERVAL 18 DAY),
(32, 8, 'FIRST_GROUP',   NOW() - INTERVAL 15 DAY),
-- Antoine (2 captures)
(33, 9, 'FIRST_CAPTURE', NOW() - INTERVAL 10 DAY),
(34, 9, 'FIRST_GROUP',   NOW() - INTERVAL 8 DAY),
-- Chloé (15 captures)
(35, 10,'FIRST_CAPTURE', NOW() - INTERVAL 60 DAY),
(36, 10,'CAPTURE_5',     NOW() - INTERVAL 50 DAY),
(37, 10,'CAPTURE_10',    NOW() - INTERVAL 38 DAY),
(38, 10,'SPECIES_3',     NOW() - INTERVAL 45 DAY),
(39, 10,'FIRST_GROUP',   NOW() - INTERVAL 55 DAY);

-- =============================================================================
--  NOTIFICATIONS
-- =============================================================================

INSERT INTO notifications (id, recipient_id, type, `read`, actor_username, group_name, group_id, post_id, created_at) VALUES
(1,  1, 'POST_REACTION',          FALSE, 'SophieLeurres',    'Carpistes Île-de-France', 1, 1,  NOW() - INTERVAL 11 DAY + INTERVAL 1 HOUR),
(2,  1, 'POST_REACTION',          FALSE, 'ThomasCarpeur',    'Carpistes Île-de-France', 1, 1,  NOW() - INTERVAL 11 DAY + INTERVAL 2 HOUR),
(3,  1, 'POST_COMMENT',           FALSE, 'SophieLeurres',    'Carpistes Île-de-France', 1, 1,  NOW() - INTERVAL 11 DAY + INTERVAL 2 HOUR),
(4,  1, 'POST_COMMENT',           FALSE, 'ThomasCarpeur',    'Carpistes Île-de-France', 1, 1,  NOW() - INTERVAL 10 DAY + INTERVAL 8 HOUR),
(5,  2, 'POST_COMMENT',           FALSE, 'MarcCarpiste',     'Carpistes Île-de-France', 1, 2,  NOW() - INTERVAL 9 DAY + INTERVAL 1 HOUR),
(6,  4, 'POST_REACTION',          FALSE, 'PierreFluvial',    'Pêcheurs du Rhône',       2, 7,  NOW() - INTERVAL 8 DAY + INTERVAL 2 HOUR),
(7,  4, 'POST_REACTION',          FALSE, 'MarcCarpiste',     'Pêcheurs du Rhône',       2, 7,  NOW() - INTERVAL 8 DAY + INTERVAL 4 HOUR),
(8,  4, 'POST_COMMENT',           FALSE, 'PierreFluvial',    'Pêcheurs du Rhône',       2, 7,  NOW() - INTERVAL 8 DAY + INTERVAL 2 HOUR),
(9,  4, 'POST_COMMENT',           FALSE, 'MarcCarpiste',     'Pêcheurs du Rhône',       2, 7,  NOW() - INTERVAL 8 DAY + INTERVAL 4 HOUR),
(10, 4, 'COMMENT_REPLY',          FALSE, 'CamilleTruite',    'Pêcheurs du Rhône',       2, 7,  NOW() - INTERVAL 7 DAY + INTERVAL 9 HOUR),
(11, 2, 'POST_REACTION',          TRUE,  'ChloePecheMouche', 'Les Leuristes du Sud',    4, 12, NOW() - INTERVAL 19 DAY + INTERVAL 1 HOUR),
(12, 2, 'POST_COMMENT',           TRUE,  'ChloePecheMouche', 'Les Leuristes du Sud',    4, 12, NOW() - INTERVAL 19 DAY + INTERVAL 1 HOUR),
(13, 5, 'JOIN_REQUEST_ACCEPTED',  TRUE,  'JulienSilure',     'Team Silure Garonne',     3, NULL,NOW() - INTERVAL 34 DAY),
(14, 2, 'JOIN_REQUEST_ACCEPTED',  TRUE,  'SophieLeurres',    'Les Leuristes du Sud',    4, NULL,NOW() - INTERVAL 27 DAY);

-- =============================================================================
--  CONSEILS COMMUNAUTAIRES (SPECIES TIPS)
-- =============================================================================

INSERT INTO species_tips (id, species_id, user_id, content, upvote_count, created_at) VALUES
(1, 1, 1, 'Pour le brochet en été, pêchez absolument tôt le matin entre 6h et 9h ou en soirée après 19h. La chaleur le rend léthargique la journée.', 8, NOW() - INTERVAL 80 DAY),
(2, 1, 2, 'Le swimbaiter de 15-20cm imite parfaitement une ablette ou un gardon. Cherchez les points de transition entre zones herbées et eau libre.', 6, NOW() - INTERVAL 60 DAY),
(3, 1, 5, 'Après une pluie forte, les brochets remontent souvent en surface et deviennent très actifs. C''est le meilleur moment pour le top water.', 5, NOW() - INTERVAL 45 DAY),
(4, 2, 1, 'La carpe se nourrit activement tôt le matin et en soirée. La nuit peut être très productive, surtout en été quand l''eau se rafraîchit.', 12, NOW() - INTERVAL 95 DAY),
(5, 2, 7, 'Amorcer 3-4 jours avant votre session avec des bouillettes et du maïs fait une énorme différence. La carpe revient sur le spot.', 9, NOW() - INTERVAL 70 DAY),
(6, 2, 1, 'La technique du zig-rig est redoutable par forte chaleur : les carpes remontent en surface et attaquent facilement une bouillette en suspension.', 7, NOW() - INTERVAL 50 DAY),
(7, 3, 2, 'Le sandre est un pêcheur nocturne. Vos meilleures prises seront souvent entre 22h et 2h du matin.', 10, NOW() - INTERVAL 85 DAY),
(8, 3, 10,'Le drop shot reste ma technique préférée pour le sandre : posé au fond dans 4-8m d''eau, avec un shad de 7-10cm en couleur naturelle.', 8, NOW() - INTERVAL 55 DAY),
(9, 5, 5, 'Le silure aime les zones profondes sous les ponts et dans les fosses. Pêchez de nuit avec un gros vif ou des bouillettes 28mm.', 15, NOW() - INTERVAL 75 DAY),
(10,5, 5, 'Pour ferrer un silure, attendez qu''il parte clairement avant de ferrer fort avec un long mouvement du corps entier. La gueule est dure.', 11, NOW() - INTERVAL 55 DAY),
(11,6, 4, 'La truite fario déteste les ombres et les vibrations. Approchez toujours en aval, restez le plus discret possible. Un faux pas et c''est fini.', 13, NOW() - INTERVAL 90 DAY),
(12,6, 3, 'Par temps couvert, les mouches sèches fonctionnent mieux. Par soleil, les nymphes en profondeur sont plus efficaces.', 9, NOW() - INTERVAL 65 DAY),
(13,10,2, 'Le black-bass attaque tout leurre qui imite quelque chose de vivant. Top water au lever du soleil = adrénaline garantie.', 7, NOW() - INTERVAL 40 DAY);

-- =============================================================================
--  UPVOTES DES TIPS
-- =============================================================================

INSERT INTO species_tip_upvotes (id, tip_id, user_id) VALUES
(1, 1, 2), (2, 1, 3), (3, 1, 4), (4, 1, 5), (5, 1, 6), (6, 1, 7), (7, 1, 8), (8, 1, 10),
(9, 4, 2), (10,4, 3), (11,4, 4), (12,4, 5), (13,4, 6), (14,4, 7), (15,4, 8), (16,4, 9), (17,4, 10), (18,4, 6),
(19,7, 1), (20,7, 3), (21,7, 4), (22,7, 5), (23,7, 6), (24,7, 7), (25,7, 8), (26,7, 9), (27,7, 10),
(28,9, 1), (29,9, 2), (30,9, 3), (31,9, 4), (32,9, 6), (33,9, 7), (34,9, 8), (35,9, 9), (36,9, 10),
(37,11,1), (38,11,2), (39,11,3), (40,11,5), (41,11,6), (42,11,7), (43,11,8), (44,11,9), (45,11,10);

-- =============================================================================
--  POST_COUNT — mise à jour des compteurs de posts
-- =============================================================================

UPDATE fishing_groups SET post_count = (SELECT COUNT(*) FROM posts WHERE group_id = fishing_groups.id);

-- =============================================================================
--  CAPTURE_COUNT — synchronisation des compteurs utilisateurs
-- =============================================================================

UPDATE users SET capture_count = (SELECT COUNT(*) FROM captures WHERE user_id = users.id);

-- =============================================================================
--  FIN DU SEED
-- =============================================================================

SELECT CONCAT('✅ Seed beta terminé — ', COUNT(*), ' utilisateurs insérés') AS résultat FROM users;
SELECT CONCAT('   ', COUNT(*), ' captures') AS résultat FROM captures;
SELECT CONCAT('   ', COUNT(*), ' posts') AS résultat FROM posts;
SELECT CONCAT('   ', COUNT(*), ' commentaires') AS résultat FROM post_comments;
SELECT CONCAT('   ', COUNT(*), ' groupes') AS résultat FROM fishing_groups;
SELECT CONCAT('   ', COUNT(*), ' espèces') AS résultat FROM species;
SELECT CONCAT('   ', COUNT(*), ' badges') AS résultat FROM badges;
SELECT CONCAT('   ', COUNT(*), ' tips communautaires') AS résultat FROM species_tips;
