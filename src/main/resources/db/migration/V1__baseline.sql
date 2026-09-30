-- ============================================================================
--  G-SERVICES — V1 : socle (Lot 0)
--  Le modèle métier complet (paquetages GestServices + GestUser, ~28 tables)
--  est introduit à partir de la migration V2 (Lot 1).
-- ============================================================================

-- Extensions utiles pour la suite (recherche accent-insensible, UUID éventuels).
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Métadonnées applicatives : sert de sonde de connectivité et de journal de socle.
CREATE TABLE gs_app_meta (
    cle           VARCHAR(80)  PRIMARY KEY,
    valeur        VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP    NOT NULL DEFAULT now()
);

COMMENT ON TABLE gs_app_meta IS
    'Métadonnées de la plateforme G-SERVICES — socle Lot 0.';

INSERT INTO gs_app_meta (cle, valeur) VALUES
    ('schema.lot',      'lot-0'),
    ('app.name',        'G-SERVICES'),
    ('app.description', 'Gestion de services et produits géolocalisés');
