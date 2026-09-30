-- ============================================================================
--  G-SERVICES — V25 : spécialité métier du fournisseur de services
--
--  Règle de gestion : un fournisseur (profil PRESTATAIRE) exerce dans UNE et
--  UNE SEULE catégorie de services. Conséquences :
--   - il ne peut être désigné propriétaire que de services de cette catégorie ;
--   - son espace d'administration du Catalogue est restreint à cette catégorie
--     (il n'a aucun accès aux autres) ;
--   - il peut y ajouter / gérer ses propres offres (catalogues, produits,
--     articles) — d'où l'octroi de la permission « créer » (4002).
-- ============================================================================

-- ---------------------------------------------------- colonne spécialité -----
ALTER TABLE personne ADD COLUMN IF NOT EXISTS id_categorie_specialite BIGINT;

ALTER TABLE personne DROP CONSTRAINT IF EXISTS fk_personne_categorie_specialite;
ALTER TABLE personne ADD CONSTRAINT fk_personne_categorie_specialite
    FOREIGN KEY (id_categorie_specialite) REFERENCES categorie_service (id_categorie_service)
    ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_personne_categorie_specialite
    ON personne (id_categorie_specialite);

-- ---------------------------------------------------- affectation des spécialités
--  17 fournisseurs pour 13 catégories : 2 spécialistes sur les 3 catégories les
--  plus fournies, 1 sur les 10 autres.
UPDATE personne p
SET    id_categorie_specialite = c.id_categorie_service
FROM  (VALUES
        ('presta.resto', 'Restauration'),
        ('fs.ndour',     'Restauration'),
        ('fs.diallo',    'Restauration'),
        ('fs.thiam',     'Mode & textile'),
        ('fs.ba',        'Mode & textile'),
        ('fs.diagne',    'Services à domicile'),
        ('fs.mbaye',     'Services à domicile'),
        ('fs.camara',    'Artisanat'),
        ('presta.hotel', 'Hôtellerie'),
        ('fs.faye',      'Santé & bien-être'),
        ('fs.sy',        'Automobile'),
        ('fs.gueye',     'Événementiel'),
        ('fs.sene',      'Beauté & coiffure'),
        ('fs.sow',       'Transport & logistique'),
        ('fs.diouf',     'Éducation & formation'),
        ('fs.kane',      'Informatique & digital'),
        ('fs.ndiaye2',   'Immobilier')
      ) AS v(login, categorie)
JOIN   categorie_service c ON c.libelle = v.categorie
WHERE  p.login = v.login;

-- ---------------------------------------------------- réalignement des propriétaires
--  Chaque service est (ré)attribué à un spécialiste de SA catégorie, réparti en
--  round-robin sur l'ordre des id de service — de sorte qu'aucun fournisseur ne
--  possède un service hors de sa spécialité.
WITH spec AS (
    SELECT p.id_personne,
           p.id_categorie_specialite,
           row_number() OVER (PARTITION BY p.id_categorie_specialite ORDER BY p.id_personne) - 1 AS idx,
           count(*)     OVER (PARTITION BY p.id_categorie_specialite)                          AS nb
    FROM   personne p
    WHERE  p.id_categorie_specialite IS NOT NULL AND p.etat = TRUE
),
svc AS (
    SELECT s.id_service,
           s.id_categorie_service,
           row_number() OVER (PARTITION BY s.id_categorie_service ORDER BY s.id_service) - 1 AS pos
    FROM   service s
)
UPDATE service t
SET    id_proprietaire = sp.id_personne
FROM   svc
JOIN   spec sp ON sp.id_categorie_specialite = svc.id_categorie_service
              AND sp.idx = (svc.pos % sp.nb)
WHERE  t.id_service = svc.id_service;

-- ---------------------------------------------------- permission « créer » pour le prestataire
--  Il gère désormais ses propres offres (catalogues / produits / articles) dans
--  sa catégorie. Le périmètre est contrôlé côté service (CatalogueServiceImpl).
INSERT INTO role_profil (id_profil, id_permission)
SELECT pr.id_profil, pe.id_permission
FROM   profil pr CROSS JOIN permission pe
WHERE  pr.libelle = 'PRESTATAIRE' AND pe.code IN ('4002', '4004')
ON CONFLICT (id_profil, id_permission) DO NOTHING;
