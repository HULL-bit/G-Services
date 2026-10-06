-- ============================================================================
--  G-SERVICES — V34 : horodatage du stock et des lots.
--
--  `lot` n'avait aucune trace système de son enregistrement (seule `date_entree`
--  existait, une date métier saisie par l'utilisateur). `stock` avait bien
--  `date_creation` mais rien pour tracer la dernière modification (quantité
--  recalculée, seuil d'alerte changé). On ajoute :
--    - lot.date_creation   : horodatage de création (immuable, défaut now()).
--    - stock.date_modification : horodatage de dernière mise à jour, rafraîchi
--      à chaque UPDATE (défaut now() pour les lignes existantes).
-- ============================================================================

ALTER TABLE lot
    ADD COLUMN date_creation TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE lot
    ALTER COLUMN date_creation DROP DEFAULT;

ALTER TABLE stock
    ADD COLUMN date_modification TIMESTAMP;

UPDATE stock SET date_modification = date_creation;
