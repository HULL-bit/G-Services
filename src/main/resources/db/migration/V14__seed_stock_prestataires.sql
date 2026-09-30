-- ============================================================================
--  G-SERVICES — V14 : jeu de démonstration Stock & prestataires (Lot 4)
-- ============================================================================

-- ---------------------------------------------------- stocks ----------------
INSERT INTO stock (quantite_totale, seuil_alerte, id_article)
SELECT s.qte, s.seuil, a.id_article
FROM (VALUES
    (0,  10, 'BIS-33'),     -- recalculé sur les lots ci-dessous (stock sain)
    (0,  15, 'BIS-100'),    -- restera sous le seuil : en alerte
    (0,   5, 'PDJ-THIEB')   -- aucun lot : rupture
) AS s(qte, seuil, ref)
JOIN article a ON a.reference = s.ref
ON CONFLICT (id_article) DO NOTHING;

-- ---------------------------------------------------- lots ------------------
INSERT INTO lot (numero_lot, quantite, prix_achat, date_entree, date_peremption, id_stock, id_mois)
SELECT l.num, l.qte, l.prix, l.entree, l.perem, st.id_stock, m.id_mois
FROM (VALUES
    ('BIS33-2027-01',  80, 250, DATE '2027-01-15', DATE '2027-12-31', 'BIS-33', 2027, 1),
    ('BIS33-2026-02',  20, 240, DATE '2026-02-10', DATE '2026-05-31', 'BIS-33', 2026, 2),  -- périmé -> exclu du disponible
    ('BIS100-2027-02', 12, 700, DATE '2027-02-05', DATE '2027-12-31', 'BIS-100', 2027, 2)
) AS l(num, qte, prix, entree, perem, ref, an, mo)
JOIN article a ON a.reference = l.ref
JOIN stock st ON st.id_article = a.id_article
JOIN annee y ON y.valeur_annee = l.an
JOIN mois m ON m.id_annee = y.id_annee AND m.numero_mois = l.mo;

-- Recale la quantité de référence sur le disponible réel (lots actifs non périmés)
UPDATE stock st SET quantite_totale = COALESCE((
    SELECT SUM(lo.quantite) FROM lot lo
    WHERE lo.id_stock = st.id_stock AND lo.etat = TRUE
      AND (lo.date_peremption IS NULL OR lo.date_peremption >= CURRENT_DATE)
), 0);

-- ---------------------------------------------------- fiche prestataire -----
INSERT INTO information_service (adresse, telephone1, email1, site_web, disponibilite, id_service, id_position)
SELECT 'Avenue Léopold Sédar Senghor, Dakar', '+221 33 800 00 00', 'contact@restaurant-dakar.sn',
       'https://restaurant-dakar.sn', TRUE, s.id_service, p.id_position
FROM service s
LEFT JOIN position p ON p.id_ville = (SELECT id_ville FROM ville WHERE libelle = 'Dakar' LIMIT 1)
WHERE s.libelle = 'Restaurant traditionnel'
LIMIT 1;

INSERT INTO information_service (adresse, telephone1, email1, disponibilite, id_service)
SELECT '15 rue de la Paix, Casablanca', '+212 5 22 00 00 00', 'atelier@couture-casa.ma', TRUE, s.id_service
FROM service s WHERE s.libelle = 'Atelier de couture' LIMIT 1;

-- ---------------------------------------------------- horaires --------------
INSERT INTO horaire (heure_ouverture, heure_fermeture, pause_dejeuner_debut, pause_dejeuner_fin, ouvert24h,
                     id_information_service, id_jour)
SELECT h.ouv, h.fer, h.pd, h.pf, FALSE, i.id_information_service, j.id_jour
FROM (VALUES
    ('11:00', '23:00', '15:00', '18:00', 1),
    ('11:00', '23:00', '15:00', '18:00', 2),
    ('11:00', '23:00', '15:00', '18:00', 3),
    ('11:00', '23:00', '15:00', '18:00', 4),
    ('11:00', '23:30', '15:00', '18:00', 5),
    ('11:00', '23:30', NULL,    NULL,    6)
) AS h(ouv, fer, pd, pf, jour)
JOIN information_service i ON i.id_service = (SELECT id_service FROM service WHERE libelle = 'Restaurant traditionnel' LIMIT 1)
JOIN jour j ON j.numero_jour_semaine = h.jour
ON CONFLICT (id_information_service, id_jour) DO NOTHING;
