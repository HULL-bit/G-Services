-- ============================================================================
--  G-SERVICES — V24 : « Atelier de couture » relocalisé à Dakar
--  La carte publique ne présente que des prestataires de Dakar.
-- ============================================================================

INSERT INTO position (latitude, longitude, altitude, precision_metres, date_releve, id_ville)
SELECT 14.6835, -17.4545, 12, 10, CURRENT_DATE, (SELECT id_ville FROM ville WHERE libelle = 'Dakar' LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM position p
    WHERE round(p.latitude::numeric,4) = 14.6835 AND round(p.longitude::numeric,4) = -17.4545);

UPDATE information_service i
SET id_position = (SELECT p.id_position FROM position p
                   WHERE round(p.latitude::numeric,4) = 14.6835 AND round(p.longitude::numeric,4) = -17.4545
                   ORDER BY p.id_position DESC LIMIT 1),
    adresse = 'Médina, Dakar'
WHERE i.id_service = (SELECT id_service FROM service WHERE libelle = 'Atelier de couture');
