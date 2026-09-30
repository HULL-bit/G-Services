-- ============================================================================
--  G-SERVICES — V4 : entrées de menu pour les classes d'association Role /
--  RoleProfil (Lot 1B) + rattachement au profil SUPER_ADMIN.
-- ============================================================================

INSERT INTO permission (code, libelle, action_autorisee, reference, niveau, id_branche_permission)
SELECT v.code, v.libelle, 'LIRE', v.reference, v.niveau, b.id_branche_permission
FROM (VALUES
    ('ROLE_LIRE',       'Consulter les rôles (Personne × Profil)',   '/admin/sec/role.xhtml',       5),
    ('ROLEPROFIL_LIRE', 'Consulter les droits des profils',          '/admin/sec/roleprofil.xhtml', 6)
) AS v(code, libelle, reference, niveau)
JOIN branche_permission b ON b.code = 'ADMINISTRATION'
ON CONFLICT (code) DO NOTHING;

-- SUPER_ADMIN reçoit ces permissions
INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p
JOIN permission pe ON pe.code IN ('ROLE_LIRE', 'ROLEPROFIL_LIRE')
WHERE p.libelle = 'SUPER_ADMIN'
ON CONFLICT (id_profil, id_permission) DO NOTHING;

-- ADMIN (lecture) les reçoit aussi
INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p
JOIN permission pe ON pe.code IN ('ROLE_LIRE', 'ROLEPROFIL_LIRE')
WHERE p.libelle = 'ADMIN'
ON CONFLICT (id_profil, id_permission) DO NOTHING;
