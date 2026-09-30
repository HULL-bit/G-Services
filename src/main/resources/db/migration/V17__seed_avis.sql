-- ============================================================================
--  G-SERVICES — V17 : jeu de démonstration Avis (Lot 5)
--  Comptes visiteurs de démonstration — mot de passe BCrypt de « password ».
--  (À supprimer / changer hors démonstration.)
-- ============================================================================

INSERT INTO personne (nom, prenom, sexe, login, password, email1, etat)
VALUES
    ('Diop',   'Awa',   'FEMME', 'awa.diop',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'awa.diop@example.sn',   TRUE),
    ('Ba',     'Karim', 'HOMME', 'karim.ba',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'karim.ba@example.sn',   TRUE),
    ('Ndiaye', 'Marie', 'FEMME', 'marie.ndiaye','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'marie.ndiaye@example.sn',TRUE)
ON CONFLICT (login) DO NOTHING;

-- ---------------------------------------------------- avis sur des services --
INSERT INTO avis (note, commentaire, reponse_prestataire, est_modere, id_personne, id_service, id_annee)
SELECT a.note, a.com, a.rep, a.mod, p.id_personne, s.id_service, y.id_annee
FROM (VALUES
    (5, 'Thiéboudiène exceptionnelle, service rapide et souriant.', NULL,
        TRUE, 'awa.diop', 'Restaurant traditionnel'),
    (4, 'Très bon accueil, un peu d''attente le midi.',
        'Merci Karim ! Nous renforçons l''équipe aux heures de pointe.',
        TRUE, 'karim.ba', 'Restaurant traditionnel'),
    (5, 'Retouches impeccables, délais tenus.', NULL,
        FALSE, 'marie.ndiaye', 'Atelier de couture')
) AS a(note, com, rep, mod, login, srv)
JOIN personne p ON p.login = a.login
JOIN service s ON s.libelle = a.srv
JOIN annee y ON y.valeur_annee = 2026
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------- avis sur des produits --
INSERT INTO avis (note, commentaire, est_modere, id_personne, id_produit, id_annee)
SELECT a.note, a.com, a.mod, p.id_personne, pr.id_produit, y.id_annee
FROM (VALUES
    (4, 'Bissap maison très rafraîchissant, pas trop sucré.', TRUE,  'awa.diop',  'Jus de bissap'),
    (3, 'Correct sans plus, portion un peu juste.',           FALSE, 'karim.ba',  'Plat du jour')
) AS a(note, com, mod, login, prod)
JOIN personne p ON p.login = a.login
JOIN produit pr ON pr.libelle = a.prod
JOIN annee y ON y.valeur_annee = 2026
ON CONFLICT DO NOTHING;
