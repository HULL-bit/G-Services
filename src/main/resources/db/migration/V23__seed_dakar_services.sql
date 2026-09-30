-- ============================================================================
--  G-SERVICES — V23 : parc de démonstration « Dakar »
--
--  ~15 fournisseurs de services + ~42 services, tous géolocalisés à Dakar,
--  chacun avec sa fiche prestataire, ses horaires, un catalogue, 2 produits et
--  6 articles. Une trentaine d'avis modérés pour varier les notes.
--  Alimente la page publique « Carte » (/public/carte.xhtml) et ses filtres.
-- ============================================================================

-- ---------------------------------------------------- catégories additionnelles
INSERT INTO categorie_service (libelle, description, icone, ordre_affichage)
SELECT v.lib, v.descr, v.icone, v.ordre
FROM (VALUES
    ('Santé & bien-être',     'Cliniques, pharmacies, kinés, spas',          'pi pi-heart',        6),
    ('Beauté & coiffure',     'Salons, barbiers, onglerie, maquillage',      'pi pi-star',         7),
    ('Automobile',            'Garages, lavage, pièces, location',           'pi pi-car',          8),
    ('Événementiel',          'Traiteurs, décoration, sonorisation, photo',  'pi pi-calendar',     9),
    ('Transport & logistique','VTC, coursiers, déménagement, fret',          'pi pi-send',         10),
    ('Éducation & formation', 'Cours particuliers, centres, langues',        'pi pi-book',         11),
    ('Informatique & digital','Dépannage, dev web, impression, réseaux',     'pi pi-desktop',      12),
    ('Immobilier',            'Agences, gestion locative, syndic',           'pi pi-building',     13)
) AS v(lib, descr, icone, ordre)
WHERE NOT EXISTS (SELECT 1 FROM categorie_service c WHERE c.libelle = v.lib);

-- ---------------------------------------------------- fournisseurs de services
INSERT INTO personne (nom, prenom, sexe, login, password, email1, etat)
SELECT v.nom, v.prenom, v.sexe, v.login,
       '$2a$10$Cf2ORSViE/Ck2LbK6RlqrO7ga7TFDuW3yzSlzJhIvTvveklOafI2S',   -- « password »
       v.login || '@gservices-demo.sn', TRUE
FROM (VALUES
    ('Ndour','Cheikh','HOMME','fs.ndour'),      ('Diallo','Fatou','FEMME','fs.diallo'),
    ('Gueye','Modou','HOMME','fs.gueye'),       ('Sow','Mariama','FEMME','fs.sow'),
    ('Sy','Ibrahima','HOMME','fs.sy'),          ('Faye','Ndeye','FEMME','fs.faye'),
    ('Camara','Lamine','HOMME','fs.camara'),    ('Ba','Rokhaya','FEMME','fs.ba'),
    ('Diouf','Serigne','HOMME','fs.diouf'),     ('Sène','Awa','FEMME','fs.sene'),
    ('Kane','Abdou','HOMME','fs.kane'),         ('Thiam','Bineta','FEMME','fs.thiam'),
    ('Mbaye','Pape','HOMME','fs.mbaye'),        ('Diagne','Sokhna','FEMME','fs.diagne'),
    ('Ndiaye','Alioune','HOMME','fs.ndiaye2')
) AS v(nom, prenom, sexe, login)
ON CONFLICT (login) DO NOTHING;

INSERT INTO role (id_personne, id_profil, date_attribution)
SELECT p.id_personne, pr.id_profil, now()
FROM personne p JOIN profil pr ON pr.libelle = 'PRESTATAIRE'
WHERE p.login LIKE 'fs.%'
ON CONFLICT (id_personne, id_profil) DO NOTHING;

-- ---------------------------------------------------- clients de démonstration
INSERT INTO personne (nom, prenom, sexe, login, password, email1, etat)
SELECT v.nom, v.prenom, v.sexe, v.login,
       '$2a$10$Cf2ORSViE/Ck2LbK6RlqrO7ga7TFDuW3yzSlzJhIvTvveklOafI2S', v.login || '@example.sn', TRUE
FROM (VALUES
    ('Sarr','Khadija','FEMME','cl.sarr'),   ('Toure','Malick','HOMME','cl.toure'),
    ('Diop','Aminata','FEMME','cl.diop'),   ('Fall','Ousseynou','HOMME','cl.fall'),
    ('Ndao','Coumba','FEMME','cl.ndao'),    ('Cisse','Babacar','HOMME','cl.cisse'),
    ('Wade','Sophie','FEMME','cl.wade'),    ('Gomis','Jean','HOMME','cl.gomis')
) AS v(nom, prenom, sexe, login)
ON CONFLICT (login) DO NOTHING;

INSERT INTO role (id_personne, id_profil, date_attribution)
SELECT p.id_personne, pr.id_profil, now()
FROM personne p JOIN profil pr ON pr.libelle = 'CLIENT'
WHERE p.login LIKE 'cl.%'
ON CONFLICT (id_personne, id_profil) DO NOTHING;

-- ---------------------------------------------------- 42 services à Dakar -----
--  Table de travail temporaire : (libelle, catégorie, quartier, lat, lng, rang).
CREATE TEMP TABLE _svc (
    libelle    text, categorie text, quartier text,
    lat double precision, lng double precision, rang int
) ON COMMIT DROP;

INSERT INTO _svc(libelle, categorie, quartier, lat, lng, rang) VALUES
 ('Chez Adja — cuisine sénégalaise','Restauration','Médina',14.6797,-17.4512,1),
 ('Le Baobab Grill','Restauration','Plateau',14.6690,-17.4380,2),
 ('Fast Teranga','Restauration','Sacré-Cœur',14.7205,-17.4630,3),
 ('Pâtisserie des Almadies','Restauration','Almadies',14.7442,-17.5195,4),
 ('Dibiterie Point E','Restauration','Point E',14.7015,-17.4620,5),
 ('Café Ngor','Restauration','Ngor',14.7490,-17.5130,6),
 ('Atelier Couture Bineta','Mode & textile','Sicap Liberté',14.7220,-17.4560,7),
 ('Wax & Style','Mode & textile','Grand Dakar',14.7108,-17.4425,8),
 ('Cordonnerie Moderne','Mode & textile','HLM',14.7090,-17.4460,9),
 ('Retouche Express Yoff','Mode & textile','Yoff',14.7530,-17.4720,10),
 ('Ménage Plus','Services à domicile','Mermoz',14.7095,-17.4695,11),
 ('Jardinage Teranga','Services à domicile','Ouakam',14.7220,-17.4930,12),
 ('Plomberie 24/7','Services à domicile','Parcelles Assainies',14.7690,-17.4290,13),
 ('Électricien Fann','Services à domicile','Fann',14.6905,-17.4640,14),
 ('Artisanat du Sahel','Artisanat','Plateau',14.6720,-17.4400,15),
 ('Bijoux Sokhna','Artisanat','Médina',14.6820,-17.4530,16),
 ('Menuiserie Bois d''Ébène','Artisanat','Grand Yoff',14.7360,-17.4530,17),
 ('Poterie de Ngor','Artisanat','Ngor',14.7475,-17.5115,18),
 ('Hôtel Teranga Plateau','Hôtellerie','Plateau',14.6708,-17.4390,19),
 ('Résidence Les Filaos','Hôtellerie','Almadies',14.7455,-17.5160,20),
 ('Maison d''hôtes Yoff','Hôtellerie','Yoff',14.7560,-17.4680,21),
 ('Clinique du Point E','Santé & bien-être','Point E',14.7010,-17.4600,22),
 ('Pharmacie Sacré-Cœur','Santé & bien-être','Sacré-Cœur',14.7215,-17.4610,23),
 ('Cabinet Kiné Mermoz','Santé & bien-être','Mermoz',14.7080,-17.4680,24),
 ('Spa Almadies','Santé & bien-être','Almadies',14.7430,-17.5185,25),
 ('Salon Élégance','Beauté & coiffure','Sicap Liberté',14.7235,-17.4545,26),
 ('Barbier du Plateau','Beauté & coiffure','Plateau',14.6675,-17.4360,27),
 ('Onglerie Glam','Beauté & coiffure','Point E',14.7000,-17.4635,28),
 ('Garage Auto Sy','Automobile','Grand Dakar',14.7120,-17.4410,29),
 ('Lavauto Express','Automobile','Ouakam',14.7250,-17.4900,30),
 ('Pièces Auto Colobane','Automobile','Colobane',14.7020,-17.4470,31),
 ('Location Voitures Dakar','Automobile','Aéroport LSS',14.7440,-17.4900,32),
 ('Traiteur Événements Faye','Événementiel','Fann',14.6920,-17.4610,33),
 ('Déco & Réception','Événementiel','Mermoz',14.7060,-17.4715,34),
 ('Sono Light Show','Événementiel','Grand Yoff',14.7345,-17.4560,35),
 ('Studio Photo Camara','Événementiel','Plateau',14.6700,-17.4370,36),
 ('VTC Dakar Confort','Transport & logistique','Yoff',14.7515,-17.4700,37),
 ('Coursiers Express','Transport & logistique','HLM',14.7100,-17.4445,38),
 ('Déménagements Kane','Transport & logistique','Pikine',14.7540,-17.3960,39),
 ('Cours Particuliers Diagne','Éducation & formation','Sacré-Cœur',14.7225,-17.4590,40),
 ('DakarTech Services','Informatique & digital','Point E',14.7025,-17.4610,41),
 ('Agence Immo Teranga','Immobilier','Almadies',14.7420,-17.5150,42);

-- services + rattachement catégorie + propriétaire (round-robin sur fs.*)
INSERT INTO service (libelle, description, prix_indicatif, id_categorie_service, id_proprietaire)
SELECT s.libelle,
       'Prestataire situé à ' || s.quartier || ', Dakar.',
       (2000 + s.rang * 250)::numeric,
       c.id_categorie_service,
       fs.id_personne
FROM _svc s
JOIN categorie_service c ON c.libelle = s.categorie
JOIN LATERAL (
     SELECT id_personne FROM personne
     WHERE login = 'fs.' || (ARRAY['ndour','diallo','gueye','sow','sy','faye','camara','ba',
                                    'diouf','sene','kane','thiam','mbaye','diagne','ndiaye2'])[1 + (s.rang % 15)]
) fs ON TRUE
WHERE NOT EXISTS (SELECT 1 FROM service x WHERE x.libelle = s.libelle);

-- positions (une par nouveau service)
INSERT INTO position (latitude, longitude, altitude, precision_metres, date_releve, id_ville)
SELECT s.lat, s.lng, 15, 10, CURRENT_DATE, (SELECT id_ville FROM ville WHERE libelle = 'Dakar' LIMIT 1)
FROM _svc s
JOIN service sv ON sv.libelle = s.libelle
WHERE NOT EXISTS (
    SELECT 1 FROM information_service i WHERE i.id_service = sv.id_service);

-- fiches prestataires
INSERT INTO information_service (adresse, telephone1, email1, site_web, disponibilite, id_service, id_position)
SELECT s.quartier || ', Dakar',
       '+221 33 8' || lpad(s.rang::text, 2, '0') || ' 00 00',
       'contact.' || sv.id_service || '@gservices-demo.sn',
       'https://demo.gservices.sn/' || sv.id_service,
       (s.rang % 7 <> 0),                                 -- ~1/7 « fermé »
       sv.id_service,
       (SELECT p.id_position FROM position p
        WHERE round(p.latitude::numeric,4) = round(s.lat::numeric,4)
          AND round(p.longitude::numeric,4) = round(s.lng::numeric,4)
        ORDER BY p.id_position DESC LIMIT 1)
FROM _svc s JOIN service sv ON sv.libelle = s.libelle
WHERE NOT EXISTS (SELECT 1 FROM information_service i WHERE i.id_service = sv.id_service);

-- horaires : lundi → samedi, 09:00–18:00 (pause 13:00–14:00)
INSERT INTO horaire (heure_ouverture, heure_fermeture, pause_dejeuner_debut, pause_dejeuner_fin,
                     ouvert24h, id_information_service, id_jour)
SELECT '09:00', '18:00', '13:00', '14:00', FALSE, i.id_information_service, j.id_jour
FROM information_service i
JOIN service sv ON sv.id_service = i.id_service
JOIN _svc s ON s.libelle = sv.libelle
JOIN jour j ON j.numero_jour_semaine BETWEEN 1 AND 6
ON CONFLICT (id_information_service, id_jour) DO NOTHING;

-- 1 catalogue par service
INSERT INTO catalogue (libelle, description, icone, ordre_affichage, id_service)
SELECT 'Prestations — ' || sv.libelle, 'Nos formules et tarifs', 'pi pi-list', 1, sv.id_service
FROM service sv JOIN _svc s ON s.libelle = sv.libelle
WHERE NOT EXISTS (SELECT 1 FROM catalogue c WHERE c.id_service = sv.id_service);

-- 2 produits par catalogue
INSERT INTO produit (libelle, description, id_catalogue)
SELECT (ARRAY['Formule Essentielle','Formule Premium'])[g.n],
       'Offre ' || lower((ARRAY['essentielle','premium'])[g.n]) || ' du prestataire',
       c.id_catalogue
FROM catalogue c
JOIN service sv ON sv.id_service = c.id_service
JOIN _svc s ON s.libelle = sv.libelle
CROSS JOIN generate_series(1, 2) AS g(n)
WHERE NOT EXISTS (
    SELECT 1 FROM produit p WHERE p.id_catalogue = c.id_catalogue
      AND p.libelle = (ARRAY['Formule Essentielle','Formule Premium'])[g.n]);

-- 3 articles par produit
INSERT INTO article (reference, description, prix, devise, mode_vente, disponibilite, promotion, taux_remise_pourcentage, id_produit)
SELECT 'SRV' || sv.id_service || '-P' || p.id_produit || '-A' || g.n,
       p.libelle || ' — option ' || g.n,
       ( (p.libelle = 'Formule Premium')::int * 5000 + 3000 + g.n * 1500 )::numeric,
       'XOF',
       (ARRAY['UNITE','FORFAIT','HEURE'])[g.n],
       TRUE,
       (g.n = 2 AND sv.id_service % 4 = 0),                        -- quelques promos
       CASE WHEN (g.n = 2 AND sv.id_service % 4 = 0) THEN 10 ELSE 0 END,
       p.id_produit
FROM produit p
JOIN catalogue c ON c.id_catalogue = p.id_catalogue
JOIN service sv ON sv.id_service = c.id_service
JOIN _svc s ON s.libelle = sv.libelle
CROSS JOIN generate_series(1, 3) AS g(n)
ON CONFLICT (reference) DO NOTHING;

-- ---------------------------------------------------- avis (≈ 30, modérés) ---
--  Chaque (client, service) au plus une fois : produit cartésien filtré.
INSERT INTO avis (note, commentaire, est_modere, id_personne, id_service, id_annee)
SELECT 3 + ((sv.id_service + cl.rn) % 3),                           -- notes 3..5
       (ARRAY[
          'Service sérieux, je recommande.',
          'Bon rapport qualité-prix, accueil chaleureux.',
          'Prestation correcte, quelques délais.',
          'Très professionnel, rien à redire.',
          'Expérience agréable, je reviendrai.',
          'Personnel à l''écoute, travail soigné.'
       ])[1 + ((sv.id_service + cl.rn) % 6)],
       TRUE,
       cl.id_personne, sv.id_service,
       (SELECT id_annee FROM annee WHERE valeur_annee = 2026)
FROM service sv
JOIN _svc s ON s.libelle = sv.libelle
JOIN LATERAL (
     SELECT id_personne, row_number() OVER (ORDER BY id_personne) AS rn
     FROM personne WHERE login LIKE 'cl.%'
) cl ON ((sv.id_service + cl.rn) % 3 = 0)                            -- ~1 avis / 3 combinaisons
WHERE sv.id_service % 2 = 0                                          -- la moitié des services notés
ON CONFLICT DO NOTHING;
