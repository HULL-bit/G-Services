import 'dart:io' show Platform;

import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'models.dart';

/// Erreur métier renvoyée par l'API ({"message": "..."}).
class ApiException implements Exception {
  final String message;
  final int? statusCode;
  ApiException(this.message, [this.statusCode]);
  @override
  String toString() => message;
}

/// Client HTTP unique vers le backend Spring Boot (`/api`) — **le même backend**
/// que le site web.
///
/// URL de base :
///  1. `--dart-define=API_BASE=http://192.168.1.10:8090/api` s'il est fourni ;
///  2. sinon `10.0.2.2` sur l'émulateur Android (= l'hôte) ;
///  3. sinon `localhost` (Linux / macOS / Windows desktop, iOS simulateur).
class ApiClient {
  static const _override = String.fromEnvironment('API_BASE');

  static String _resolveBase() {
    if (_override.isNotEmpty) return _override;
    if (!kIsWeb && Platform.isAndroid) return 'http://10.0.2.2:8090/api';
    return 'http://localhost:8090/api';
  }

  final String _base = _resolveBase();

  final _storage = const FlutterSecureStorage();
  late final Dio _dio;
  String? _token;

  ApiClient() {
    _dio = Dio(BaseOptions(
      baseUrl: _base,
      connectTimeout: const Duration(seconds: 12),
      receiveTimeout: const Duration(seconds: 20),
      headers: {'Content-Type': 'application/json'},
    ));
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) {
        if (_token != null) options.headers['Authorization'] = 'Bearer $_token';
        handler.next(options);
      },
    ));
  }

  String get baseUrl => _base;
  String get origin => _base.replaceAll('/api', '');
  bool get hasToken => _token != null;

  Future<void> loadToken() async {
    _token = await _storage.read(key: 'jwt');
  }

  Future<void> _setToken(String? t) async {
    _token = t;
    if (t == null) {
      await _storage.delete(key: 'jwt');
    } else {
      await _storage.write(key: 'jwt', value: t);
    }
  }

  Never _rethrow(Object e) {
    if (e is DioException) {
      final data = e.response?.data;
      final msg = (data is Map && data['message'] != null)
          ? data['message'].toString()
          : switch (e.response?.statusCode) {
              401 => 'Session expirée ou identifiants invalides.',
              403 => 'Accès refusé.',
              _ => 'Le serveur est injoignable. Vérifiez votre connexion.',
            };
      throw ApiException(msg, e.response?.statusCode);
    }
    throw ApiException('Erreur inattendue : $e');
  }

  // ---------------------------------------------------- Auth
  Future<AuthUser> login(String login, String motDePasse) async {
    try {
      final r = await _dio.post('/auth/login', data: {'login': login, 'motDePasse': motDePasse});
      await _setToken(r.data['token']);
      return AuthUser.fromJson(r.data['utilisateur']);
    } catch (e) {
      _rethrow(e);
    }
  }

  Future<String> register(Map<String, dynamic> body) async {
    try {
      final r = await _dio.post('/auth/register', data: body);
      return r.data['message']?.toString() ?? 'Compte créé.';
    } catch (e) {
      _rethrow(e);
    }
  }

  Future<AuthUser?> me() async {
    if (_token == null) return null;
    try {
      final r = await _dio.get('/auth/me');
      return AuthUser.fromJson(r.data);
    } on DioException {
      await _setToken(null);
      return null;
    }
  }

  Future<void> logout() => _setToken(null);

  // ---------------------------------------------------- Catalogue
  Future<List<Categorie>> categories() async {
    try {
      final r = await _dio.get('/categories');
      return (r.data as List).map((e) => Categorie.fromJson(e)).toList();
    } catch (e) {
      _rethrow(e);
    }
  }

  Future<List<ServiceListe>> services({int? categorieId, int? featured}) async {
    try {
      final r = await _dio.get('/services', queryParameters: {
        if (categorieId != null) 'categorieId': categorieId,
        if (featured != null) 'featured': featured,
      });
      return (r.data as List).map((e) => ServiceListe.fromJson(e)).toList();
    } catch (e) {
      _rethrow(e);
    }
  }

  Future<List<ServiceListe>> servicesGeo() async {
    try {
      final r = await _dio.get('/services/geo');
      return (r.data as List).map((e) => ServiceListe.fromJson(e)).toList();
    } catch (e) {
      _rethrow(e);
    }
  }

  Future<ServiceDetail> serviceDetail(int id) async {
    try {
      final r = await _dio.get('/services/$id');
      return ServiceDetail.fromJson(r.data);
    } catch (e) {
      _rethrow(e);
    }
  }

  Future<void> deposerAvis(int serviceId, int note, String commentaire) async {
    try {
      await _dio.post('/services/$serviceId/avis', data: {'note': note, 'commentaire': commentaire});
    } catch (e) {
      _rethrow(e);
    }
  }

  // ---------------------------------------------------- Commandes
  Future<List<Article>> articlesCommandables(int serviceId) async {
    try {
      final r = await _dio.get('/services/$serviceId/articles-commandables');
      return (r.data as List).map((e) => Article.fromJson(e)).toList();
    } catch (e) {
      _rethrow(e);
    }
  }

  Future<Commande> passerCommande(int serviceId, Map<String, dynamic> body) async {
    try {
      final r = await _dio.post('/services/$serviceId/commande', data: body);
      return Commande.fromJson(r.data);
    } catch (e) {
      _rethrow(e);
    }
  }

  Future<List<Commande>> mesCommandes() async {
    try {
      final r = await _dio.get('/me/commandes');
      return (r.data as List).map((e) => Commande.fromJson(e)).toList();
    } catch (e) {
      _rethrow(e);
    }
  }

  // ---------------------------------------------------- Favoris
  Future<List<Favori>> mesFavoris() async {
    try {
      final r = await _dio.get('/me/favoris');
      return (r.data as List).map((e) => Favori.fromJson(e)).toList();
    } catch (e) {
      _rethrow(e);
    }
  }

  Future<bool> estFavori(int serviceId) async {
    try {
      final r = await _dio.get('/services/$serviceId/favori');
      return r.data['favori'] == true;
    } catch (e) {
      _rethrow(e);
    }
  }

  Future<bool> basculerFavori(int serviceId) async {
    try {
      final r = await _dio.post('/services/$serviceId/favori');
      return r.data['favori'] == true;
    } catch (e) {
      _rethrow(e);
    }
  }

  // ---------------------------------------------------- Signalement (surveillance de la plateforme)
  Future<List<String>> signalementMotifs() async {
    try {
      final r = await _dio.get('/signalement-motifs');
      return (r.data as List).map((e) => e.toString()).toList();
    } catch (e) {
      _rethrow(e);
    }
  }

  Future<void> signalerService(int serviceId, String motif, String? description) async {
    try {
      await _dio.post('/services/$serviceId/signalement', data: {
        'motif': motif,
        if (description != null && description.isNotEmpty) 'description': description,
      });
    } catch (e) {
      _rethrow(e);
    }
  }
}
