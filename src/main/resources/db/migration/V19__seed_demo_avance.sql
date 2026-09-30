-- ============================================================================
--  G-SERVICES — V19 : démonstration « conforme au modèle » de cas variés
--
--  1. Produit physique mesuré et décliné par couleur/taille  → Chemise Oxford
--     (ProprietesArticle + UniteMesure + VarieteArticle + Stock + Lot).
--  2. Service d'hôtellerie et ses offres                       → Hôtel Teranga
--     (catégorie/service, catalogue, produits « types de chambre », articles
--      « tarifs », propriétés Capacité/Superficie/Vue).
--
--  Toutes les cardinalités du diagramme sont respectées :
--   CategorieService 1-n Service (n-0..1 parent) ; Service 1-n Catalogue ;
--   Catalogue 1-n Produit ; Produit 1-n Article ; Produit n-n ProprietesArticle
--   (« définit ») ; Article n-n ProprietesArticle via VarieteArticle
--   (« caractérise ») ; ProprietesArticle n-1 UniteMesure ;
--   Article 1-0..1 Stock ; Stock 1-n Lot ; Lot n-1 Mois.
-- ============================================================================

-- ---------------------------------------------------- unités complémentaires
INSERT INTO unite_mesure (libelle, symbole) VALUES
    ('Grammage',   'g/m2'),
    ('Mètre carré','m2'),
    ('Personne',   'pers'),
    ('Nuit',       'nuit')
ON CONFLICT (symbole) DO NOTHING;

-- ============================================================================
--  1) CHEMISE OXFORD — prêt-à-porter, physique, mesuré, décliné couleur/taille
-- ============================================================================

-- service prêt-à-porter (racine de « Mode & textile »)
INSERT INTO service (libelle, description, prix_indicatif, id_categorie_service, id_service_parent)
SELECT 'Prêt-à-porter homme', 'Chemises, pantalons, vestes en stock', 0,
       c.id_categorie_service, NULL
FROM categorie_service c WHERE c.libelle = 'Mode & textile'
  AND NOT EXISTS (SELECT 1 FROM service s WHERE s.libelle = 'Prêt-à-porter homme');

INSERT INTO catalogue (libelle, description, icone, ordre_affichage, id_service)
SELECT 'Chemises', 'Chemises ville et casual', 'pi pi-bookmark', 1, s.id_service
FROM service s WHERE s.libelle = 'Prêt-à-porter homme'
  AND NOT EXISTS (SELECT 1 FROM catalogue c WHERE c.libelle = 'Chemises' AND c.id_service = s.id_service);

INSERT INTO produit (libelle, description, id_catalogue)
SELECT 'Chemise Oxford', 'Chemise Oxford coupe droite, boutons nacre', c.id_catalogue
FROM catalogue c JOIN service s ON s.id_service = c.id_service
WHERE c.libelle = 'Chemises' AND s.libelle = 'Prêt-à-porter homme'
  AND NOT EXISTS (SELECT 1 FROM produit p WHERE p.libelle = 'Chemise Oxford');

-- propriétés utilisées par la chemise
INSERT INTO proprietes_article (libelle, description, type_saisie, id_unite_mesure)
SELECT v.lib, v.descr, v.type, um.id_unite_mesure
FROM (VALUES
    ('Couleur',  'Coloris du tissu',            'LISTE',  NULL),
    ('Taille',   'Taille de confection (S..XXL)','LISTE',  NULL),
    ('Matière',  'Composition principale',      'LISTE',  NULL),
    ('Grammage', 'Poids du tissu',              'NOMBRE', 'g/m2')
) AS v(lib, descr, type, sym)
LEFT JOIN unite_mesure um ON um.symbole = v.sym
WHERE NOT EXISTS (SELECT 1 FROM proprietes_article pa WHERE pa.libelle = v.lib);

-- « définit » : la chemise expose Couleur, Taille, Matière, Grammage
INSERT INTO produit_proprietes_article (id_produit, id_proprietes_article)
SELECT p.id_produit, pa.id_proprietes_article
FROM produit p CROSS JOIN proprietes_article pa
WHERE p.libelle = 'Chemise Oxford'
  AND pa.libelle IN ('Couleur', 'Taille', 'Matière', 'Grammage')
ON CONFLICT DO NOTHING;

-- articles = déclinaisons (couleur x taille)
INSERT INTO article (reference, description, prix, devise, mode_vente, disponibilite, id_produit)
SELECT v.ref, v.descr, v.prix, 'XOF', 'UNITE', TRUE, p.id_produit
FROM (VALUES
    ('CHEM-OXF-BLC-M', 'Chemise Oxford blanche, taille M',  15000, 'Chemise Oxford'),
    ('CHEM-OXF-BLC-L', 'Chemise Oxford blanche, taille L',  15000, 'Chemise Oxford'),
    ('CHEM-OXF-BLU-M', 'Chemise Oxford bleu ciel, taille M', 15000, 'Chemise Oxford'),
    ('CHEM-OXF-BLU-L', 'Chemise Oxford bleu ciel, taille L', 15000, 'Chemise Oxford'),
    ('CHEM-OXF-RAY-L', 'Chemise Oxford rayée, taille L',     16500, 'Chemise Oxford')
) AS v(ref, descr, prix, prod)
JOIN produit p ON p.libelle = v.prod
ON CONFLICT (reference) DO NOTHING;

-- variétés : valeur de chaque propriété pour chaque article
INSERT INTO variete_article (valeur, id_article, id_proprietes_article)
SELECT v.valeur, a.id_article, pa.id_proprietes_article
FROM (VALUES
    ('CHEM-OXF-BLC-M', 'Couleur',  'Blanc'),
    ('CHEM-OXF-BLC-M', 'Taille',   'M'),
    ('CHEM-OXF-BLC-M', 'Matière',  '100% coton'),
    ('CHEM-OXF-BLC-M', 'Grammage', '140'),
    ('CHEM-OXF-BLC-L', 'Couleur',  'Blanc'),
    ('CHEM-OXF-BLC-L', 'Taille',   'L'),
    ('CHEM-OXF-BLC-L', 'Matière',  '100% coton'),
    ('CHEM-OXF-BLC-L', 'Grammage', '140'),
    ('CHEM-OXF-BLU-M', 'Couleur',  'Bleu ciel'),
    ('CHEM-OXF-BLU-M', 'Taille',   'M'),
    ('CHEM-OXF-BLU-M', 'Matière',  '100% coton'),
    ('CHEM-OXF-BLU-M', 'Grammage', '140'),
    ('CHEM-OXF-BLU-L', 'Couleur',  'Bleu ciel'),
    ('CHEM-OXF-BLU-L', 'Taille',   'L'),
    ('CHEM-OXF-BLU-L', 'Matière',  '100% coton'),
    ('CHEM-OXF-BLU-L', 'Grammage', '140'),
    ('CHEM-OXF-RAY-L', 'Couleur',  'Rayé bleu/blanc'),
    ('CHEM-OXF-RAY-L', 'Taille',   'L'),
    ('CHEM-OXF-RAY-L', 'Matière',  '80% coton, 20% lin'),
    ('CHEM-OXF-RAY-L', 'Grammage', '160')
) AS v(ref, prop, valeur)
JOIN article a ON a.reference = v.ref
JOIN proprietes_article pa ON pa.libelle = v.prop
ON CONFLICT (id_article, id_proprietes_article) DO NOTHING;

-- stock + lots pour les articles physiques (reçus en janvier 2027, sans péremption)
INSERT INTO stock (quantite_totale, seuil_alerte, id_article)
SELECT 0, 5, a.id_article
FROM article a
WHERE a.reference LIKE 'CHEM-OXF-%'
ON CONFLICT (id_article) DO NOTHING;

INSERT INTO lot (numero_lot, quantite, prix_achat, date_entree, id_stock, id_mois)
SELECT 'LOT-' || a.reference, v.qte, v.achat, DATE '2027-01-20', st.id_stock, m.id_mois
FROM (VALUES
    ('CHEM-OXF-BLC-M', 18, 6000),
    ('CHEM-OXF-BLC-L', 22, 6000),
    ('CHEM-OXF-BLU-M', 12, 6000),
    ('CHEM-OXF-BLU-L',  4, 6000),   -- sous le seuil : en alerte
    ('CHEM-OXF-RAY-L',  9, 6800)
) AS v(ref, qte, achat)
JOIN article a ON a.reference = v.ref
JOIN stock st ON st.id_article = a.id_article
JOIN annee y ON y.valeur_annee = 2027
JOIN mois m ON m.id_annee = y.id_annee AND m.numero_mois = 1
ON CONFLICT DO NOTHING;

UPDATE stock st SET quantite_totale = COALESCE((
    SELECT SUM(lo.quantite) FROM lot lo
    WHERE lo.id_stock = st.id_stock AND lo.etat = TRUE
      AND (lo.date_peremption IS NULL OR lo.date_peremption >= CURRENT_DATE)), 0)
WHERE st.id_article IN (SELECT id_article FROM article WHERE reference LIKE 'CHEM-OXF-%');

-- ============================================================================
--  2) HÔTELLERIE — service, catalogue de chambres, tarifs, propriétés mesurées
-- ============================================================================

INSERT INTO categorie_service (libelle, description, icone, ordre_affichage)
SELECT 'Hôtellerie', 'Hôtels, maisons d''hôtes, résidences', 'pi pi-building', 5
WHERE NOT EXISTS (SELECT 1 FROM categorie_service WHERE libelle = 'Hôtellerie');

INSERT INTO service (libelle, description, prix_indicatif, id_categorie_service, id_service_parent)
SELECT 'Hôtel Teranga', 'Hôtel 3 étoiles, centre-ville de Dakar', 25000,
       c.id_categorie_service, NULL
FROM categorie_service c WHERE c.libelle = 'Hôtellerie'
  AND NOT EXISTS (SELECT 1 FROM service s WHERE s.libelle = 'Hôtel Teranga');

-- fiche prestataire + géolocalisation (Dakar) + ouverture 24h/24
INSERT INTO information_service (adresse, telephone1, email1, site_web, disponibilite, id_service, id_position)
SELECT '12 avenue Pompidou, Dakar', '+221 33 889 00 00', 'reservation@hotel-teranga.sn',
       'https://hotel-teranga.sn', TRUE, s.id_service,
       (SELECT id_position FROM position p JOIN ville v ON v.id_ville = p.id_ville
        WHERE v.libelle = 'Dakar' LIMIT 1)
FROM service s WHERE s.libelle = 'Hôtel Teranga'
  AND NOT EXISTS (SELECT 1 FROM information_service i WHERE i.id_service = s.id_service);

INSERT INTO horaire (heure_ouverture, heure_fermeture, ouvert24h, id_information_service, id_jour)
SELECT NULL, NULL, TRUE, i.id_information_service, j.id_jour
FROM information_service i
JOIN service s ON s.id_service = i.id_service AND s.libelle = 'Hôtel Teranga'
CROSS JOIN jour j
ON CONFLICT (id_information_service, id_jour) DO NOTHING;

INSERT INTO catalogue (libelle, description, icone, ordre_affichage, id_service)
SELECT 'Chambres', 'Nos catégories de chambres et formules', 'pi pi-home', 1, s.id_service
FROM service s WHERE s.libelle = 'Hôtel Teranga'
  AND NOT EXISTS (SELECT 1 FROM catalogue c WHERE c.libelle = 'Chambres' AND c.id_service = s.id_service);

-- produits = types de chambre
INSERT INTO produit (libelle, description, id_catalogue)
SELECT v.lib, v.descr, c.id_catalogue
FROM (VALUES
    ('Chambre Standard', 'Chambre double, climatisée, salle d''eau'),
    ('Chambre Deluxe',   'Chambre spacieuse avec balcon'),
    ('Suite Junior',     'Salon séparé, vue sur mer')
) AS v(lib, descr)
JOIN catalogue c ON c.libelle = 'Chambres'
JOIN service s ON s.id_service = c.id_service AND s.libelle = 'Hôtel Teranga'
WHERE NOT EXISTS (SELECT 1 FROM produit p WHERE p.libelle = v.lib);

-- propriétés mesurées des chambres
INSERT INTO proprietes_article (libelle, description, type_saisie, id_unite_mesure)
SELECT v.lib, v.descr, v.type, um.id_unite_mesure
FROM (VALUES
    ('Capacité',   'Nombre de personnes',        'NOMBRE', 'pers'),
    ('Superficie', 'Surface de la chambre',      'NOMBRE', 'm2'),
    ('Vue',        'Orientation / vue',          'LISTE',  NULL),
    ('Lit',        'Type de couchage',           'LISTE',  NULL)
) AS v(lib, descr, type, sym)
LEFT JOIN unite_mesure um ON um.symbole = v.sym
WHERE NOT EXISTS (SELECT 1 FROM proprietes_article pa WHERE pa.libelle = v.lib);

INSERT INTO produit_proprietes_article (id_produit, id_proprietes_article)
SELECT p.id_produit, pa.id_proprietes_article
FROM produit p
JOIN catalogue c ON c.id_catalogue = p.id_catalogue AND c.libelle = 'Chambres'
CROSS JOIN proprietes_article pa
WHERE pa.libelle IN ('Capacité', 'Superficie', 'Vue', 'Lit')
ON CONFLICT DO NOTHING;

-- articles = tarifs (formule) par type de chambre, vendus à la nuit
INSERT INTO article (reference, description, prix, devise, mode_vente, disponibilite, promotion, taux_remise_pourcentage, id_produit)
SELECT v.ref, v.descr, v.prix, 'XOF', 'NUIT', TRUE, v.promo, v.taux, p.id_produit
FROM (VALUES
    ('STD-BB',     'Standard — nuit + petit-déjeuner',        35000, FALSE, 0,  'Chambre Standard'),
    ('STD-DP',     'Standard — nuit + demi-pension',          48000, FALSE, 0,  'Chambre Standard'),
    ('STD-LONG',   'Standard — séjour 7 nuits (-15%)',        35000, TRUE,  15, 'Chambre Standard'),
    ('DLX-BB',     'Deluxe — nuit + petit-déjeuner',          52000, FALSE, 0,  'Chambre Deluxe'),
    ('SUITE-BB',   'Suite Junior — nuit + petit-déjeuner',    85000, FALSE, 0,  'Suite Junior')
) AS v(ref, descr, prix, promo, taux, prod)
JOIN produit p ON p.libelle = v.prod
ON CONFLICT (reference) DO NOTHING;

INSERT INTO variete_article (valeur, id_article, id_proprietes_article)
SELECT v.valeur, a.id_article, pa.id_proprietes_article
FROM (VALUES
    ('STD-BB',   'Capacité',   '2'),
    ('STD-BB',   'Superficie', '18'),
    ('STD-BB',   'Vue',        'Ville'),
    ('STD-BB',   'Lit',        '1 lit double'),
    ('STD-DP',   'Capacité',   '2'),
    ('STD-DP',   'Superficie', '18'),
    ('STD-DP',   'Vue',        'Ville'),
    ('STD-DP',   'Lit',        '1 lit double'),
    ('STD-LONG', 'Capacité',   '2'),
    ('STD-LONG', 'Superficie', '18'),
    ('STD-LONG', 'Vue',        'Ville'),
    ('DLX-BB',   'Capacité',   '3'),
    ('DLX-BB',   'Superficie', '28'),
    ('DLX-BB',   'Vue',        'Jardin'),
    ('DLX-BB',   'Lit',        '1 lit double + 1 lit simple'),
    ('SUITE-BB', 'Capacité',   '4'),
    ('SUITE-BB', 'Superficie', '45'),
    ('SUITE-BB', 'Vue',        'Mer'),
    ('SUITE-BB', 'Lit',        '2 lits doubles')
) AS v(ref, prop, valeur)
JOIN article a ON a.reference = v.ref
JOIN proprietes_article pa ON pa.libelle = v.prop
ON CONFLICT (id_article, id_proprietes_article) DO NOTHING;

-- ---- rattache le module Catalogue à SUPER_ADMIN pour les nouvelles branches (déjà fait par V12) ----
