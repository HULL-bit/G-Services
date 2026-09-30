-- ============================================================================
--  G-SERVICES — V5 : codes numériques hiérarchiques + page sécurité consolidée
--
--  BranchePermission.code : multiple de 1000 (1000, 2000, 3000...).
--  Permission.code        : <code branche> + rang dans la branche
--                           (branche 2000 -> 2001, 2002, ... 2099).
--  Les permissions de lecture du domaine sécurité pointent toutes vers la page
--  unique /admin/sec/securite.xhtml.
-- ============================================================================

-- ---- Branches : renumérotation ----
UPDATE branche_permission SET code = '1000' WHERE code = 'TABLEAU_BORD';
UPDATE branche_permission SET code = '2000' WHERE code = 'ADMINISTRATION';
UPDATE branche_permission SET code = '3000' WHERE code = 'REFERENTIEL_GEO';
UPDATE branche_permission SET code = '4000' WHERE code = 'CATALOGUE';
UPDATE branche_permission SET code = '5000' WHERE code = 'STOCK';
UPDATE branche_permission SET code = '6000' WHERE code = 'MODERATION';

-- ---- Permissions : mapping sémantique -> numérique + référence de vue ----
UPDATE permission AS p SET code = m.neuf, reference = m.ref
FROM (VALUES
    ('DASHBOARD_LIRE',    '1001', '/admin/index.xhtml'),

    ('PERSONNE_LIRE',     '2001', '/admin/sec/securite.xhtml'),
    ('PERSONNE_CREER',    '2002', '/admin/sec/securite.xhtml'),
    ('PERSONNE_MODIFIER', '2003', '/admin/sec/securite.xhtml'),
    ('PERSONNE_SUPPRIMER','2004', '/admin/sec/securite.xhtml'),
    ('PERSONNE_EXPORTER', '2005', '/admin/sec/securite.xhtml'),

    ('PROFIL_LIRE',       '2011', '/admin/sec/securite.xhtml'),
    ('PROFIL_CREER',      '2012', '/admin/sec/securite.xhtml'),
    ('PROFIL_MODIFIER',   '2013', '/admin/sec/securite.xhtml'),
    ('PROFIL_SUPPRIMER',  '2014', '/admin/sec/securite.xhtml'),
    ('PROFIL_EXPORTER',   '2015', '/admin/sec/securite.xhtml'),
    ('PROFIL_AFFECTER',   '2016', '/admin/sec/securite.xhtml'),

    ('PERMISSION_LIRE',    '2021', '/admin/sec/securite.xhtml'),
    ('PERMISSION_CREER',   '2022', '/admin/sec/securite.xhtml'),
    ('PERMISSION_MODIFIER','2023', '/admin/sec/securite.xhtml'),
    ('PERMISSION_SUPPRIMER','2024','/admin/sec/securite.xhtml'),
    ('PERMISSION_EXPORTER','2025', '/admin/sec/securite.xhtml'),

    ('BRANCHE_LIRE',      '2031', '/admin/sec/securite.xhtml'),
    ('BRANCHE_CREER',     '2032', '/admin/sec/securite.xhtml'),
    ('BRANCHE_MODIFIER',  '2033', '/admin/sec/securite.xhtml'),
    ('BRANCHE_SUPPRIMER', '2034', '/admin/sec/securite.xhtml'),
    ('BRANCHE_EXPORTER',  '2035', '/admin/sec/securite.xhtml'),

    ('ROLE_LIRE',         '2041', '/admin/sec/securite.xhtml'),
    ('ROLEPROFIL_LIRE',   '2042', '/admin/sec/securite.xhtml')
) AS m(ancien, neuf, ref)
WHERE p.code = m.ancien;

-- Les futurs codes sont attribués automatiquement par la couche service
-- (prochain multiple de 1000 pour une branche ; code branche + rang pour une
--  permission).
