import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'app_state.dart';
import 'models.dart';
import 'screens_public.dart';
import 'theme.dart';
import 'widgets.dart';

// ============================================================ Auth (choix → form)
class AuthScreen extends StatefulWidget {
  const AuthScreen({super.key});
  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

enum _Step { choix, connexion, typeInscription, formInscription, fait }

class _AuthScreenState extends State<AuthScreen> {
  _Step _step = _Step.choix;
  bool _fournisseur = false;
  bool _busy = false;
  String? _message;

  final _login = TextEditingController();
  final _mdp = TextEditingController();
  final _nom = TextEditingController();
  final _prenom = TextEditingController();
  final _email = TextEditingController();
  final _service = TextEditingController();
  int? _categorieId;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mon compte')),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: switch (_step) {
            _Step.choix => _choix(),
            _Step.connexion => _connexion(),
            _Step.typeInscription => _typeInscription(),
            _Step.formInscription => _formInscription(),
            _Step.fait => _fait(),
          },
        ),
      ),
    );
  }

  Widget _titre(String t, String s) => Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text(t, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w700)),
        const SizedBox(height: 4),
        Text(s, style: const TextStyle(color: GsColors.textSoft)),
        const SizedBox(height: 20),
      ]);

  Widget _carte(IconData i, String t, String s, VoidCallback onTap, {bool accent = false}) => Card(
        child: InkWell(
          borderRadius: BorderRadius.circular(16),
          onTap: onTap,
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Row(children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                      colors: accent
                          ? const [GsColors.accent, Color(0xFFE0BF5A)]
                          : const [GsColors.primary, GsColors.primary2]),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(i, color: accent ? Colors.black87 : Colors.white),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Text(t, style: const TextStyle(fontWeight: FontWeight.w700)),
                  Text(s, style: const TextStyle(fontSize: 12, color: GsColors.textSoft)),
                ]),
              ),
              const Icon(Icons.chevron_right, color: GsColors.textSoft),
            ]),
          ),
        ),
      );

  Widget _choix() => Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        _titre('Bienvenue sur G-SERVICES', 'Connectez-vous ou créez votre compte.'),
        _carte(Icons.login, "J'ai déjà un compte", 'Se connecter', () => setState(() => _step = _Step.connexion)),
        const SizedBox(height: 12),
        _carte(Icons.person_add_alt, 'Créer un compte', 'Client ou fournisseur de services',
            () => setState(() => _step = _Step.typeInscription),
            accent: true),
      ]);

  Widget _connexion() => Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        _back(_Step.choix),
        _titre('Connexion', 'Accédez à votre espace.'),
        if (_message != null) _alert(_message!),
        TextField(controller: _login, decoration: const InputDecoration(labelText: 'Identifiant')),
        const SizedBox(height: 12),
        TextField(controller: _mdp, obscureText: true, decoration: const InputDecoration(labelText: 'Mot de passe')),
        const SizedBox(height: 18),
        FilledButton(
          onPressed: _busy ? null : _faireLogin,
          style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(46)),
          child: _busy ? const _Spin() : const Text('Se connecter'),
        ),
      ]);

  Widget _typeInscription() => Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        _back(_Step.choix),
        _titre('Créer un compte', 'Quel type de compte ?'),
        _carte(Icons.person_outline, 'Client', 'Laisser des avis, passer des commandes', () {
          setState(() {
            _fournisseur = false;
            _step = _Step.formInscription;
          });
        }),
        const SizedBox(height: 12),
        _carte(Icons.work_outline, 'Fournisseur de services', 'Proposer et gérer un service', () {
          setState(() {
            _fournisseur = true;
            _step = _Step.formInscription;
          });
        }, accent: true),
      ]);

  Widget _formInscription() {
    final cats = context.read<AppState>().categories;
    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      _back(_Step.typeInscription),
      _titre(_fournisseur ? 'Compte fournisseur' : 'Compte client',
          _fournisseur ? 'Proposer et gérer un service' : 'Laisser des avis, passer des commandes'),
      if (_message != null) _alert(_message!),
      Row(children: [
        Expanded(child: TextField(controller: _prenom, decoration: const InputDecoration(labelText: 'Prénom'))),
        const SizedBox(width: 10),
        Expanded(child: TextField(controller: _nom, decoration: const InputDecoration(labelText: 'Nom'))),
      ]),
      const SizedBox(height: 12),
      TextField(controller: _email, decoration: const InputDecoration(labelText: 'E-mail')),
      const SizedBox(height: 12),
      TextField(controller: _login, decoration: const InputDecoration(labelText: 'Identifiant')),
      const SizedBox(height: 12),
      TextField(controller: _mdp, obscureText: true, decoration: const InputDecoration(labelText: 'Mot de passe (8 min.)')),
      if (_fournisseur) ...[
        const SizedBox(height: 12),
        DropdownButtonFormField<int>(
          initialValue: _categorieId,
          decoration: const InputDecoration(labelText: 'Catégorie de spécialité'),
          items: [for (final c in cats) DropdownMenuItem(value: c.id, child: Text(c.libelle))],
          onChanged: (v) => setState(() => _categorieId = v),
        ),
        const SizedBox(height: 12),
        TextField(controller: _service, decoration: const InputDecoration(labelText: 'Service à ouvrir (optionnel)')),
      ],
      const SizedBox(height: 18),
      FilledButton(
        onPressed: _busy ? null : _faireInscription,
        style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(46)),
        child: _busy ? const _Spin() : const Text('Créer mon compte'),
      ),
    ]);
  }

  Widget _fait() => Column(crossAxisAlignment: CrossAxisAlignment.center, children: [
        const SizedBox(height: 30),
        const Icon(Icons.check_circle_outline, size: 56, color: GsColors.success),
        const SizedBox(height: 12),
        const Text('Demande enregistrée', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w700)),
        const SizedBox(height: 8),
        Text(_message ?? '', textAlign: TextAlign.center, style: const TextStyle(color: GsColors.textSoft)),
        const SizedBox(height: 20),
        FilledButton(onPressed: () => setState(() => _step = _Step.connexion), child: const Text('Se connecter')),
      ]);

  Widget _back(_Step to) => Align(
        alignment: Alignment.centerLeft,
        child: TextButton.icon(
          onPressed: () => setState(() {
            _message = null;
            _step = to;
          }),
          icon: const Icon(Icons.arrow_back, size: 18),
          label: const Text('Retour'),
        ),
      );

  Widget _alert(String m) => Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(color: GsColors.danger.withValues(alpha: .1), borderRadius: BorderRadius.circular(10)),
        child: Text(m, style: const TextStyle(color: GsColors.danger, fontSize: 13)),
      );

  Future<void> _faireLogin() async {
    setState(() {
      _busy = true;
      _message = null;
    });
    try {
      await context.read<AppState>().login(_login.text.trim(), _mdp.text);
      if (mounted) Navigator.pop(context, true);
    } catch (e) {
      setState(() {
        _message = e.toString();
        _busy = false;
      });
    }
  }

  Future<void> _faireInscription() async {
    setState(() {
      _busy = true;
      _message = null;
    });
    try {
      final msg = await context.read<AppState>().register({
        'typeCompte': _fournisseur ? 'FOURNISSEUR' : 'CLIENT',
        'nom': _nom.text.trim(),
        'prenom': _prenom.text.trim(),
        'email': _email.text.trim(),
        'login': _login.text.trim(),
        'motDePasse': _mdp.text,
        if (_fournisseur) 'categorieSpecialiteId': _categorieId,
        if (_fournisseur && _service.text.trim().isNotEmpty) 'serviceLibelle': _service.text.trim(),
      });
      setState(() {
        _message = msg;
        _step = _Step.fait;
        _busy = false;
      });
    } catch (e) {
      setState(() {
        _message = e.toString();
        _busy = false;
      });
    }
  }
}

class _Spin extends StatelessWidget {
  const _Spin();
  @override
  Widget build(BuildContext context) => const SizedBox(
      width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white));
}

// ============================================================ Compte
class CompteScreen extends StatelessWidget {
  const CompteScreen({super.key});
  @override
  Widget build(BuildContext context) {
    final s = context.watch<AppState>();
    return Scaffold(
      appBar: AppBar(title: const Text('Mon compte')),
      body: s.connecte ? _connecte(context, s) : _deconnecte(context),
    );
  }

  Widget _deconnecte(BuildContext context) => Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(mainAxisSize: MainAxisSize.min, children: [
            const Icon(Icons.person_outline, size: 48, color: GsColors.textSoft),
            const SizedBox(height: 12),
            const Text('Connectez-vous pour suivre vos commandes et laisser des avis.',
                textAlign: TextAlign.center, style: TextStyle(color: GsColors.textSoft)),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const AuthScreen())),
              child: const Text('Se connecter / S’inscrire'),
            ),
          ]),
        ),
      );

  Widget _connecte(BuildContext context, AppState s) => ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: ListTile(
              leading: const CircleAvatar(backgroundColor: GsColors.primary, child: Icon(Icons.person, color: Colors.white)),
              title: Text(s.user!.nomComplet, style: const TextStyle(fontWeight: FontWeight.w700)),
              subtitle: Text('${s.user!.login} · ${s.user!.profils.join(', ')}'),
            ),
          ),
          const SizedBox(height: 16),
          const Text('Mes favoris', style: TextStyle(fontWeight: FontWeight.w700, fontSize: 16)),
          const SizedBox(height: 8),
          FutureBuilder<List<Favori>>(
            future: s.api.mesFavoris(),
            builder: (context, snap) {
              if (!snap.hasData) {
                return const Padding(padding: EdgeInsets.all(20), child: Center(child: CircularProgressIndicator()));
              }
              final list = snap.data!;
              if (list.isEmpty) {
                return const Text('Aucun favori pour le moment.', style: TextStyle(color: GsColors.textSoft));
              }
              return SizedBox(
                height: 190,
                child: ListView.separated(
                  scrollDirection: Axis.horizontal,
                  itemCount: list.length,
                  separatorBuilder: (_, __) => const SizedBox(width: 12),
                  itemBuilder: (_, i) => SizedBox(
                    width: 170,
                    child: ServiceCard(list[i].commeService, onTap: () => openService(context, list[i].serviceId)),
                  ),
                ),
              );
            },
          ),
          const SizedBox(height: 20),
          const Text('Mes commandes', style: TextStyle(fontWeight: FontWeight.w700, fontSize: 16)),
          const SizedBox(height: 8),
          FutureBuilder<List<Commande>>(
            future: s.api.mesCommandes(),
            builder: (context, snap) {
              if (!snap.hasData) return const Padding(padding: EdgeInsets.all(20), child: Center(child: CircularProgressIndicator()));
              final list = snap.data!;
              if (list.isEmpty) return const Text('Aucune commande.', style: TextStyle(color: GsColors.textSoft));
              return Column(children: [
                for (final c in list)
                  Card(
                    margin: const EdgeInsets.only(bottom: 8),
                    child: ListTile(
                      title: Text(c.reference, style: const TextStyle(fontWeight: FontWeight.w600)),
                      subtitle: Text('${c.serviceLibelle ?? ''} · ${money(c.montantTotal)}'),
                      trailing: Chip(
                        label: Text(c.statut, style: const TextStyle(fontSize: 11)),
                        visualDensity: VisualDensity.compact,
                      ),
                    ),
                  ),
              ]);
            },
          ),
          const SizedBox(height: 20),
          OutlinedButton.icon(
            onPressed: () => context.read<AppState>().logout(),
            icon: const Icon(Icons.logout),
            label: const Text('Se déconnecter'),
          ),
        ],
      );
}

// ============================================================ Commander
class CommanderScreen extends StatefulWidget {
  final int serviceId;
  final String titre;
  const CommanderScreen({super.key, required this.serviceId, required this.titre});
  @override
  State<CommanderScreen> createState() => _CommanderScreenState();
}

class _CommanderScreenState extends State<CommanderScreen> {
  late Future<List<Article>> _future;
  final Map<int, int> _qte = {};
  final _adresse = TextEditingController();
  final _tel = TextEditingController();
  final _note = TextEditingController();
  bool _busy = false;

  @override
  void initState() {
    super.initState();
    _future = context.read<AppState>().api.articlesCommandables(widget.serviceId);
  }

  double _total(List<Article> arts) {
    var t = 0.0;
    for (final a in arts) {
      t += (a.prixNet) * (_qte[a.id] ?? 0);
    }
    return t;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Commander · ${widget.titre}')),
      body: FutureBuilder<List<Article>>(
        future: _future,
        builder: (context, snap) {
          if (!snap.hasData) return const Center(child: CircularProgressIndicator());
          final arts = snap.data!;
          if (arts.isEmpty) return const Center(child: Text('Aucun article disponible.'));
          return Column(children: [
            Expanded(
              child: ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  for (final a in arts)
                    Card(
                      margin: const EdgeInsets.only(bottom: 8),
                      child: Padding(
                        padding: const EdgeInsets.all(10),
                        child: Row(children: [
                          Expanded(
                            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                              Text(a.description ?? a.reference, style: const TextStyle(fontSize: 13)),
                              Text(money(a.prixNet, a.devise),
                                  style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13)),
                            ]),
                          ),
                          _Stepper(
                            value: _qte[a.id] ?? 0,
                            onChanged: (v) => setState(() => _qte[a.id] = v),
                          ),
                        ]),
                      ),
                    ),
                  const SizedBox(height: 8),
                  TextField(controller: _adresse, decoration: const InputDecoration(labelText: 'Adresse de livraison')),
                  const SizedBox(height: 10),
                  TextField(controller: _tel, decoration: const InputDecoration(labelText: 'Téléphone de contact')),
                  const SizedBox(height: 10),
                  TextField(controller: _note, decoration: const InputDecoration(labelText: 'Précisions (facultatif)')),
                ],
              ),
            ),
            SafeArea(
              child: Container(
                padding: const EdgeInsets.all(16),
                decoration: const BoxDecoration(color: GsColors.surface, boxShadow: [
                  BoxShadow(color: Colors.black12, blurRadius: 8, offset: Offset(0, -2)),
                ]),
                child: Row(children: [
                  Expanded(
                    child: Text('Total : ${money(_total(arts))}',
                        style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 16)),
                  ),
                  FilledButton(
                    onPressed: _busy || _total(arts) <= 0 ? null : () => _valider(arts),
                    child: _busy ? const _Spin() : const Text('Valider ma commande'),
                  ),
                ]),
              ),
            ),
          ]);
        },
      ),
    );
  }

  Future<void> _valider(List<Article> arts) async {
    setState(() => _busy = true);
    try {
      final c = await context.read<AppState>().api.passerCommande(widget.serviceId, {
        'lignes': [
          for (final a in arts)
            if ((_qte[a.id] ?? 0) > 0) {'articleId': a.id, 'quantite': _qte[a.id]},
        ],
        'adresseLivraison': _adresse.text.trim(),
        'telephoneContact': _tel.text.trim(),
        'commentaire': _note.text.trim(),
      });
      if (mounted) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Commande ${c.reference} enregistrée.'), backgroundColor: GsColors.success),
        );
      }
    } catch (e) {
      if (mounted) {
        showError(context, e);
        setState(() => _busy = false);
      }
    }
  }
}

class _Stepper extends StatelessWidget {
  final int value;
  final ValueChanged<int> onChanged;
  const _Stepper({required this.value, required this.onChanged});
  @override
  Widget build(BuildContext context) {
    return Row(mainAxisSize: MainAxisSize.min, children: [
      IconButton(
        visualDensity: VisualDensity.compact,
        onPressed: value > 0 ? () => onChanged(value - 1) : null,
        icon: const Icon(Icons.remove_circle_outline),
      ),
      Text('$value', style: const TextStyle(fontWeight: FontWeight.w700)),
      IconButton(
        visualDensity: VisualDensity.compact,
        onPressed: value < 99 ? () => onChanged(value + 1) : null,
        icon: const Icon(Icons.add_circle_outline),
      ),
    ]);
  }
}
