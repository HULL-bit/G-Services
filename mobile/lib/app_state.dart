import 'package:flutter/foundation.dart';

import 'api_client.dart';
import 'models.dart';

/// État global : session + référentiels chargés une fois.
class AppState extends ChangeNotifier {
  final ApiClient api = ApiClient();

  AuthUser? user;
  bool booting = true;

  List<Categorie> categories = [];
  List<ServiceListe> _geo = [];

  bool get connecte => user != null;

  Future<void> boot() async {
    await api.loadToken();
    try {
      user = await api.me();
    } catch (_) {
      user = null;
    }
    try {
      categories = await api.categories();
    } catch (_) {}
    booting = false;
    notifyListeners();
  }

  Future<List<ServiceListe>> servicesGeo({bool refresh = false}) async {
    if (_geo.isEmpty || refresh) {
      _geo = await api.servicesGeo();
    }
    return _geo;
  }

  Future<void> login(String login, String mdp) async {
    user = await api.login(login, mdp);
    notifyListeners();
  }

  Future<String> register(Map<String, dynamic> body) => api.register(body);

  Future<void> logout() async {
    await api.logout();
    user = null;
    notifyListeners();
  }
}
