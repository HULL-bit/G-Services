# G-SERVICES — Comptes de démonstration

> ⚠️ Comptes **de démonstration uniquement**. Les mots de passe sont volontairement
> simples et le hash est commun. **À supprimer / réinitialiser hors démonstration.**
> Application : <http://localhost:8090> · connexion : `/login.xhtml`.

Après connexion, la redirection dépend du rôle :
administrateur → tableau de bord · fournisseur → écran Catalogue · client → site public.

---

## 1. Administration

| Login | Mot de passe | Profil | Accès |
|---|---|---|---|
| `superadmin` | `Admin@2026` | `SUPER_ADMIN` | tout le back-office (`/admin/**`), toutes les permissions |
| `admin.demo` | `password` | `ADMIN` | presque tout : gère utilisateurs/fournisseurs/clients, catalogue, stock, avis, **signalements et sanctions** — pas la gestion des profils/permissions elles-mêmes ; vue globale (non lié à une catégorie) |
| `gestionnaire.demo` | `password` | `GESTIONNAIRE`, **catégorie Restauration** | stock, avis, commandes, signalements — **limité aux services de sa catégorie**, comme un fournisseur est limité à son service (voir ci-dessous) |

Le compte `superadmin` est créé au premier démarrage par `SecuriteDataInitializer`
(paramétrable via `SECURITE_BOOTSTRAP_*`, cf. `.env.example`). `admin.demo` et
`gestionnaire.demo` sont semés par la migration `V32` — seuls comptes permettant de
tester ces deux profils (aucun n'existait avant).

### Un gestionnaire est lié à une catégorie (donc à ses fournisseurs)

Comme un fournisseur ne gère que son unique service, un **gestionnaire est rattaché à
une catégorie** (`personne.id_categorie_specialite`, migration `V33`) et son
back-office est **restreint à cette catégorie** (`PerimetreFournisseur`, même
mécanisme que pour les fournisseurs) : stock & fiches prestataires, avis à modérer,
commandes à traiter, signalements et services bloqués — uniquement ceux des services
de sa catégorie. Sans catégorie assignée (champ « Catégorie de rattachement » vide
dans la fiche Personne), un gestionnaire garde la vue globale.

Back-office dédié (menu **Fournisseurs & clients**, branche 8000) :
- **Fournisseurs** (`/admin/sec/fournisseurs.xhtml`) — liste, création d'un compte fournisseur
  (login + mot de passe + catégorie de spécialité + service à ouvrir), activation/désactivation.
- **Clients** (`/admin/sec/clients.xhtml`) — idem pour les clients.
- **File de validation** (`/admin/sec/validations.xhtml`) — comptes auto-inscrits et services
  créés par les fournisseurs, à **valider** ou **refuser**.

Un compte auto-inscrit (via `/public/inscription.xhtml`, au choix **client** ou **fournisseur**)
reste **en attente** : il ne peut pas se connecter (message dédié) tant qu'un admin ne l'a pas
validé. Un service créé par un fournisseur est **invisible sur le front public** jusqu'à validation.

Comptes de démonstration en attente : `client.attente` et `fournisseur.attente`
(mot de passe `password`) + le service « Snack Le Petit Dej ».

---

## 2. Fournisseurs de services (`PRESTATAIRE`)

**1 fournisseur = 1 service.** Chaque fournisseur gère **un seul service** (son
activité) et ses éventuels sous-services : son arborescence, son catalogue, ses
produits, ses articles (caractéristiques « chemise bleu XXL », prix, images…),
ses stocks/lots, sa fiche et sa **localisation GPS**. Il **n'a accès à rien d'autre**.
Dans son espace Catalogue : onglets *Arborescence*, *Catalogues*, *Offres*,
*Propriétés* et *Unités* (pour décrire ses articles) — **pas** l'onglet *Catégories*.
Il **ajoute / modifie** ses offres et **répond aux avis** de son service.

Tous les mots de passe : `password`. Il y a **un compte fournisseur par service**
(48 au total). Les 17 comptes nommés :

| Login | Nom | Service géré |
|---|---|---|
| `presta.resto` | Ousmane Sarr | Restaurant traditionnel |
| `presta.hotel` | Aïda Fall | Hôtel Teranga |
| `fs.ba` | Rokhaya Ba | Restauration rapide |
| `fs.camara` | Lamine Camara | Atelier de couture |
| `fs.diagne` | Sokhna Diagne | Ménage & repassage |
| `fs.diallo` | Fatou Diallo | Prêt-à-porter homme |
| `fs.diouf` | Serigne Diouf | Chez Adja — cuisine sénégalaise |
| `fs.faye` | Ndeye Faye | Le Baobab Grill |
| `fs.gueye` | Modou Gueye | Fast Teranga |
| `fs.kane` | Abdou Kane | Pâtisserie des Almadies |
| `fs.mbaye` | Pape Mbaye | Dibiterie Point E |
| `fs.ndiaye2` | Alioune Ndiaye | Café Ngor |
| `fs.ndour` | Cheikh Ndour | Atelier Couture Bineta |
| `fs.sene` | Awa Sène | Wax & Style |
| `fs.sow` | Mariama Sow | Cordonnerie Moderne |
| `fs.sy` | Ibrahima Sy | Retouche Express Yoff |
| `fs.thiam` | Bineta Thiam | Ménage Plus |

Les ~31 autres services ont un compte généré `f.svc<id_service>` (ex. `f.svc19`
gère « Jardinage Teranga »). Liste complète et mapping service → fournisseur :

```sql
SELECT s.libelle AS service, cs.libelle AS categorie, p.login, p.prenom||' '||p.nom AS fournisseur
FROM service s
JOIN categorie_service cs ON cs.id_categorie_service = s.id_categorie_service
JOIN personne p ON p.id_personne = s.id_proprietaire
WHERE s.id_service_parent IS NULL
ORDER BY cs.libelle, s.libelle;
```

> L'administrateur voit **tous** les fournisseurs et **tous** les clients (écran
> *Utilisateurs & sécurité*, colonne « Spécialité »), ainsi que le **stock de chaque
> fournisseur** (écran *Stock & prestataires*, colonne « Service / catégorie »).

---

## 3. Clients (`CLIENT`)

Visiteurs inscrits : ils déposent un avis (une fois par prestataire) et le suivent.
Un visiteur peut créer son propre compte **au moment où il veut laisser un avis**
(bouton sur la fiche prestataire → `/public/inscription.xhtml`).

| Login | Mot de passe | Nom |
|---|---|---|
| `awa.diop` | `password` | Awa Diop |
| `karim.ba` | `password` | Karim Ba |
| `marie.ndiaye` | `password` | Marie Ndiaye |
| `cl.sarr` | `password` | Khadija Sarr |
| `cl.toure` | `password` | Malick Touré |
| `cl.diop` | `password` | Aminata Diop |
| `cl.fall` | `password` | Ousseynou Fall |
| `cl.ndao` | `password` | Coumba Ndao |
| `cl.cisse` | `password` | Babacar Cissé |
| `cl.wade` | `password` | Sophie Wade |
| `cl.gomis` | `password` | Jean Gomis |

---

## 3 bis. Commandes à distance

Certains services acceptent la **vente en ligne** (`service.commande_a_distance`,
coché dans Catalogue → service). Sur leur fiche publique, un client **connecté**
voit un tunnel « Commander en ligne » (choix des articles + quantités + adresse).

- Catégories commandables en démo : Restauration, Mode & textile, Hôtellerie,
  Artisanat, Événementiel (`V27`, ~27 services, ~12 commandes de démonstration).
- Back-office : écran **Commandes** (branche `7000`). L'administrateur voit tout ;
  le **fournisseur ne voit que les commandes de son service**.
- Cycle : `NOUVELLE → CONFIRMEE → EN_PREPARATION → EXPEDIEE → LIVREE`
  (+ `ANNULEE` tant que non livrée).
- Permissions : `SUPER_ADMIN` 7001-7004 ; `ADMIN`/`GESTIONNAIRE` 7001/7002/7004 ;
  `PRESTATAIRE` 7001/7002.

---

## 4. Visiteur anonyme

Aucun compte. Accès libre à tout le front public :

- `/` — accueil + carte des prestataires
- `/public/carte.xhtml` — carte complète de Dakar avec filtres (catégorie, note, disponibilité, recherche)
- `/public/catalogue.xhtml` — catalogue par catégorie
- `/public/service.xhtml?id=…` — fiche d'un prestataire (offre, horaires, carte, avis)

La connexion n'est demandée que pour **laisser un avis**.

---

## 5. Régénérer / changer un mot de passe

Le hash BCrypt commun aux comptes de démo (`password`) est
`$2a$10$Cf2ORSViE/Ck2LbK6RlqrO7ga7TFDuW3yzSlzJhIvTvveklOafI2S`.

Générer un nouveau hash :

```bash
htpasswd -bnBC 10 "" "monNouveauMotDePasse" | cut -d: -f2 | sed 's/^\$2y/\$2a/'
```

Puis en base :

```sql
UPDATE personne SET password = '<hash>' WHERE login = '<login>';
```

Depuis le back-office : écran **Utilisateurs & sécurité → Personnes → Réinitialiser le mot de passe**.
