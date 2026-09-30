import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'app_state.dart';
import 'screens_public.dart';
import 'screens_account.dart';
import 'theme.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (_) => AppState()..boot(),
      child: const GServicesApp(),
    ),
  );
}

class GServicesApp extends StatelessWidget {
  const GServicesApp({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'G-SERVICES',
      debugShowCheckedModeBanner: false,
      theme: gsTheme(),
      home: const _Root(),
    );
  }
}

class _Root extends StatelessWidget {
  const _Root();
  @override
  Widget build(BuildContext context) {
    final s = context.watch<AppState>();
    if (s.booting) {
      return const Scaffold(
        backgroundColor: GsColors.primary,
        body: Center(child: CircularProgressIndicator(color: Colors.white)),
      );
    }
    return const HomeShell();
  }
}

class HomeShell extends StatefulWidget {
  const HomeShell({super.key});
  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  int _i = 0;

  @override
  Widget build(BuildContext context) {
    const pages = [AccueilScreen(), CarteScreen(), CatalogueScreen(), CompteScreen()];
    return Scaffold(
      body: IndexedStack(index: _i, children: pages),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _i,
        onDestinationSelected: (v) => setState(() => _i = v),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.explore_outlined), selectedIcon: Icon(Icons.explore), label: 'Accueil'),
          NavigationDestination(icon: Icon(Icons.map_outlined), selectedIcon: Icon(Icons.map), label: 'Carte'),
          NavigationDestination(icon: Icon(Icons.grid_view_outlined), selectedIcon: Icon(Icons.grid_view), label: 'Catalogue'),
          NavigationDestination(icon: Icon(Icons.person_outline), selectedIcon: Icon(Icons.person), label: 'Compte'),
        ],
      ),
    );
  }
}

/// Aide : va sur l'écran d'auth si non connecté, sinon exécute [ifAuth].
Future<void> requireAuth(BuildContext context, VoidCallback ifAuth) async {
  final s = context.read<AppState>();
  if (s.connecte) {
    ifAuth();
    return;
  }
  final ok = await Navigator.of(context).push<bool>(
    MaterialPageRoute(builder: (_) => const AuthScreen()),
  );
  if (ok == true && context.mounted) ifAuth();
}
