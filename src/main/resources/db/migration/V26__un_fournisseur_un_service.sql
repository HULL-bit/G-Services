-- ============================================================================
--  G-SERVICES — V26 : « 1 fournisseur = 1 service »
--
--  Règle de gestion : un fournisseur est le plus spécialisé possible — il gère
--  UN SEUL service (son activité) et ses éventuels sous-services. Sa catégorie
--  de spécialité est déduite de ce service.
--
--  Réattribution complète des données de démo :
--   0. on repart d'une table rase (aucun propriétaire) ;
--   1. `presta.resto` / `presta.hotel` gardent leur service phare ;
--   2. les 15 comptes `fs.*` prennent chacun un service racine ;
--   3. un compte `f.svc<id>` est créé pour chaque service racine restant ;
--   4. la spécialité de chaque fournisseur = la catégorie de son service ;
--   5. les sous-services héritent du propriétaire de leur parent.
-- ============================================================================

-- 0. table rase
UPDATE service SET id_proprietaire = NULL;

-- 1. services phares
UPDATE service SET id_proprietaire = (SELECT id_personne FROM personne WHERE login = 'presta.resto')
WHERE libelle = 'Restaurant traditionnel';
UPDATE service SET id_proprietaire = (SELECT id_personne FROM personne WHERE login = 'presta.hotel')
WHERE libelle = 'Hôtel Teranga';

-- 2. 15 comptes fs.* -> 15 services racine libres (ordre stable par id)
WITH libres AS (
    SELECT s.id_service, row_number() OVER (ORDER BY s.id_service) AS rn
    FROM service s
    WHERE s.etat AND s.id_service_parent IS NULL AND s.id_proprietaire IS NULL
),
fs AS (
    SELECT p.id_personne, row_number() OVER (ORDER BY p.login) AS rn
    FROM personne p WHERE p.login LIKE 'fs.%'
)
UPDATE service t
SET id_proprietaire = fs.id_personne
FROM libres JOIN fs ON fs.rn = libres.rn
WHERE t.id_service = libres.id_service;

-- 3. un compte dédié par service racine encore libre
WITH restants AS (
    SELECT s.id_service, s.id_categorie_service,
           row_number() OVER (ORDER BY s.id_service) AS rn
    FROM service s
    WHERE s.etat AND s.id_service_parent IS NULL AND s.id_proprietaire IS NULL
),
pool_idx AS (
    SELECT nom, prenom, row_number() OVER () AS i
    FROM unnest(
        ARRAY['Diop','Ndiaye','Fall','Sarr','Ba','Sy','Sow','Faye','Gueye','Kane','Mbaye',
              'Diagne','Thiam','Toure','Camara','Cisse','Diallo','Ndour','Sene','Diouf',
              'Gomis','Wade','Ndao','Diakhate','Barry','Balde','Sagna','Badji','Mane','Coly'],
        ARRAY['Amadou','Fatou','Ousmane','Aïssatou','Cheikh','Mariama','Ibrahima','Awa','Modou',
              'Khady','Pape','Sokhna','Moussa','Bineta','Alioune','Coumba','Serigne','Rama',
              'Babacar','Ndeye','Malick','Aida','Souleymane','Adja','Lamine','Yacine',
              'Abdoulaye','Nafissatou','Mamadou','Dieynaba']
    ) AS t(nom, prenom)
)
INSERT INTO personne (nom, prenom, sexe, login, password, email1, etat, id_categorie_specialite)
SELECT pi.nom, pi.prenom,
       CASE WHEN r.rn % 2 = 0 THEN 'FEMME' ELSE 'HOMME' END,
       'f.svc' || r.id_service,
       '$2a$10$Cf2ORSViE/Ck2LbK6RlqrO7ga7TFDuW3yzSlzJhIvTvveklOafI2S',
       'f.svc' || r.id_service || '@gservices-demo.sn',
       TRUE,
       r.id_categorie_service
FROM restants r
JOIN pool_idx pi ON pi.i = 1 + ((r.rn - 1) % 30)
WHERE NOT EXISTS (SELECT 1 FROM personne p WHERE p.login = 'f.svc' || r.id_service);

INSERT INTO role (id_personne, id_profil, date_attribution)
SELECT p.id_personne, pr.id_profil, now()
FROM personne p JOIN profil pr ON pr.libelle = 'PRESTATAIRE'
WHERE p.login LIKE 'f.svc%'
ON CONFLICT (id_personne, id_profil) DO NOTHING;

UPDATE service s
SET id_proprietaire = p.id_personne
FROM personne p
WHERE p.login = 'f.svc' || s.id_service
  AND s.id_service_parent IS NULL
  AND s.id_proprietaire IS NULL;

-- 4. spécialité = catégorie de l'unique service racine possédé
UPDATE personne p
SET id_categorie_specialite = s.id_categorie_service
FROM service s
WHERE s.id_proprietaire = p.id_personne AND s.id_service_parent IS NULL;

-- 5. sous-services -> propriétaire du parent
UPDATE service s
SET id_proprietaire = par.id_proprietaire
FROM service par
WHERE par.id_service = s.id_service_parent AND par.id_proprietaire IS NOT NULL;

-- 6. filet : fournisseur sans service -> désactivé
UPDATE personne p
SET etat = FALSE
WHERE p.id_categorie_specialite IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM service s WHERE s.id_proprietaire = p.id_personne);
