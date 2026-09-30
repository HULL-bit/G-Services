-- ============================================================================
--  G-SERVICES — V11 : jeu de démonstration Catalogue (Lot 3)
-- ============================================================================

-- ---------------------------------------------------- unités de mesure -------
INSERT INTO unite_mesure (libelle, symbole) VALUES
    ('Pièce',      'pc'),
    ('Gramme',     'g'),
    ('Kilogramme', 'kg'),
    ('Litre',      'L'),
    ('Heure',      'h'),
    ('Mètre',      'm')
ON CONFLICT (symbole) DO NOTHING;

-- ---------------------------------------------------- catégories de service --
INSERT INTO categorie_service (libelle, description, icone, ordre_affichage) VALUES
    ('Restauration',        'Restaurants, traiteurs, restauration rapide', 'pi pi-shopping-bag', 1),
    ('Mode & textile',      'Prêt-à-porter, sur-mesure, retouches',        'pi pi-tag',          2),
    ('Services à domicile', 'Ménage, jardinage, petits travaux',          'pi pi-home',         3),
    ('Artisanat',           'Bois, cuir, bijouterie, décoration',         'pi pi-palette',      4)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------- services (arborescents) -
INSERT INTO service (libelle, description, prix_indicatif, id_categorie_service, id_service_parent)
SELECT s.libelle, s.descr, s.prix, c.id_categorie_service, NULL
FROM (VALUES
    ('Restaurant traditionnel', 'Cuisine sénégalaise servie sur place',       0,    'Restauration'),
    ('Restauration rapide',     'Plats à emporter, préparation minute',       0,    'Restauration'),
    ('Atelier de couture',      'Confection et retouches sur-mesure',         0,    'Mode & textile'),
    ('Ménage & repassage',      'Prestation récurrente ou ponctuelle',       3000,  'Services à domicile')
) AS s(libelle, descr, prix, cat)
JOIN categorie_service c ON c.libelle = s.cat;

-- sous-service (sous-catégorie) rattaché à « Restaurant traditionnel »
INSERT INTO service (libelle, description, prix_indicatif, id_categorie_service, id_service_parent)
SELECT 'Traiteur événementiel', 'Buffets et cocktails pour réceptions', 0,
       p.id_categorie_service, p.id_service
FROM service p WHERE p.libelle = 'Restaurant traditionnel';

-- ---------------------------------------------------- catalogues -------------
INSERT INTO catalogue (libelle, description, icone, ordre_affichage, id_service)
SELECT ca.libelle, ca.descr, 'pi pi-book', ca.ordre, s.id_service
FROM (VALUES
    ('Menu du midi',      'Formules servies du lundi au vendredi', 1, 'Restaurant traditionnel'),
    ('Carte des boissons','Boissons chaudes et fraîches',          2, 'Restaurant traditionnel'),
    ('À emporter',        'Plats conditionnés',                    1, 'Restauration rapide'),
    ('Prestations couture','Retouches et confections',             1, 'Atelier de couture')
) AS ca(libelle, descr, ordre, srv)
JOIN service s ON s.libelle = ca.srv;

-- ---------------------------------------------------- produits --------------
INSERT INTO produit (libelle, description, id_catalogue)
SELECT pr.libelle, pr.descr, cat.id_catalogue
FROM (VALUES
    ('Plat du jour',         'Le plat cuisiné du jour, portion généreuse', 'Menu du midi'),
    ('Formule entrée + plat','Une entrée au choix et le plat du jour',     'Menu du midi'),
    ('Jus de bissap',        'Infusion d''hibiscus maison',                'Carte des boissons'),
    ('Ourlet pantalon',      'Retouche simple, reprise à la machine',      'Prestations couture')
) AS pr(libelle, descr, cat)
JOIN catalogue cat ON cat.libelle = pr.cat;

-- ---------------------------------------------------- propriétés d'article ---
INSERT INTO proprietes_article (libelle, description, type_saisie, id_unite_mesure)
SELECT pa.libelle, pa.descr, pa.type, um.id_unite_mesure
FROM (VALUES
    ('Portion',          'Poids de la portion servie',   'NOMBRE',  'g'),
    ('Niveau de piment', 'Doux / Moyen / Fort',          'LISTE',   NULL),
    ('Sans gluten',      'Convient aux intolérants',     'BOOLEEN', NULL),
    ('Contenance',       'Volume servi',                 'NOMBRE',  'L'),
    ('Taille',           'Taille de confection',         'LISTE',   NULL)
) AS pa(libelle, descr, type, sym)
LEFT JOIN unite_mesure um ON um.symbole = pa.sym;

-- « définit » : quelles propriétés chaque produit expose
INSERT INTO produit_proprietes_article (id_produit, id_proprietes_article)
SELECT p.id_produit, pa.id_proprietes_article
FROM (VALUES
    ('Plat du jour',          'Portion'),
    ('Plat du jour',          'Niveau de piment'),
    ('Plat du jour',          'Sans gluten'),
    ('Formule entrée + plat', 'Portion'),
    ('Jus de bissap',         'Contenance'),
    ('Ourlet pantalon',       'Taille')
) AS lien(prod, prop)
JOIN produit p ON p.libelle = lien.prod
JOIN proprietes_article pa ON pa.libelle = lien.prop
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------- articles --------------
INSERT INTO article (reference, description, prix, devise, mode_vente, disponibilite, promotion, taux_remise_pourcentage, id_produit)
SELECT a.ref, a.descr, a.prix, 'XOF', a.mode, a.dispo, a.promo, a.taux, pr.id_produit
FROM (VALUES
    ('PDJ-THIEB', 'Thiéboudiène rouge, poisson et légumes',  3500, 'UNITE', TRUE,  FALSE, 0,  'Plat du jour'),
    ('PDJ-YASSA', 'Yassa poulet, riz blanc',                 3000, 'UNITE', TRUE,  TRUE,  15, 'Plat du jour'),
    ('FORM-EP',   'Formule entrée + plat du jour',           4500, 'UNITE', TRUE,  FALSE, 0,  'Formule entrée + plat'),
    ('BIS-33',    'Jus de bissap 33 cl',                      500, 'UNITE', TRUE,  FALSE, 0,  'Jus de bissap'),
    ('BIS-100',   'Jus de bissap 1 L',                       1200, 'UNITE', FALSE, FALSE, 0,  'Jus de bissap'),
    ('OURLET-STD','Ourlet pantalon standard',                1500, 'UNITE', TRUE,  FALSE, 0,  'Ourlet pantalon')
) AS a(ref, descr, prix, mode, dispo, promo, taux, prod)
JOIN produit pr ON pr.libelle = a.prod;

-- ---------------------------------------------------- variétés d'article ----
INSERT INTO variete_article (valeur, id_article, id_proprietes_article)
SELECT v.valeur, art.id_article, pa.id_proprietes_article
FROM (VALUES
    ('350',    'PDJ-THIEB', 'Portion'),
    ('Moyen',  'PDJ-THIEB', 'Niveau de piment'),
    ('Non',    'PDJ-THIEB', 'Sans gluten'),
    ('320',    'PDJ-YASSA', 'Portion'),
    ('Doux',   'PDJ-YASSA', 'Niveau de piment'),
    ('0.33',   'BIS-33',    'Contenance'),
    ('1',      'BIS-100',   'Contenance')
) AS v(valeur, ref, prop)
JOIN article art ON art.reference = v.ref
JOIN proprietes_article pa ON pa.libelle = v.prop
ON CONFLICT (id_article, id_proprietes_article) DO NOTHING;
