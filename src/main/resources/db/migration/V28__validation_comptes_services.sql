-- ============================================================================
--  G-SERVICES — V28 : validation des comptes et des services par l'administrateur
--
--   - `personne.valide`  : un compte auto-inscrit (client OU fournisseur) reste
--     en attente tant qu'un administrateur ne l'a pas validé — il ne peut pas
--     se connecter avant.
--   - `service.valide`   : un service créé par un fournisseur n'est visible sur
--     le front public qu'une fois validé par un administrateur.
--   - Branche 8000 « Fournisseurs & clients » : écrans dédiés d'administration
--     (liste fournisseurs / clients, création de comptes, file de validation).
-- ============================================================================

ALTER TABLE personne ADD COLUMN IF NOT EXISTS valide BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE service  ADD COLUMN IF NOT EXISTS valide BOOLEAN NOT NULL DEFAULT TRUE;

-- ---------------------------------------------------- branche + permissions --
INSERT INTO branche_permission (code, libelle, icone, niveau, etat)
SELECT '8000', 'Fournisseurs & clients', 'pi pi-briefcase', 8, TRUE
WHERE NOT EXISTS (SELECT 1 FROM branche_permission WHERE code = '8000');

INSERT INTO permission (code, libelle, action_autorisee, reference, niveau, id_branche_permission)
SELECT v.code, v.libelle, v.action, v.ref, v.niveau, b.id_branche_permission
FROM (VALUES
    ('8001', 'Consulter les fournisseurs',    'LIRE',        '/admin/sec/fournisseurs.xhtml', 0),
    ('8002', 'Gérer les fournisseurs',         'ADMINISTRER', '/admin/sec/fournisseurs.xhtml', 1),
    ('8003', 'Consulter les clients',          'LIRE',        '/admin/sec/clients.xhtml',      2),
    ('8004', 'Gérer les clients',              'ADMINISTRER', '/admin/sec/clients.xhtml',      3),
    ('8005', 'Consulter la file de validation','LIRE',        '/admin/sec/validations.xhtml',  4),
    ('8006', 'Valider comptes et services',    'ADMINISTRER', '/admin/sec/validations.xhtml',  5)
) AS v(code, libelle, action, ref, niveau)
JOIN branche_permission b ON b.code = '8000'
ON CONFLICT (code) DO NOTHING;

-- SUPER_ADMIN + ADMIN : tout ; GESTIONNAIRE : lecture seule
INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p CROSS JOIN permission pe
WHERE p.libelle IN ('SUPER_ADMIN', 'ADMIN')
  AND pe.code IN ('8001','8002','8003','8004','8005','8006')
ON CONFLICT (id_profil, id_permission) DO NOTHING;

INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p CROSS JOIN permission pe
WHERE p.libelle = 'GESTIONNAIRE' AND pe.code IN ('8001','8003','8005')
ON CONFLICT (id_profil, id_permission) DO NOTHING;

INSERT INTO profil_branche_permission (id_profil, id_branche_permission)
SELECT p.id_profil, b.id_branche_permission
FROM profil p JOIN branche_permission b ON b.code = '8000'
WHERE p.libelle IN ('SUPER_ADMIN', 'ADMIN', 'GESTIONNAIRE')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------- démo : file d'attente ---
--  Un client et un fournisseur auto-inscrits en attente + un service à valider.
INSERT INTO personne (nom, prenom, sexe, login, password, email1, etat, valide)
VALUES
    ('Sène', 'Mareme', 'FEMME', 'client.attente',
     '$2a$10$Cf2ORSViE/Ck2LbK6RlqrO7ga7TFDuW3yzSlzJhIvTvveklOafI2S',
     'mareme.sene@example.sn', TRUE, FALSE),
    ('Dieng', ' Malick', 'HOMME', 'fournisseur.attente',
     '$2a$10$Cf2ORSViE/Ck2LbK6RlqrO7ga7TFDuW3yzSlzJhIvTvveklOafI2S',
     'malick.dieng@example.sn', TRUE, FALSE)
ON CONFLICT (login) DO NOTHING;

UPDATE personne SET prenom = 'Malick' WHERE login = 'fournisseur.attente';

INSERT INTO role (id_personne, id_profil, date_attribution)
SELECT p.id_personne, pr.id_profil, now()
FROM personne p JOIN profil pr ON pr.libelle = 'CLIENT'
WHERE p.login = 'client.attente'
ON CONFLICT (id_personne, id_profil) DO NOTHING;

INSERT INTO role (id_personne, id_profil, date_attribution)
SELECT p.id_personne, pr.id_profil, now()
FROM personne p JOIN profil pr ON pr.libelle = 'PRESTATAIRE'
WHERE p.login = 'fournisseur.attente'
ON CONFLICT (id_personne, id_profil) DO NOTHING;

UPDATE personne
SET id_categorie_specialite = (SELECT id_categorie_service FROM categorie_service WHERE libelle = 'Restauration')
WHERE login = 'fournisseur.attente';

-- service à valider, rattaché au fournisseur en attente
INSERT INTO service (libelle, description, prix_indicatif, id_categorie_service, id_proprietaire, valide, etat)
SELECT 'Snack Le Petit Dej', 'Petit-déjeuner et sandwichs — service en attente de validation.', 2500,
       (SELECT id_categorie_service FROM categorie_service WHERE libelle = 'Restauration'),
       (SELECT id_personne FROM personne WHERE login = 'fournisseur.attente'),
       FALSE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM service WHERE libelle = 'Snack Le Petit Dej');
