# G-SERVICES — application mobile (Flutter)

Client **Flutter** qui consomme **le même backend** que le site web
(Spring Boot), via l'API REST `/api` exposée en plus des pages JSF.

## Ce que fait l'app

| Onglet | Écran |
|---|---|
| Accueil | Services en avant, accès à la fiche |
| Carte | Carte OpenStreetMap (`flutter_map`) des prestataires de Dakar + filtre catégorie |
| Catalogue | Catégories → liste des prestataires |
| Compte | Connexion / inscription (client ou fournisseur, choix d'abord), mes commandes, déconnexion |

Fiche service : offres (avec illustrations SVG servies par le backend), horaires,
carte + itinéraire, avis. Boutons **Commander**, **Laisser un avis**, **♥ favori**
(AppBar) et **Signaler ce service** — la connexion n'est demandée qu'à ce moment-là
(comme sur le web). Un service bloqué par un administrateur (signalement traité)
disparaît automatiquement de l'app, comme du site web — même backend, même filtre.

## Backend / API

L'app parle à `…:8090/api`. L'URL est résolue ainsi :

1. `--dart-define=API_BASE=http://IP:8090/api` s'il est fourni ;
2. sinon `http://10.0.2.2:8090/api` sur l'émulateur Android ;
3. sinon `http://localhost:8090/api` (**Linux / desktop**, iOS simulateur).

Authentification : **JWT** (`POST /api/auth/login` → `Authorization: Bearer …`),
stocké via `flutter_secure_storage`. Le back-office web continue d'utiliser la session.

Endpoints publics : `GET /api/categories`, `/api/services`, `/api/services/geo`,
`/api/services/{id}`, `/api/signalement-motifs`. Authentifiés : `/api/auth/me`,
`POST /api/services/{id}/avis`, `POST /api/services/{id}/commande`,
`GET /api/me/commandes`, `GET/POST/DELETE /api/services/{id}/favori`,
`GET /api/me/favoris`, `POST /api/services/{id}/signalement`.

## Lancer

Le backend doit tourner (`docker compose up` à la racine du projet).

```bash
cd mobile
flutter pub get

# Linux desktop (l'app doit être exécutable sur Linux)
flutter run -d linux
# ou construire
flutter build linux            # -> build/linux/x64/release/bundle/gservices_mobile

# Android (émulateur)
flutter run -d emulator-5554

# Appareil physique / autre machine : préciser l'IP de l'hôte
flutter run --dart-define=API_BASE=http://192.168.1.20:8090/api
```

## Comptes de démo

Voir `../COMPTES.md`. Ex. client : `awa.diop` / `password`.
Un compte auto-inscrit reste **en attente de validation** par un administrateur
(la connexion renvoie alors une erreur 401 tant qu'il n'est pas validé).

## Structure

```
lib/
  main.dart            coquille + navigation (BottomNav) + requireAuth()
  api_client.dart      client Dio + JWT + résolution d'URL
  app_state.dart       session + référentiels (Provider / ChangeNotifier)
  models.dart          modèles alignés sur les DTO de l'API
  theme.dart           palette navy/or
  widgets.dart         Stars, ServiceCard, OffreImage (SVG), helpers
  screens_public.dart  Accueil, Carte, Catalogue, Fiche service
  screens_account.dart Auth (choix→form), Compte, Commander
```
