-- ============================================================================
--  G-SERVICES — V33 : un gestionnaire est lié à une catégorie (donc à ses
--  fournisseurs), au même titre qu'un fournisseur est lié à son service.
--
--  `personne.id_categorie_specialite` (existant, V25) prend ici un second sens :
--  pour un PRESTATAIRE, c'est la catégorie de l'unique service qu'il possède ;
--  pour un GESTIONNAIRE, c'est la catégorie qu'il supervise dans son ensemble
--  (tous les services, catalogues, stocks, avis, commandes et signalements de
--  cette catégorie — cf. PerimetreFournisseur.servicesAutorises()).
-- ============================================================================

UPDATE personne
SET id_categorie_specialite = (SELECT id_categorie_service FROM categorie_service WHERE libelle = 'Restauration')
WHERE login = 'gestionnaire.demo';
