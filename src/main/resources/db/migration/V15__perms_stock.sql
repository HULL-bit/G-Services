-- ============================================================================
--  G-SERVICES — V15 : permissions du module Stock & prestataires (branche 5000)
-- ============================================================================

INSERT INTO permission (code, libelle, action_autorisee, reference, niveau, id_branche_permission)
SELECT v.code, v.libelle, v.action, '/admin/stock/stock.xhtml', v.niveau, b.id_branche_permission
FROM (VALUES
    ('5001', 'Consulter stock & prestataires', 'LIRE',      0),
    ('5002', 'Ouvrir un stock / créer une fiche', 'CREER',   1),
    ('5003', 'Mouvementer le stock / éditer une fiche', 'MODIFIER', 2),
    ('5004', 'Activer/désactiver (stock)',      'SUPPRIMER', 3),
    ('5005', 'Exporter le stock',               'EXPORTER',  4)
) AS v(code, libelle, action, niveau)
JOIN branche_permission b ON b.code = '5000'
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p CROSS JOIN permission pe
WHERE p.libelle = 'SUPER_ADMIN' AND pe.code IN ('5001','5002','5003','5004','5005')
ON CONFLICT (id_profil, id_permission) DO NOTHING;

INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p CROSS JOIN permission pe
WHERE p.libelle = 'GESTIONNAIRE' AND pe.code IN ('5001','5002','5003','5005')
ON CONFLICT (id_profil, id_permission) DO NOTHING;

INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p CROSS JOIN permission pe
WHERE p.libelle = 'ADMIN' AND pe.code IN ('5001','5005')
ON CONFLICT (id_profil, id_permission) DO NOTHING;

INSERT INTO profil_branche_permission (id_profil, id_branche_permission)
SELECT p.id_profil, b.id_branche_permission
FROM profil p JOIN branche_permission b ON b.code = '5000'
WHERE p.libelle IN ('SUPER_ADMIN', 'ADMIN', 'GESTIONNAIRE')
ON CONFLICT DO NOTHING;
