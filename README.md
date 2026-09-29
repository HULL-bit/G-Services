# G-SERVICES

> Plateforme de gestion de **services & produits géolocalisés** — back-office
> d'administration complet + front public au design cinématique.

**État : tous les lots livrés (0 → 6).** Le modèle UML complet (~28 classes,
2 paquetages, 4 classes d'association) est implémenté en base, en JPA, en service
et administrable via l'IHM. Back-office RBAC complet + front public.
Validé de bout en bout par `docker compose up --build` (19 migrations Flyway,
contexte Spring + Mojarra/PrimeFaces, jeux de démonstration).

---

## 1. Stack

| Couche | Techno |
|---|---|
| Langage | Java 21 |
| Framework | Spring Boot 3.3.5 |
| IHM | Jakarta Faces (Mojarra) + PrimeFaces 14 via **JoinFaces 5.3.5** |
| ORM | Spring Data JPA / Hibernate 6 |
| Sécurité | Spring Security 6 (RBAC complet au Lot 1) |
| Base | PostgreSQL 16 |
| Migrations | Flyway |
| Mapping DTO | MapStruct |
| Build | Maven (WAR exécutable) |
| Tests | JUnit 5, Testcontainers |
| Conteneurisation | Docker + Docker Compose |

> JoinFaces 5.3.5 est **la** ligne alignée sur Spring Boot 3.3.5 ; elle embarque
> PrimeFaces 14.0.x et Mojarra 4.0 (Jakarta Faces 4.0).

---

## 2. Démarrage — une seule commande

Prérequis : **Docker** + **Docker Compose v2**. Rien d'autre (ni Java, ni Maven,
ni PostgreSQL sur la machine hôte).

```bash
cp .env.example .env        # (optionnel) adapter les mots de passe
docker compose up --build
```

| Service | URL | Détails |
|---|---|---|
| Application G-SERVICES | http://localhost:8080 | redirige vers l'accueil animé |
| Health check | http://localhost:8080/actuator/health | `{"status":"UP"}` |
| pgAdmin | http://localhost:5050 | `admin@gservices.com` / `admin` (voir `.env`) |
| PostgreSQL | `localhost:5432` | base `gservices` / user `gservices` |

Arrêt : `docker compose down` — les données Postgres persistent dans le volume
`gservices_pgdata` (`docker compose down -v` pour tout effacer).

### Connexion pgAdmin → base

Dans pgAdmin : *Add New Server* → onglet *Connection* :
`Host = db`, `Port = 5432`, `Username = gservices`, `Password = gservices`.

---

## 3. Développement local (sans Docker pour l'app)

```bash
# 1. une base Postgres seule
docker compose up -d db
# 2. l'app en local (JDK 21 requis)
./mvnw spring-boot:run          # ou : mvn spring-boot:run
```

`application.yml` pointe par défaut sur `localhost:5432` / `gservices` / `gservices`.

### Tests

```bash
mvn test                                   # 20 tests unitaires (aucune dépendance)
mvn test -Dtest=GservicesApplicationTests   # 1 test d'intégration (Testcontainers)
```

**Tests unitaires** (`src/test/java/com/gservices/`) — couvrent les règles de
gestion §10, sans base ni contexte Spring :

| Classe | Règle vérifiée |
|---|---|
| `entity/ArticleRegleTest` | prix net = prix remisé si `promotion` + taux > 0 ; arrondi au centime |
| `entity/StockRegleTest` | disponible = Σ lots actifs **non périmés** ; alerte si disponible ≤ seuil |
| `entity/AvisRegleTest` | avis public ⇔ `estModere` **et** `etat` |
| `entity/AnneeBissextileTest` | années bissextiles ; février 28/29 |
| `mapper/CatalogueMapperTest` | mappings dérivés (prix net, nb variétés, drapeau emoji ISO-2) |
| `security/GservicesUserDetailsTest` | autorités RBAC : rôles/profils actifs, **surcharges `PersonnePermission`** (grant/deny), verrouillage |

**Test d'intégration** — démarre un PostgreSQL 16 jetable via Testcontainers,
applique **toutes** les migrations, charge le contexte complet (JSF inclus) et
vérifie les jeux de démo + les règles stock/avis en base réelle.

> Testcontainers requiert un daemon exposant l'**API Docker ≥ 1.40** (Docker
> standard convient). Sous **Podman** : `systemctl --user enable --now podman.socket`
> puis `export DOCKER_HOST=unix://$XDG_RUNTIME_DIR/podman/podman.sock` et
> `export TESTCONTAINERS_RYUK_DISABLED=true` (la mise en réseau conteneur-dans-conteneur
> peut rester capricieuse). **La validation de référence reste `docker compose up --build`**,
> qui exerce le même chemin (contexte + 19 migrations + Mojarra/PrimeFaces) à chaque lancement.

---

## 4. Comptes de test

| Login | Mot de passe | Profil | Usage |
|---|---|---|---|
| `superadmin` | `Admin@2026` | `SUPER_ADMIN` | back-office complet → tableau de bord |
| `presta.resto`, `presta.hotel` | `password` | `PRESTATAIRE` | propriétaires de services → écran Catalogue + réponse aux avis de leurs services sur le front public |
| `awa.diop`, `karim.ba`, `marie.ndiaye` | `password` | `CLIENT` | visiteurs — dépôt d'avis sur le front public |

Un visiteur peut aussi **créer son compte** sur `/public/inscription.xhtml` (profil `CLIENT`).
Après connexion, la redirection dépend du rôle (tableau de bord / écran Catalogue / site public).
*(Comptes de démo : seed V17/V20, mot de passe corrigé par V22 — à supprimer hors démonstration.)*

Le compte `superadmin` est créé au **premier démarrage** par
`SecuriteDataInitializer` si le login n'existe pas (mot de passe haché BCrypt,
donc pas semé en SQL). Paramétrable via `SECURITE_BOOTSTRAP_*` (voir
`.env.example`). **À changer après la première connexion.**

- Front public : `http://localhost:8080/` — ouvert à tous.
- Back-office : `http://localhost:8080/admin/index.xhtml` — authentification requise
  (`/login.xhtml`), verrouillage du compte après **5 échecs** pendant **15 min**,
  « se souvenir de moi » 14 jours. Le **menu latéral est généré dynamiquement**
  à partir des `BranchePermission` / `Permission` détenues par l'utilisateur.
- Toute la sécurité (personnes, profils, permissions, branches, rôles, droits)
  est regroupée sur **`/admin/sec/securite.xhtml`** (onglets).

> **Codes RBAC numériques** : `BranchePermission.code` est attribué
> automatiquement (1000, 2000, 3000…) ; `Permission.code` dérive de sa branche
> (branche 2000 → 2001, 2002…). Les autorités Spring utilisent ces codes ;
> les constantes lisibles sont dans `security/Perms.java`.

> **Port local** : si 8080/5432 sont déjà pris, un fichier `.env` peut décaler
> les ports (ex. `APP_PORT=8090`). Voir `.env.example`.

---

## 5. Architecture

Architecture n-tiers en couches (MVC-2) :

```
entity → repository → service (interface + impl) → dto + mapper (MapStruct)
                                   ↓
                        web/bean (Managed Beans JSF) → *.xhtml (Facelets + PrimeFaces)
```

À la racine du dépôt, hors projet Maven :
`modelisation/` (diagramme de classes corrigé + `.moo` + `userPD.pdf`) et
`media/` (médias sources d'origine, non compressés — les versions web servies
par l'app sont sous `src/main/webapp/resources/media/`). Les deux sont exclus
du contexte de build Docker (`.dockerignore`).

```
src/main/
├── java/com/gservices/
│   ├── GservicesApplication.java · ServletInitializer.java
│   ├── config/                       # WebConfig (i18n), CacheConfig, JsfConfig
│   ├── entity/                       # ~30 entités JPA (dont AbstractEntity, Sexe, PersonnePermission hors-UML)
│   ├── repository/                   # Spring Data JPA (dérivées + @EntityGraph + @Query)
│   ├── service/  + service/impl/     # 1 service cohérent par module + GeographieService/TempsService/CatalogueService/StockService/AvisService/VitrineService
│   ├── dto/  + mapper/               # DTO + mappers MapStruct (componentModel = spring)
│   ├── security/                     # SecurityConfig, GservicesUserDetails(Service), Perms, SecuriteEventListener, SecuriteDataInitializer
│   ├── web/bean/                     # Managed Beans (@Component @Scope("view")) : HomeBean, MenuBean, DashboardBean, PersonneBean, GeographieBean, CatalogueBean, StockBean, AvisBean, VitrineBean, PermsBean…
│   ├── web/util/                     # LazyModelService (pont Page<Dto> ↔ LazyDataModel)
│   └── exception/                    # BusinessException, ResourceNotFoundException + handler JSF
├── resources/
│   ├── application.yml               # config 12-factor (surcharge intégrale par env)
│   ├── db/migration/V1..V19__*.sql   # Flyway — schéma (V1/2/7/10/13/16), seed (V3/8/11/14/17/19), perms (V4/9/12/15/18), refonte codes (V5), hors-UML (V6)
│   └── i18n/messages_{fr,en}.properties
└── webapp/
    ├── WEB-INF/{web.xml, faces-config.xml}
    ├── template/{layout.xhtml, admin.xhtml}   # front public / back-office
    ├── public/{index, catalogue, service, error}.xhtml
    ├── admin/
    │   ├── index.xhtml                         # tableau de bord
    │   ├── sec/securite.xhtml (+ _*.xhtml)     # sécurité (onglets)
    │   ├── geo/{geographie,temps}.xhtml
    │   ├── cat/catalogue.xhtml (+ _*.xhtml)    # 6 onglets dont Arborescence + Offre
    │   ├── stock/stock.xhtml
    │   └── avis/moderation.xhtml
    └── resources/
        ├── css/{theme, animations, dark, admin, vitrine}.css
        ├── js/{effects.js, geo.js}             # anims ; carte Leaflet/OSM
        └── media/                              # ⭐ voir §7
```

### Choix structurants

- **WAR exécutable** : `java -jar gservices.war` embarque Tomcat — aucun serveur
  d'application à installer (reste déployable en externe via `ServletInitializer`).
- **Flyway seul maître du schéma** : Hibernate en `ddl-auto: validate`.
- **Un service cohérent par module** (pas un service par entité) : `GeographieService`,
  `CatalogueService`, `StockService`, `AvisService` regroupent les opérations d'un
  bounded context, consommés par un unique Managed Bean `@Scope("view")`.
- **RBAC numérique** : `BranchePermission.code` auto (1000, 2000…), `Permission.code`
  dérivé (2001, 2002…). Autorités Spring = ces codes + `ROLE_<PROFIL>` + surcharges
  `PersonnePermission`. Constantes lisibles : `security/Perms.java`, exposées à l'EL
  via `PermsBean` (`#{perms.PERSONNE_LIRE}`).
- **Tableaux paginés côté serveur** partout (`LazyDataModel`) — jamais de chargement complet.
- **i18n unifié** : Spring et JSF lisent `i18n/messages*` (`#{msg['clé']}`).
- **Thème sombre** : `data-theme` sur `<html>`, mémorisé en `localStorage`,
  anti-flash inline, `prefers-color-scheme` respecté ; `prefers-reduced-motion`
  coupe vidéos et révélations. Animations GPU only (`transform`/`opacity`).
- **Carte** : Leaflet + tuiles OpenStreetMap (aucune clé d'API), dégrade proprement hors-ligne.
- **Piège JSF récurrent** : `rendered` n'existe **pas** sur une balise HTML pure
  (`<div>`, `<p>`, `<span>`…) — toujours envelopper dans `<ui:fragment rendered>`.

---

## 6. Design system

Tokens CSS dans `:root` (`theme.css`) :

| Token | Valeur | Usage |
|---|---|---|
| `--gs-primary` | `#1F3A5F` | bleu nuit |
| `--gs-primary-2` | `#2E5C8A` | bleu |
| `--gs-accent` | `#C9A227` | or (CTA, accents) |
| `--gs-danger` | `#E63946` | promo / erreurs |
| `--gs-success` | `#2F855A` | succès |
| `--gs-radius` | `16px` | arrondis |
| `--gs-ease` | `cubic-bezier(.22,1,.36,1)` | courbe d'animation |

Typo : **Poppins** (titres) + **Inter** (corps) via Google Fonts, avec pile de
repli système.

Effets livrés au Lot 0 : hero vidéo + dégradé animé + machine à écrire,
apparition au scroll en cascade, compteurs animés (count-up), parallaxe légère,
carrousel autoplay (pause au survol, indicateurs animés), ripple sur boutons,
bascule de thème animée, `p:growl` stylé.

---

## 7. Médias — dossier `/media` (source de vérité du design)

Tous les visuels d'ambiance proviennent **exclusivement** de fichiers réels,
référencés via le mécanisme de ressources JSF (`#{resource['media/…']}`) — aucune
URL externe, aucun nom inventé.

### 7.1 Fichiers fournis et leur affectation

| Fichier fourni (`/media`) | Fichier web (`resources/media/…`) | Écran / section | Rôle |
|---|---|---|---|
| `videos.mp4` *(déposé par l'utilisateur dans `backgrounds/`)* | `backgrounds/videos.mp4` + `.webm` + `videos-poster.jpg` | Accueil — **Hero** | Vidéo de fond plein écran, muette, en boucle |
| `pexels-pixabay-38271.jpg` | `backgrounds/discover-map.jpg` | Accueil — section « Découvrez autour de vous » | Fond parallaxe (main + tablette + carte) |
| `pexels-theo-decker-5448171.jpg` | `carousel/city-navigation.jpg` | Carrousel vitrine — slide 1 | Visuel prestataire |
| `pexels-asysin-8892463.jpg` | `carousel/on-the-road.jpg` | Carrousel vitrine — slide 2 | Visuel prestataire |
| `pexels-nathanjhilton-17500375.jpg` | `carousel/cafe-discovery.jpg` | Carrousel vitrine — slide 3 | Visuel prestataire |
| `5834558-uhd_2160_3840_24fps.mp4` | `video/geoloc-map.mp4` + `.webm` + poster | Accueil — section « géolocalisation » (split) | Vidéo d'illustration (carte sur mobile) |
| `videos2.mp4` | `video/app-constellation.mp4` + poster | Accueil — section « Catalogue » | Vidéo de fond assombrie |

*(Le slide 4 du carrousel réutilise `backgrounds/discover-map.jpg`.)*
Les images/vidéos ont été recompressées (images < 300 Ko, vidéos ~1,7 Mo, `+faststart`).
Les originaux restent dans `/media` à la racine.

### 7.2 ⚠️ Placeholders temporaires à remplacer

| Emplacement | Problème | À déposer |
|---|---|---|
| `resources/media/backgrounds/videos.mp4` | **Filigrane « iStock by Getty Images »** au centre de l'image (utilisation validée par l'utilisateur en attendant) | Une vidéo de hero libre de droits, paysage, ~1920×1080, muette, 10–20 s, < 3 Mo (sources : Coverr, Pexels Video, Mixkit). Remplacer le fichier en gardant le nom, ou mettre à jour les `<source>` dans `public/index.xhtml`. |
| `resources/media/logo/*.svg` | Logos **générés** (aucun logo fourni) | Le logo officiel : `gservices-logo.svg` (~320×72, version foncée), `gservices-logo-light.svg` (version claire pour fonds sombres), `favicon.svg` (carré). |
| `resources/media/lottie/` | Vide — animations Lottie non fournies | `.json` Lottie (LottieFiles) si l'on veut des pictos animés ; sinon les animations restent en CSS. |
| `resources/media/flags/` | Vide | Drapeaux pays (référentiel `Pays.drapeau`), intégrés au **Lot 2**. |
| `resources/media/icons/` | Vide | Non requis — **PrimeIcons** (`pi pi-*`) est utilisé par défaut. |

### 7.3 Dimensions recommandées pour un remplacement

| Type | Dimensions | Poids cible |
|---|---|---|
| Vidéo hero (paysage) | 1920×1080 (ou 1280×720) | < 3 Mo, `.mp4` H.264 + `.webm` VP9 |
| Image de fond de section | 1800–2000 px de large | < 300 Ko, JPEG progressif |
| Visuel de carrousel | ~1200×1500 (portrait) ou 1200×900 | < 250 Ko |
| Poster vidéo | même ratio que la vidéo | < 60 Ko |

---

## 8. Plan de construction (lots)

| Lot | Contenu | État |
|---|---|---|
| **0** | Socle Maven + Spring/JSF/Flyway, template, design system, Docker, accueil animé | ✅ **fait** |
| **1A** | 7 entités GestUser + Flyway V2/V3 + repositories + services + DTO/MapStruct + sécurité (login BCrypt, remember-me, verrouillage) + RBAC `@PreAuthorize` + seed | ✅ **fait** |
| **1B** | Shell admin (topbar + sidebar) · **menu dynamique** piloté par `BranchePermission` filtré RBAC · tableau de bord · écrans CRUD PrimeFaces lazy · thème clair/sombre complet | ✅ **fait** |
| **1C** | Sécurité regroupée sur `/admin/sec/securite.xhtml` (**2 onglets** : Comptes · Profils/permissions/branches) · **codes auto** : branche = 1000/2000/3000… · permission = `code branche + rang` (2001, 2002…) · tableau de bord & sidebar plus dynamiques | ✅ **fait** |
| **1D** | **Permissions par utilisateur** : dans « Permissions effectives » d'une personne, cocher/décocher ajoute ou retire une permission au-dessus de ses profils (`PersonnePermission`, migration V6) · onglets *Rôles* / *Droits* retirés (redondants avec les dialogues Personnes / Profils) | ✅ **fait** |
| **2** | `Continent → ZoneGeographique → Pays → Region → Ville → Position` + `Annee`/`Mois`/`Jour` (Flyway V7-V9) · `GeographieService` + `TempsService` · pages `/admin/geo/geographie.xhtml` (6 onglets, **cascade** Continent→Pays→Région, **carte Leaflet/OSM**) et `/admin/geo/temps.xhtml` (création d'année → 12 mois auto) | ✅ **fait** |
| **3** | `CategorieService → Service` (arborescent) `→ Catalogue → Produit → Article` + `ProprietesArticle`/`UniteMesure` + classe d'assoc. `VarieteArticle` + assoc. `Produit`↔`ProprietesArticle` « définit » (Flyway V10-V12) · `CatalogueService` · page `/admin/cat/catalogue.xhtml` (7 onglets, **cascade** à 4 niveaux, dialogue *Variétés*, prix remisé) | ✅ **fait** |
| **4** | `Stock` (1-1 Article) · `Lot` (composition, reçu en un `Mois`) · `InformationService` (1-1 Service, 0-1 Position) · `Horaire` (par jour) — Flyway V13-V15 · règles : alerte de seuil, disponible = lots actifs non périmés · page `/admin/stock/stock.xhtml` · **onglet Mois** ajouté à Temps | ✅ **fait** |
| **3bis** | Arborescence des services **de profondeur illimitée** (`p:treeTable`, anti-cycle) · écran **« Offre » unifié** (un produit + toutes ses déclinaisons/articles dans le même dialogue ; l'onglet Articles disparaît) · seed démo avancé V19 (chemise déclinée couleur/taille + grammage `g/m²` + stock/lots ; hôtel + chambres + tarifs + capacité/superficie) | ✅ **fait** |
| **5** | `Avis` (classe d'assoc. Personne × Service\|Produit, « publié en » une `Annee`, note 1-5) — Flyway V16-V18 · **modération** back-office (`/admin/avis/moderation.xhtml`) · **front public** : `/public/catalogue.xhtml` (catégories → prestataires) et `/public/service.xhtml?id=` (fiche complète : coordonnées + carte + horaires + offre + avis + dépôt d'avis si connecté) · `VitrineService` (lecture publique) | ✅ **fait** |
| **6** | Recette : 20 tests unitaires (règles §10) + 1 test d'intégration Testcontainers · seed de démonstration (V8/V11/V14/V17/V19) · doc · `docker compose up --build` validé | ✅ **fait** |

---

## 9. Modèle de données & règles de gestion

Le modèle UML (`modelisation/Diagramme de classes (corrige).png`) est implémenté
**intégralement**. Les 4 classes d'association sont des entités JPA à part entière
avec leur propre écran/dialogue :

| Classe d'assoc. | Entre | Portée | Administration |
|---|---|---|---|
| `Role` | Personne × Profil (datée) | affectation d'un profil | dialogue « Gérer les profils » (onglet Personnes) |
| `RoleProfil` | Profil × Permission (datée) | droits d'un profil | dialogue « Gérer les droits » (onglet Profils) |
| `VarieteArticle` | Article × ProprietesArticle | valeur d'une propriété pour un article | dialogue « Variétés » (écran Offre) |
| `Avis` | Personne × (Service \| Produit) | note + commentaire, « publié en » une `Annee` | écran de modération + front public |

Hiérarchies : `Continent → ZoneGeographique → Pays → Region → Ville → Position` ;
`CategorieService → Service` (arborescent, profondeur illimitée) `→ Catalogue →
Produit → Article`. `Service ↔ InformationService` (1-1), `Article ↔ Stock` (1-0..1),
`Stock → Lot` (composition), `InformationService → Horaire` (par `Jour`).
`Personne (1) — paramètre — (n) Service` = `Service.proprietaire` : le prestataire
qui gère l'offre du service et répond à ses avis.

### Acteurs

| Rôle | Peut |
|---|---|
| `SUPER_ADMIN` / `ADMIN` / `GESTIONNAIRE` | back-office selon les permissions de leur profil |
| `PRESTATAIRE` | gérer le catalogue / le stock, **répondre aux avis de ses propres services** (perm `6006`, contrôle de propriété) |
| `CLIENT` | déposer un avis (une fois par cible), suivre ses avis |
| visiteur anonyme | consulter le front public, la carte, les fiches |

### Règles appliquées (spec §10)

| Règle | Où |
|---|---|
| **Prix remisé** : `prixNet = promotion ? prix × (1 − taux/100) : prix`, arrondi HALF_UP au centime | `Article.getPrixNet()` |
| **Alerte de stock** : ligne en alerte dès que `disponible ≤ seuilAlerte` | `Stock.isEnAlerte()` — surlignage rouge dans l'IHM |
| **Exclusion des lots périmés** : `disponible` = Σ quantités des lots **actifs** dont `datePeremption ≥ aujourd'hui` | `Stock.getQuantiteDisponible()` |
| **Lot antidaté** : refus si `datePeremption < dateEntree` | `StockServiceImpl.controlerLot` |
| **Note ∈ [1;5]** | `CHECK` SQL + `@Min/@Max` + contrôle service |
| **Avis visible après modération** : `estModere && etat` | `Avis.isPublic()` + requêtes `...EstModereTrueAndEtatTrue` |
| **Un avis par personne et par cible** | `UNIQUE (id_personne, id_service)` / `(id_personne, id_produit)` |
| **Années bissextiles** : février 29 j si l'année a 366 jours | `TempsServiceImpl.creerAnnee` (`Month.length(bissextile)`) |
| **RBAC fin** : chaque action métier gardée par `@PreAuthorize` + élément d'IHM masqué si non autorisé | `security/Perms.java` + `#{utilisateurCourant.autorise(perms.X)}` |
| **Verrouillage** : blocage du compte après 5 échecs pendant 15 min | `SecuriteEventListener` |
| **Surcharge de droits par utilisateur** : `effectif = (permissions des profils) − refus + accords` | `GservicesUserDetails.buildAuthorities` (via `PersonnePermission`) |
| **Soft-delete** : `etat BOOLEAN` partout, jamais de `DELETE` physique | toutes les entités (`AbstractEntity`) |

---

## 10. Front public

| Page | Contenu |
|---|---|
| `/` (`public/index.xhtml`) | Accueil cinématique — hero vidéo + dégradé animé, aperçu de la **carte** des prestataires, carrousel, compteurs, parallaxe |
| `/public/carte.xhtml` | **Carte plein écran de Dakar** : ~45 prestataires géolocalisés, panneau de filtres (recherche, catégorie, note minimale, disponibilité) et liste synchronisée avec la carte |
| `/public/catalogue.xhtml` | Catégories (chips) → cartes prestataires (note animée, ville, disponibilité) — squelette au changement de catégorie |
| `/public/service.xhtml?id=` | Fiche complète : coordonnées, **carte Leaflet/OSM**, horaires, catalogue → produits → articles (prix remisés), avis publiés + histogramme animé, **dépôt d'avis** (client connecté, publication après modération), **réponse inline** si l'on est le propriétaire du service |
| `/public/inscription.xhtml` | Auto-inscription d'un visiteur (profil `CLIENT`) — proposée uniquement au moment de laisser un avis |

Le back-office `/admin/**` exige une authentification ; le front `/public/**` est
**entièrement ouvert** (consultation, carte, filtres). La connexion ou l'inscription
n'est demandée qu'au moment de **laisser un avis**. Comptes de démonstration :
voir [`COMPTES.md`](COMPTES.md).

### Couche cinématique

`resources/css/vitrine.css` + `resources/js/vitrine.js` (vanilla, sans dépendance,
`prefers-reduced-motion` respecté) : dégradé de hero animé, entrée du texte en
cascade, transition de page en fondu, étoiles à remplissage doré progressif,
histogramme d'avis qui se remplit au scroll, ripple sur les puces de catégorie,
squelette de chargement, count-up des notes. La carte utilise Leaflet 1.9 +
tuiles OpenStreetMap (aucune clé), et dégrade proprement hors-ligne.

---

## 10bis. API REST + application mobile Flutter

Le **même backend** expose, en plus des pages JSF, une **API REST** sous `/api`
pour l'application mobile (`mobile/`, Flutter) :

| Méthode | Endpoint | Auth |
|---|---|---|
| `POST` | `/api/auth/login` → `{token, utilisateur}` | — |
| `POST` | `/api/auth/register` | — |
| `GET` | `/api/auth/me` | JWT |
| `GET` | `/api/categories` · `/api/services` · `/api/services/geo` · `/api/services/{id}` | — |
| `POST` | `/api/services/{id}/avis` | JWT |
| `GET` | `/api/services/{id}/articles-commandables` | — |
| `POST` | `/api/services/{id}/commande` | JWT |
| `GET` | `/api/me/commandes` | JWT |

Chaîne de sécurité dédiée (`ApiSecurityConfig`, ordre 1) : **stateless**, JWT HS256
(`io.jsonwebtoken`), CORS ouvert. Le back-office `/admin/**` garde sa session.
Un compte non validé (`personne.valide = false`) est rejeté en 401.

L'app Flutter (`cd mobile && flutter run -d linux`) est **exécutable sur Linux**
(desktop), Android et iOS. Elle résout l'URL du backend automatiquement
(`localhost:8090` en desktop, `10.0.2.2` sur l'émulateur Android, ou
`--dart-define=API_BASE=…`). Détails : [`mobile/README.md`](mobile/README.md).

---

## 11. Variables d'environnement

Toute la configuration est externalisée (12-factor). Voir `.env.example`.

| Variable | Défaut | Rôle |
|---|---|---|
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `gservices` | base PostgreSQL |
| `APP_PORT` | `8080` | port exposé de l'app |
| `FACES_PROJECT_STAGE` | `Production` (compose) / `Development` (local) | verbosité JSF |
| `PF_THEME` | `saga` | thème PrimeFaces de base |
| `PGADMIN_EMAIL` / `PGADMIN_PASSWORD` | `admin@gservices.com` / `admin` | accès pgAdmin |

---

## 12. Licence des médias

Photos : Pexels (licence libre). Vidéo `videos.mp4` : **échantillon iStock avec
filigrane — à remplacer** (voir §7.2). Icônes : PrimeIcons (MIT).
# G-Services
