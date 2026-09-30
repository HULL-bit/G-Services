-- ============================================================================
--  G-SERVICES — V9 : permissions du module Géographie & temps (branche 3000)
-- ============================================================================

INSERT INTO permission (code, libelle, action_autorisee, reference, niveau, id_branche_permission)
SELECT v.code, v.libelle, v.action, v.ref, v.niveau, b.id_branche_permission
FROM (VALUES
    ('3001', 'Consulter la géographie',         'LIRE',        '/admin/geo/geographie.xhtml', 0),
    ('3002', 'Créer un élément géographique',    'CREER',       '/admin/geo/geographie.xhtml', 1),
    ('3003', 'Modifier un élément géographique', 'MODIFIER',    '/admin/geo/geographie.xhtml', 2),
    ('3004', 'Activer/désactiver (géographie)',  'SUPPRIMER',   '/admin/geo/geographie.xhtml', 3),
    ('3005', 'Exporter la géographie',           'EXPORTER',    '/admin/geo/geographie.xhtml', 4),
    ('3006', 'Consulter les jours, mois et années', 'LIRE',     '/admin/geo/temps.xhtml',      5),
    ('3007', 'Gérer le référentiel temps',       'ADMINISTRER', '/admin/geo/temps.xhtml',      6)
) AS v(code, libelle, action, ref, niveau)
JOIN branche_permission b ON b.code = '3000'
ON CONFLICT (code) DO NOTHING;

-- SUPER_ADMIN : toutes
INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p CROSS JOIN permission pe
WHERE p.libelle = 'SUPER_ADMIN' AND pe.code IN ('3001','3002','3003','3004','3005','3006','3007')
ON CONFLICT (id_profil, id_permission) DO NOTHING;

-- ADMIN : lecture + export
INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p CROSS JOIN permission pe
WHERE p.libelle = 'ADMIN' AND pe.code IN ('3001','3005','3006')
ON CONFLICT (id_profil, id_permission) DO NOTHING;

INSERT INTO profil_branche_permission (id_profil, id_branche_permission)
SELECT p.id_profil, b.id_branche_permission
FROM profil p JOIN branche_permission b ON b.code = '3000'
WHERE p.libelle IN ('SUPER_ADMIN', 'ADMIN')
ON CONFLICT DO NOTHING;
