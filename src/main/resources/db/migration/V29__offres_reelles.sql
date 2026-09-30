-- ============================================================================
--  G-SERVICES — V29 : de vraies offres à la place des « option 1/2/3 »
--
--  V23 avait généré pour chaque service 2 produits « Formule Essentielle » /
--  « Formule Premium » avec des articles « — option 1/2/3 ». On les remplace par
--  des offres crédibles, propres à chaque catégorie, avec libellés, prix
--  cohérents et une illustration (SVG de /media/offres, aucune image externe).
-- ============================================================================

CREATE TEMP TABLE _offre (
    categorie   text,
    rang        int,          -- 1 = ex-« Formule Essentielle », 2 = ex-« Formule Premium »
    produit     text,
    descr       text,
    image       text,
    a1 text, p1 numeric, a2 text, p2 numeric, a3 text, p3 numeric
) ON COMMIT DROP;

INSERT INTO _offre VALUES
('Restauration',1,'Menu du jour','Notre formule du midi, préparée chaque matin avec des produits frais.','media/offres/restauration.svg',
  'Entrée + plat',4500,'Plat + dessert',5000,'Formule complète (entrée, plat, dessert)',6500),
('Restauration',2,'À la carte','Nos spécialités à composer selon vos envies.','media/offres/restauration.svg',
  'Plat signature',6500,'Grillade ou poisson braisé',7500,'Dessert maison',2500),

('Mode & textile',1,'Prêt-à-porter','Pièces disponibles en boutique, essayage sur place.','media/offres/mode.svg',
  'Chemise ou haut',12000,'Pantalon ou bas',15000,'Ensemble deux pièces',25000),
('Mode & textile',2,'Sur-mesure & retouches','Ajustements et créations sur mesure par nos couturiers.','media/offres/mode.svg',
  'Retouche simple (ourlet)',3000,'Reprise ou ajustement',6000,'Création sur-mesure',35000),

('Services à domicile',1,'Intervention à l''heure','Un professionnel se déplace chez vous, matériel inclus.','media/offres/services-domicile.svg',
  '1 heure',5000,'2 heures',9000,'Demi-journée (4 h)',16000),
('Services à domicile',2,'Forfait entretien','Passages réguliers planifiés, tarif dégressif.','media/offres/services-domicile.svg',
  'Passage mensuel',18000,'Forfait trimestriel',48000,'Forfait annuel',170000),

('Artisanat',1,'Pièce artisanale','Fabrication locale, finitions main.','media/offres/artisanat.svg',
  'Petit modèle',8000,'Grand modèle',20000,'Pièce sur commande',45000),
('Artisanat',2,'Réparation & restauration','Remise en état d''objets et de mobilier.','media/offres/artisanat.svg',
  'Diagnostic',3000,'Réparation courante',12000,'Restauration complète',40000),

('Hôtellerie',1,'Chambre Standard','Chambre confortable, salle de bain privative.','media/offres/hotellerie.svg',
  'Nuit + petit-déjeuner',22000,'Nuit + demi-pension',30000,'Séjour 7 nuits',135000),
('Hôtellerie',2,'Chambre Supérieure','Espace plus grand, vue dégagée, prestations premium.','media/offres/hotellerie.svg',
  'Nuit + petit-déjeuner',38000,'Nuit + demi-pension',48000,'Week-end (2 nuits)',90000),

('Santé & bien-être',1,'Consultation','Consultation sur rendez-vous, praticien diplômé.','media/offres/sante.svg',
  'Consultation standard',8000,'Consultation de suivi',6000,'Consultation à domicile',15000),
('Santé & bien-être',2,'Soin & massage','Séances de bien-être en cabinet.','media/offres/sante.svg',
  'Séance 30 min',12000,'Séance 60 min',20000,'Séance 90 min',28000),

('Beauté & coiffure',1,'Coiffure','Coupe, coiffage et coloration, femmes et hommes.','media/offres/beaute.svg',
  'Coupe',5000,'Coupe + brushing',9000,'Coloration',18000),
('Beauté & coiffure',2,'Soins & esthétique','Prestations ongles et maquillage.','media/offres/beaute.svg',
  'Manucure',6000,'Pédicure',8000,'Maquillage',12000),

('Automobile',1,'Entretien','Entretien courant, pièces d''origine.','media/offres/automobile.svg',
  'Vidange',25000,'Révision complète',55000,'Diagnostic électronique',15000),
('Automobile',2,'Réparation','Mécanique et remplacement de pièces.','media/offres/automobile.svg',
  'Main-d''œuvre (1 h)',12000,'Train de pneus',120000,'Système de freinage',65000),

('Événementiel',1,'Prestation événement','Organisation et animation de votre événement.','media/offres/evenementiel.svg',
  'Demi-journée',90000,'Journée complète',160000,'Week-end',280000),
('Événementiel',2,'Pack clé en main','Formules tout compris selon le nombre d''invités.','media/offres/evenementiel.svg',
  'Pack essentiel (50 pers.)',350000,'Pack confort (100 pers.)',650000,'Pack prestige (200 pers.)',1200000),

('Transport & logistique',1,'Course & livraison','Livraison à Dakar et proche banlieue.','media/offres/transport.svg',
  'Course urbaine',2500,'Livraison express',4500,'Coursier à la journée',30000),
('Transport & logistique',2,'Déménagement','Équipe, véhicule et matériel d''emballage.','media/offres/transport.svg',
  'Studio / T1',45000,'Appartement T3',95000,'Maison',180000),

('Éducation & formation',1,'Cours particulier','Cours individuels à domicile ou en centre.','media/offres/education.svg',
  'Séance 1 h',6000,'Forfait 10 h',50000,'Forfait mensuel (8 h)',40000),
('Éducation & formation',2,'Stage intensif','Sessions groupées pendant les vacances.','media/offres/education.svg',
  'Stage week-end',35000,'Stage vacances (5 j)',90000,'Préparation examen',120000),

('Informatique & digital',1,'Dépannage','Assistance matérielle et logicielle.','media/offres/informatique.svg',
  'Assistance à distance',8000,'Intervention sur site',20000,'Forfait 5 interventions',80000),
('Informatique & digital',2,'Création web','Sites et maintenance pour professionnels.','media/offres/informatique.svg',
  'Site vitrine',250000,'Maintenance mensuelle',30000,'Formation (3 h)',45000),

('Immobilier',1,'Transaction','Estimation, vente et recherche de biens.','media/offres/immobilier.svg',
  'Estimation',25000,'Mandat de vente (honoraires)',350000,'Accompagnement recherche',150000),
('Immobilier',2,'Gestion locative','Mise en location et gestion pour propriétaires.','media/offres/immobilier.svg',
  'Mise en location',80000,'Gestion mensuelle',25000,'État des lieux',30000);

-- ---------------------------------------------------- rebranding des produits
UPDATE produit pr
SET libelle = o.produit, description = o.descr, image = o.image
FROM catalogue c
JOIN service s  ON s.id_service = c.id_service
JOIN categorie_service cs ON cs.id_categorie_service = s.id_categorie_service
JOIN _offre o ON o.categorie = cs.libelle
WHERE c.id_catalogue = pr.id_catalogue
  AND ( (o.rang = 1 AND pr.libelle = 'Formule Essentielle')
     OR (o.rang = 2 AND pr.libelle = 'Formule Premium') );

-- ---------------------------------------------------- articles : nom + prix réels
UPDATE article a
SET description = CASE regexp_replace(a.reference, '.*-A', '')::int
                    WHEN 1 THEN o.a1 WHEN 2 THEN o.a2 ELSE o.a3 END,
    prix        = CASE regexp_replace(a.reference, '.*-A', '')::int
                    WHEN 1 THEN o.p1 WHEN 2 THEN o.p2 ELSE o.p3 END
FROM produit pr
JOIN catalogue c ON c.id_catalogue = pr.id_catalogue
JOIN service s   ON s.id_service = c.id_service
JOIN categorie_service cs ON cs.id_categorie_service = s.id_categorie_service
JOIN _offre o ON o.categorie = cs.libelle AND o.produit = pr.libelle
WHERE a.id_produit = pr.id_produit
  AND a.description LIKE '% — option %';

-- promotion : garde une remise sur le 3e article ~1 produit sur 4 (comme V23)
UPDATE article a
SET promotion = (a.id_article % 9 = 0),
    taux_remise_pourcentage = CASE WHEN a.id_article % 9 = 0 THEN 10 ELSE 0 END
WHERE a.reference LIKE 'SRV%-A3';

-- ---------------------------------------------------- illustration par défaut
UPDATE produit pr
SET image = COALESCE(pr.image, 'media/offres/' || CASE cs.libelle
        WHEN 'Restauration' THEN 'restauration'
        WHEN 'Mode & textile' THEN 'mode'
        WHEN 'Services à domicile' THEN 'services-domicile'
        WHEN 'Artisanat' THEN 'artisanat'
        WHEN 'Hôtellerie' THEN 'hotellerie'
        WHEN 'Santé & bien-être' THEN 'sante'
        WHEN 'Beauté & coiffure' THEN 'beaute'
        WHEN 'Automobile' THEN 'automobile'
        WHEN 'Événementiel' THEN 'evenementiel'
        WHEN 'Transport & logistique' THEN 'transport'
        WHEN 'Éducation & formation' THEN 'education'
        WHEN 'Informatique & digital' THEN 'informatique'
        WHEN 'Immobilier' THEN 'immobilier'
        ELSE 'generique' END || '.svg')
FROM catalogue c
JOIN service s ON s.id_service = c.id_service
JOIN categorie_service cs ON cs.id_categorie_service = s.id_categorie_service
WHERE c.id_catalogue = pr.id_catalogue AND pr.image IS NULL;
