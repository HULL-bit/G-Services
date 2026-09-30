-- ============================================================================
--  G-SERVICES — V21 : géolocalise davantage de prestataires (carte de l'accueil)
--  Chaque fiche prestataire reçoit sa PROPRE position (pas de marqueurs empilés).
-- ============================================================================

-- Atelier de couture → Casablanca
INSERT INTO position (latitude, longitude, altitude, precision_metres, date_releve, id_ville)
SELECT 33.5731, -7.5898, 50, 15, CURRENT_DATE, v.id_ville FROM ville v WHERE v.libelle = 'Casablanca';

UPDATE information_service i SET id_position = (
    SELECT max(p.id_position) FROM position p JOIN ville v ON v.id_ville = p.id_ville WHERE v.libelle = 'Casablanca')
WHERE i.id_service = (SELECT id_service FROM service WHERE libelle = 'Atelier de couture');

-- Hôtel Teranga → sa propre position (Plateau, Dakar), légèrement à l'écart du restaurant
INSERT INTO position (latitude, longitude, altitude, precision_metres, date_releve, id_ville)
SELECT 14.6708, -17.4390, 12, 10, CURRENT_DATE, v.id_ville FROM ville v WHERE v.libelle = 'Dakar';

UPDATE information_service i SET id_position = (
    SELECT max(p.id_position) FROM position p JOIN ville v ON v.id_ville = p.id_ville WHERE v.libelle = 'Dakar')
WHERE i.id_service = (SELECT id_service FROM service WHERE libelle = 'Hôtel Teranga');
