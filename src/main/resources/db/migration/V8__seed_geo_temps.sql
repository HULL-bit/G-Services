-- ============================================================================
--  G-SERVICES — V8 : jeu de démonstration Géographie & temps (Lot 2)
-- ============================================================================

-- ---------------------------------------------------- continents ------------
INSERT INTO continent (libelle, code, superficie_km2, population) VALUES
    ('Afrique',      'AF', 30370000, 1400000000),
    ('Europe',       'EU', 10180000,  748000000),
    ('Asie',         'AS', 44579000, 4700000000),
    ('Amérique',     'AM', 42549000, 1030000000),
    ('Océanie',      'OC',  8600000,   45000000),
    ('Antarctique',  'AN', 14000000,          0)
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------- zones -----------------
INSERT INTO zone_geographique (libelle, code, description, id_continent)
SELECT z.libelle, z.code, z.descr, c.id_continent
FROM (VALUES
    ('Afrique de l''Ouest', 'AF-W',  'CEDEAO et voisins',            'AF'),
    ('Afrique du Nord',     'AF-N',  'Maghreb et Égypte',            'AF'),
    ('Afrique centrale',    'AF-C',  'Bassin du Congo',              'AF'),
    ('Europe de l''Ouest',  'EU-W',  'France, Benelux, Îles Britanniques', 'EU'),
    ('Europe du Sud',       'EU-S',  'Péninsules ibérique et italienne',   'EU')
) AS z(libelle, code, descr, cont)
JOIN continent c ON c.code = z.cont
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------- pays ------------------
INSERT INTO pays (libelle, code_iso2, code_iso3, indicatif_telephonique, devise,
                  capitale, langue_officielle, superficie_km2, population, id_zone_geographique)
SELECT p.libelle, p.i2, p.i3, p.ind, p.dev, p.cap, p.lang, p.surf, p.pop, z.id_zone_geographique
FROM (VALUES
    ('Sénégal',        'SN', 'SEN', '+221', 'XOF', 'Dakar',         'Français', 196722,   17700000, 'AF-W'),
    ('Côte d''Ivoire', 'CI', 'CIV', '+225', 'XOF', 'Yamoussoukro',  'Français', 322463,   28000000, 'AF-W'),
    ('Mali',           'ML', 'MLI', '+223', 'XOF', 'Bamako',        'Français', 1240192,  22000000, 'AF-W'),
    ('Maroc',          'MA', 'MAR', '+212', 'MAD', 'Rabat',         'Arabe',    446550,   37000000, 'AF-N'),
    ('Tunisie',        'TN', 'TUN', '+216', 'TND', 'Tunis',         'Arabe',    163610,   12000000, 'AF-N'),
    ('France',         'FR', 'FRA', '+33',  'EUR', 'Paris',         'Français', 551695,   68000000, 'EU-W'),
    ('Belgique',       'BE', 'BEL', '+32',  'EUR', 'Bruxelles',     'Français', 30528,    11700000, 'EU-W')
) AS p(libelle, i2, i3, ind, dev, cap, lang, surf, pop, zone)
JOIN zone_geographique z ON z.code = p.zone
ON CONFLICT (code_iso2) DO NOTHING;

-- ---------------------------------------------------- regions --------------
INSERT INTO region (libelle, code_region, chef_lieu, population, id_pays)
SELECT r.libelle, r.code, r.chef, r.pop, pa.id_pays
FROM (VALUES
    ('Région de Dakar',       'SN-DK', 'Dakar',        3900000, 'SN'),
    ('Région de Thiès',       'SN-TH', 'Thiès',        2000000, 'SN'),
    ('District d''Abidjan',   'CI-AB', 'Abidjan',      6300000, 'CI'),
    ('Casablanca-Settat',     'MA-06', 'Casablanca',   7000000, 'MA'),
    ('Île-de-France',         'FR-IDF','Paris',       12300000, 'FR')
) AS r(libelle, code, chef, pop, pays)
JOIN pays pa ON pa.code_iso2 = r.pays;

-- ---------------------------------------------------- villes ---------------
INSERT INTO ville (libelle, code_postal, indicatif_zone, population, est_capitale, id_region)
SELECT v.libelle, v.cp, v.ind, v.pop, v.cap, rg.id_region
FROM (VALUES
    ('Dakar',       '10000', '33',  1200000, TRUE,  'SN-DK'),
    ('Pikine',      '11000', '33',  1100000, FALSE, 'SN-DK'),
    ('Thiès',       '21000', '33',   350000, FALSE, 'SN-TH'),
    ('Abidjan',     '00225', '27',  4700000, FALSE, 'CI-AB'),
    ('Casablanca',  '20000', '522', 3350000, FALSE, 'MA-06'),
    ('Paris',       '75000', '1',   2100000, TRUE,  'FR-IDF')
) AS v(libelle, cp, ind, pop, cap, region)
JOIN region rg ON rg.code_region = v.region;

-- ---------------------------------------------------- positions -----------
INSERT INTO position (latitude, longitude, altitude, precision_metres, date_releve, id_ville)
SELECT p.lat, p.lng, p.alt, 10, CURRENT_DATE, vi.id_ville
FROM (VALUES
    (14.7167, -17.4677,  22, 'Dakar'),
    ( 5.3599, -4.0083,   18, 'Abidjan'),
    (33.5731, -7.5898,   50, 'Casablanca'),
    (48.8566,  2.3522,   35, 'Paris')
) AS p(lat, lng, alt, ville)
JOIN ville vi ON vi.libelle = p.ville;

-- ---------------------------------------------------- jours ----------------
INSERT INTO jour (libelle, abreviation, numero_jour_semaine, est_weekend) VALUES
    ('Lundi',    'Lun', 1, FALSE),
    ('Mardi',    'Mar', 2, FALSE),
    ('Mercredi', 'Mer', 3, FALSE),
    ('Jeudi',    'Jeu', 4, FALSE),
    ('Vendredi', 'Ven', 5, FALSE),
    ('Samedi',   'Sam', 6, TRUE),
    ('Dimanche', 'Dim', 7, TRUE)
ON CONFLICT (numero_jour_semaine) DO NOTHING;

-- ---------------------------------------------------- années + mois -------
--  2024 (bissextile), 2025, 2026, 2027.
INSERT INTO annee (libelle, valeur_annee, est_bissextile, date_debut, date_fin)
SELECT y::text, y,
       (y % 4 = 0 AND (y % 100 <> 0 OR y % 400 = 0)),
       make_date(y, 1, 1), make_date(y, 12, 31)
FROM generate_series(2024, 2027) AS y
ON CONFLICT (valeur_annee) DO NOTHING;

INSERT INTO mois (libelle, abreviation, numero_mois, nombre_jours, id_annee)
SELECT m.lib, m.abr, m.num,
       CASE m.num
           WHEN 2 THEN CASE WHEN a.est_bissextile THEN 29 ELSE 28 END
           WHEN 4 THEN 30 WHEN 6 THEN 30 WHEN 9 THEN 30 WHEN 11 THEN 30
           ELSE 31
       END,
       a.id_annee
FROM annee a
CROSS JOIN (VALUES
    ('Janvier','Jan',1),('Février','Fév',2),('Mars','Mar',3),('Avril','Avr',4),
    ('Mai','Mai',5),('Juin','Juin',6),('Juillet','Juil',7),('Août','Août',8),
    ('Septembre','Sep',9),('Octobre','Oct',10),('Novembre','Nov',11),('Décembre','Déc',12)
) AS m(lib, abr, num)
ON CONFLICT (id_annee, numero_mois) DO NOTHING;
