-- ============================================================================
--  G-SERVICES — V20 : acteurs « client » et « propriétaire de service »
--
--  Concrétise l'association « paramètre (admin) » du diagramme :
--  Personne (1) ── paramètre ──> (n) Service   → colonne service.id_proprietaire.
--
--  - CLIENT      : visiteur inscrit (dépose des avis, aucun accès back-office).
--  - PRESTATAIRE : propriétaire d'un ou plusieurs services ; gère son offre et
--                  répond aux avis publiés sur SES services.
-- ============================================================================

-- ---------------------------------------------------- profils ---------------
INSERT INTO profil (libelle, description, niveau, date)
SELECT v.libelle, v.descr, v.niveau, now()
FROM (VALUES
    ('PRESTATAIRE', 'Propriétaire de services : gère son offre et répond aux avis', 30),
    ('CLIENT',      'Visiteur inscrit : dépose et suit ses avis',                    90)
) AS v(libelle, descr, niveau)
WHERE NOT EXISTS (SELECT 1 FROM profil p WHERE p.libelle = v.libelle);

-- ---------------------------------------------------- permission « répondre à ses avis »
INSERT INTO permission (code, libelle, action_autorisee, reference, niveau, id_branche_permission)
SELECT '6006', 'Répondre aux avis de ses propres services', 'ADMINISTRER',
       '/public/service.xhtml', 5, b.id_branche_permission
FROM branche_permission b WHERE b.code = '6000'
ON CONFLICT (code) DO NOTHING;

-- PRESTATAIRE : lecture/écriture de son catalogue + stock, réponse à ses avis
INSERT INTO role_profil (id_profil, id_permission)
SELECT pr.id_profil, pe.id_permission
FROM profil pr CROSS JOIN permission pe
WHERE pr.libelle = 'PRESTATAIRE'
  AND pe.code IN ('4001', '4003', '5001', '5003', '6001', '6006')
ON CONFLICT (id_profil, id_permission) DO NOTHING;

INSERT INTO profil_branche_permission (id_profil, id_branche_permission)
SELECT pr.id_profil, b.id_branche_permission
FROM profil pr JOIN branche_permission b ON b.code IN ('4000', '5000', '6000')
WHERE pr.libelle = 'PRESTATAIRE'
ON CONFLICT DO NOTHING;

-- SUPER_ADMIN reçoit aussi 6006
INSERT INTO role_profil (id_profil, id_permission)
SELECT pr.id_profil, pe.id_permission
FROM profil pr CROSS JOIN permission pe
WHERE pr.libelle = 'SUPER_ADMIN' AND pe.code = '6006'
ON CONFLICT (id_profil, id_permission) DO NOTHING;

-- ---------------------------------------------------- colonne propriétaire --
ALTER TABLE service ADD COLUMN IF NOT EXISTS id_proprietaire BIGINT;
ALTER TABLE service ADD CONSTRAINT fk_service_proprietaire
    FOREIGN KEY (id_proprietaire) REFERENCES personne (id_personne) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_service_proprietaire ON service (id_proprietaire);

-- ---------------------------------------------------- comptes de démonstration
--  Mot de passe BCrypt de « password » — à changer hors démonstration.
INSERT INTO personne (nom, prenom, sexe, login, password, email1, etat)
VALUES
    ('Sarr', 'Ousmane', 'HOMME', 'presta.resto',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'ousmane.sarr@restaurant-dakar.sn', TRUE),
    ('Fall', 'Aïda', 'FEMME', 'presta.hotel',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'aida.fall@hotel-teranga.sn', TRUE)
ON CONFLICT (login) DO NOTHING;

INSERT INTO role (id_personne, id_profil, date_attribution)
SELECT p.id_personne, pr.id_profil, now()
FROM personne p JOIN profil pr ON pr.libelle = 'PRESTATAIRE'
WHERE p.login IN ('presta.resto', 'presta.hotel')
ON CONFLICT (id_personne, id_profil) DO NOTHING;

-- les 3 visiteurs de V17 deviennent des CLIENT
INSERT INTO role (id_personne, id_profil, date_attribution)
SELECT p.id_personne, pr.id_profil, now()
FROM personne p JOIN profil pr ON pr.libelle = 'CLIENT'
WHERE p.login IN ('awa.diop', 'karim.ba', 'marie.ndiaye')
ON CONFLICT (id_personne, id_profil) DO NOTHING;

-- ---------------------------------------------------- affectation propriétaires
UPDATE service SET id_proprietaire = (SELECT id_personne FROM personne WHERE login = 'presta.resto')
WHERE libelle IN ('Restaurant traditionnel', 'Restauration rapide', 'Traiteur événementiel', 'Atelier de couture');

UPDATE service SET id_proprietaire = (SELECT id_personne FROM personne WHERE login = 'presta.hotel')
WHERE libelle IN ('Hôtel Teranga', 'Prêt-à-porter homme');
