-- ============================================================================
--  G-SERVICES — V12 : permissions du module Catalogue (branche 4000)
-- ============================================================================

INSERT INTO permission (code, libelle, action_autorisee, reference, niveau, id_branche_permission)
SELECT v.code, v.libelle, v.action, '/admin/cat/catalogue.xhtml', v.niveau, b.id_branche_permission
FROM (VALUES
    ('4001', 'Consulter le catalogue',            'LIRE',      0),
    ('4002', 'Créer un élément de catalogue',      'CREER',     1),
    ('4003', 'Modifier un élément de catalogue',   'MODIFIER',  2),
    ('4004', 'Activer/désactiver (catalogue)',     'SUPPRIMER', 3),
    ('4005', 'Exporter le catalogue',              'EXPORTER',  4)
) AS v(code, libelle, action, niveau)
JOIN branche_permission b ON b.code = '4000'
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p CROSS JOIN permission pe
WHERE p.libelle = 'SUPER_ADMIN' AND pe.code IN ('4001','4002','4003','4004','4005')
ON CONFLICT (id_profil, id_permission) DO NOTHING;

INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p CROSS JOIN permission pe
WHERE p.libelle = 'ADMIN' AND pe.code IN ('4001','4005')
ON CONFLICT (id_profil, id_permission) DO NOTHING;

INSERT INTO profil_branche_permission (id_profil, id_branche_permission)
SELECT p.id_profil, b.id_branche_permission
FROM profil p JOIN branche_permission b ON b.code = '4000'
WHERE p.libelle IN ('SUPER_ADMIN', 'ADMIN')
ON CONFLICT DO NOTHING;
