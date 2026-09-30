-- ============================================================================
--  G-SERVICES — V32 : comptes de démonstration ADMIN et GESTIONNAIRE
--
--  Jusqu'ici seul `superadmin` (SUPER_ADMIN) existait comme compte d'admin —
--  impossible de tester les profils ADMIN et GESTIONNAIRE (moins de droits,
--  cf. role_profil de V2/V25/V28/V31) sans compte dédié.
-- ============================================================================

INSERT INTO personne (nom, prenom, sexe, login, password, email1, etat, valide)
VALUES
    ('Ndoye', 'Fatoumata', 'FEMME', 'admin.demo',
     '$2a$10$Cf2ORSViE/Ck2LbK6RlqrO7ga7TFDuW3yzSlzJhIvTvveklOafI2S',
     'admin.demo@gservices-demo.sn', TRUE, TRUE),
    ('Sarr', 'Moussa', 'HOMME', 'gestionnaire.demo',
     '$2a$10$Cf2ORSViE/Ck2LbK6RlqrO7ga7TFDuW3yzSlzJhIvTvveklOafI2S',
     'gestionnaire.demo@gservices-demo.sn', TRUE, TRUE)
ON CONFLICT (login) DO NOTHING;

INSERT INTO role (id_personne, id_profil, date_attribution)
SELECT p.id_personne, pr.id_profil, now()
FROM personne p JOIN profil pr ON pr.libelle = 'ADMIN'
WHERE p.login = 'admin.demo'
ON CONFLICT (id_personne, id_profil) DO NOTHING;

INSERT INTO role (id_personne, id_profil, date_attribution)
SELECT p.id_personne, pr.id_profil, now()
FROM personne p JOIN profil pr ON pr.libelle = 'GESTIONNAIRE'
WHERE p.login = 'gestionnaire.demo'
ON CONFLICT (id_personne, id_profil) DO NOTHING;
