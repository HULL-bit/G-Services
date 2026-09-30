-- ============================================================================
--  G-SERVICES — V3 : données de référence RBAC (Lot 1)
--  Branches de menu, permissions fines, profils, et affectations du profil
--  SUPER_ADMIN (toutes les branches + toutes les permissions).
--  Le compte SUPER_ADMIN (Personne + Role) est créé au démarrage par
--  SecuriteDataInitializer (mot de passe BCrypt configurable).
-- ============================================================================

-- ----------------------------------------------------- branches de menu -----
INSERT INTO branche_permission (code, libelle, icone, reference, niveau) VALUES
    ('TABLEAU_BORD',   'Tableau de bord',        'pi pi-chart-bar',   '/admin/index.xhtml',            0),
    ('ADMINISTRATION', 'Utilisateurs & sécurité','pi pi-shield',      '/admin/sec/personne.xhtml',     10),
    ('REFERENTIEL_GEO','Géographie & temps',     'pi pi-map',          '/admin/geo/continent.xhtml',    20),
    ('CATALOGUE',      'Catalogue',              'pi pi-sitemap',      '/admin/cat/categorie.xhtml',    30),
    ('STOCK',          'Stock & prestataires',   'pi pi-box',          '/admin/stock/stock.xhtml',      40),
    ('MODERATION',     'Avis & modération',      'pi pi-comments',     '/admin/avis/avis.xhtml',        50)
ON CONFLICT (code) DO NOTHING;

-- ----------------------------------------------------- permissions ----------
--  Convention de code : <DOMAINE>_<ACTION>. action_autorisee ∈
--  {LIRE, CREER, MODIFIER, SUPPRIMER, EXPORTER, ADMINISTRER}.
INSERT INTO permission (code, libelle, action_autorisee, reference, niveau, id_branche_permission)
SELECT v.code, v.libelle, v.action, v.reference, v.niveau, b.id_branche_permission
FROM (VALUES
    ('DASHBOARD_LIRE',   'Consulter le tableau de bord', 'LIRE',      '/admin/index.xhtml',        0,  'TABLEAU_BORD'),

    ('PERSONNE_LIRE',     'Consulter les personnes',     'LIRE',      '/admin/sec/personne.xhtml',  0,  'ADMINISTRATION'),
    ('PERSONNE_CREER',    'Créer une personne',          'CREER',     '/admin/sec/personne.xhtml',  1,  'ADMINISTRATION'),
    ('PERSONNE_MODIFIER', 'Modifier une personne',       'MODIFIER',  '/admin/sec/personne.xhtml',  2,  'ADMINISTRATION'),
    ('PERSONNE_SUPPRIMER','Activer/désactiver une personne','SUPPRIMER','/admin/sec/personne.xhtml', 3,  'ADMINISTRATION'),
    ('PERSONNE_EXPORTER', 'Exporter les personnes',      'EXPORTER',  '/admin/sec/personne.xhtml',  4,  'ADMINISTRATION'),

    ('PROFIL_LIRE',       'Consulter les profils',       'LIRE',      '/admin/sec/profil.xhtml',    0,  'ADMINISTRATION'),
    ('PROFIL_CREER',      'Créer un profil',             'CREER',     '/admin/sec/profil.xhtml',    1,  'ADMINISTRATION'),
    ('PROFIL_MODIFIER',   'Modifier un profil',          'MODIFIER',  '/admin/sec/profil.xhtml',    2,  'ADMINISTRATION'),
    ('PROFIL_SUPPRIMER',  'Activer/désactiver un profil','SUPPRIMER', '/admin/sec/profil.xhtml',    3,  'ADMINISTRATION'),
    ('PROFIL_EXPORTER',   'Exporter les profils',        'EXPORTER',  '/admin/sec/profil.xhtml',    4,  'ADMINISTRATION'),
    ('PROFIL_AFFECTER',   'Affecter permissions/branches','ADMINISTRER','/admin/sec/profil.xhtml',   5,  'ADMINISTRATION'),

    ('PERMISSION_LIRE',    'Consulter les permissions',  'LIRE',      '/admin/sec/permission.xhtml', 0,  'ADMINISTRATION'),
    ('PERMISSION_CREER',   'Créer une permission',       'CREER',     '/admin/sec/permission.xhtml', 1,  'ADMINISTRATION'),
    ('PERMISSION_MODIFIER','Modifier une permission',    'MODIFIER',  '/admin/sec/permission.xhtml', 2,  'ADMINISTRATION'),
    ('PERMISSION_SUPPRIMER','Activer/désactiver',        'SUPPRIMER', '/admin/sec/permission.xhtml', 3,  'ADMINISTRATION'),
    ('PERMISSION_EXPORTER','Exporter les permissions',   'EXPORTER',  '/admin/sec/permission.xhtml', 4,  'ADMINISTRATION'),

    ('BRANCHE_LIRE',      'Consulter les branches',      'LIRE',      '/admin/sec/branche.xhtml',   0,  'ADMINISTRATION'),
    ('BRANCHE_CREER',     'Créer une branche',           'CREER',     '/admin/sec/branche.xhtml',   1,  'ADMINISTRATION'),
    ('BRANCHE_MODIFIER',  'Modifier une branche',        'MODIFIER',  '/admin/sec/branche.xhtml',   2,  'ADMINISTRATION'),
    ('BRANCHE_SUPPRIMER', 'Activer/désactiver',          'SUPPRIMER', '/admin/sec/branche.xhtml',   3,  'ADMINISTRATION'),
    ('BRANCHE_EXPORTER',  'Exporter les branches',       'EXPORTER',  '/admin/sec/branche.xhtml',   4,  'ADMINISTRATION')
) AS v(code, libelle, action, reference, niveau, branche)
JOIN branche_permission b ON b.code = v.branche
ON CONFLICT (code) DO NOTHING;

-- ----------------------------------------------------- profils --------------
INSERT INTO profil (libelle, description, niveau) VALUES
    ('SUPER_ADMIN',  'Accès total à la plateforme',                     0),
    ('ADMIN',        'Administration fonctionnelle (hors sécurité fine)',10),
    ('GESTIONNAIRE', 'Gestion courante du catalogue et du stock',       50)
ON CONFLICT (libelle) DO NOTHING;

-- ------------------------------ SUPER_ADMIN : toutes les permissions --------
INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p
CROSS JOIN permission pe
WHERE p.libelle = 'SUPER_ADMIN'
ON CONFLICT (id_profil, id_permission) DO NOTHING;

-- ------------------------------ SUPER_ADMIN : toutes les branches -----------
INSERT INTO profil_branche_permission (id_profil, id_branche_permission)
SELECT p.id_profil, b.id_branche_permission
FROM profil p
CROSS JOIN branche_permission b
WHERE p.libelle = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ------------------------------ ADMIN : lecture partout + gestion non-sécurité
INSERT INTO role_profil (id_profil, id_permission)
SELECT p.id_profil, pe.id_permission
FROM profil p
JOIN permission pe ON pe.action_autorisee IN ('LIRE', 'EXPORTER')
WHERE p.libelle = 'ADMIN'
ON CONFLICT (id_profil, id_permission) DO NOTHING;

INSERT INTO profil_branche_permission (id_profil, id_branche_permission)
SELECT p.id_profil, b.id_branche_permission
FROM profil p
JOIN branche_permission b ON b.code IN ('TABLEAU_BORD', 'ADMINISTRATION')
WHERE p.libelle = 'ADMIN'
ON CONFLICT DO NOTHING;
