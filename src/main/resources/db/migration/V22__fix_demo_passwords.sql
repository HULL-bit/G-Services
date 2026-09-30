-- ============================================================================
--  G-SERVICES — V22 : correction des mots de passe des comptes de démonstration
--
--  Le hash BCrypt utilisé dans V17 / V20 était invalide (mauvaise valeur de
--  référence). On le remplace par un hash BCrypt vérifié du mot de passe
--  « password ». À CHANGER hors démonstration.
-- ============================================================================

UPDATE personne
SET password = '$2a$10$Cf2ORSViE/Ck2LbK6RlqrO7ga7TFDuW3yzSlzJhIvTvveklOafI2S'
WHERE login IN ('awa.diop', 'karim.ba', 'marie.ndiaye', 'presta.resto', 'presta.hotel');
