-- ============================================================================
--  G-SERVICES — V18 : permissions de modération des avis (branche 6000)
-- ============================================================================

INSERT INTO permission (code, libelle, action_autorisee, reference, niveau, id_branche_permission)
SELECT v.code, v.libelle, v.action, '/admin/avis/moderation.xhtml', v.niveau, b.id_branche_permission
FROM (VALUES
    ('6001', 'Consulter les avis',         'LIRE',       0),
    ('6002', 'Modérer les avis',            'MODIFIER',   1),
    ('6003', 'Répondre aux avis',           'ADMINISTRER',2),
    ('6004', 'Activer/désactiver un avis',  'SUPPRIMER',  3),
    ('6005', 'Exporter les avis',           'EXPORTER',   4)
) AS v(code, libelle, action, niveau)
JOIN branche_permission b ON b.code = '6000'
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p CROSS JOIN permission pe
WHERE p.libelle = 'SUPER_ADMIN' AND pe.code IN ('6001','6002','6003','6004','6005')
ON CONFLICT (id_profil, id_permission) DO NOTHING;

INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p CROSS JOIN permission pe
WHERE p.libelle IN ('ADMIN','GESTIONNAIRE') AND pe.code IN ('6001','6002','6003','6005')
ON CONFLICT (id_profil, id_permission) DO NOTHING;

INSERT INTO profil_branche_permission (id_profil, id_branche_permission)
SELECT p.id_profil, b.id_branche_permission
FROM profil p JOIN branche_permission b ON b.code = '6000'
WHERE p.libelle IN ('SUPER_ADMIN', 'ADMIN', 'GESTIONNAIRE')
ON CONFLICT DO NOTHING;
