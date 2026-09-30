import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';

import 'app_state.dart';
import 'main.dart';
import 'models.dart';
import 'screens_account.dart';
import 'theme.dart';
import 'widgets.dart';

// ============================================================ Accueil
class AccueilScreen extends StatefulWidget {
  const AccueilScreen({super.key});
  @override
  State<AccueilScreen> createState() => _AccueilScreenState();
}

class _AccueilScreenState extends State<AccueilScreen> {
  late Future<List<ServiceListe>> _future;
  @override
  void initState() {
    super.initState();
    _future = context.read<AppState>().api.services(featured: 12);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('G-SERVICES')),
      body: RefreshIndicator(
        onRefresh: () async => setState(() => _future = context.read<AppState>().api.services(featured: 12)),
        child: FutureBuilder<List<ServiceListe>>(
          future: _future,
          builder: (context, snap) {
            if (snap.connectionState == ConnectionState.waiting) {
              return const Center(child: CircularProgressIndicator());
            }
            if (snap.hasError) return _ErrorView(snap.error!, () => setState(() {}));
            final list = snap.data ?? [];
            return ListView(
              padding: const EdgeInsets.all(16),
              children: [
                Container(
                  padding: const EdgeInsets.all(18),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(colors: [GsColors.primary, GsColors.primary2]),
                    borderRadius: BorderRadius.circular(18),
                  ),
                  child: const Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                    Text('Services & produits géolocalisés',
                        style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w700)),
                    SizedBox(height: 6),
                    Text('Trouvez un prestataire près de vous à Dakar, consultez ses offres, commandez et laissez un avis.',
                        style: TextStyle(color: Colors.white70, fontSize: 13)),
                  ]),
                ),
                const SizedBox(height: 18),
                const Text('À découvrir', style: TextStyle(fontWeight: FontWeight.w700, fontSize: 16)),
                const SizedBox(height: 10),
                GridView.count(
                  crossAxisCount: 2,
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  mainAxisSpacing: 12,
                  crossAxisSpacing: 12,
                  childAspectRatio: .82,
                  children: [
                    for (final s in list)
                      ServiceCard(s, onTap: () => openService(context, s.id)),
                  ],
                ),
              ],
            );
          },
        ),
      ),
    );
  }
}

void openService(BuildContext context, int id) {
  Navigator.of(context).push(MaterialPageRoute(builder: (_) => ServiceDetailScreen(id)));
}

// ============================================================ Carte
class CarteScreen extends StatefulWidget {
  const CarteScreen({super.key});
  @override
  State<CarteScreen> createState() => _CarteScreenState();
}

class _CarteScreenState extends State<CarteScreen> {
  late Future<List<ServiceListe>> _future;
  int? _categorieId;

  @override
  void initState() {
    super.initState();
    _future = context.read<AppState>().servicesGeo();
  }

  @override
  Widget build(BuildContext context) {
    final cats = context.read<AppState>().categories;
    return Scaffold(
      appBar: AppBar(title: const Text('Carte des prestataires')),
      body: Column(children: [
        SizedBox(
          height: 48,
          child: ListView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            children: [
              _chip('Toutes', _categorieId == null, () => setState(() => _categorieId = null)),
              for (final c in cats)
                _chip(c.libelle, _categorieId == c.id, () => setState(() => _categorieId = c.id)),
            ],
          ),
        ),
        Expanded(
          child: FutureBuilder<List<ServiceListe>>(
            future: _future,
            builder: (context, snap) {
              if (!snap.hasData) return const Center(child: CircularProgressIndicator());
              final pts = snap.data!
                  .where((s) => s.latitude != null && s.longitude != null)
                  .where((s) => _categorieId == null || s.categorieLibelle == _catName(cats))
                  .toList();
              return FlutterMap(
                options: const MapOptions(
                  initialCenter: LatLng(14.716, -17.467),
                  initialZoom: 12,
                ),
                children: [
                  TileLayer(
                    urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                    userAgentPackageName: 'sn.gservices.mobile',
                  ),
                  MarkerLayer(
                    markers: [
                      for (final s in pts)
                        Marker(
                          point: LatLng(s.latitude!, s.longitude!),
                          width: 40,
                          height: 40,
                          child: GestureDetector(
                            onTap: () => openService(context, s.id),
                            child: const _Pin(),
                          ),
                        ),
                    ],
                  ),
                  const RichAttributionWidget(attributions: [
                    TextSourceAttribution('OpenStreetMap contributors'),
                  ]),
                ],
              );
            },
          ),
        ),
      ]),
    );
  }

  String? _catName(List<Categorie> cats) =>
      cats.where((c) => c.id == _categorieId).map((c) => c.libelle).firstOrNull;

  Widget _chip(String label, bool active, VoidCallback onTap) => Padding(
        padding: const EdgeInsets.only(right: 8),
        child: ChoiceChip(
          label: Text(label),
          selected: active,
          onSelected: (_) => onTap(),
          selectedColor: GsColors.primary,
          labelStyle: TextStyle(color: active ? Colors.white : GsColors.primary, fontSize: 12),
        ),
      );
}

class _Pin extends StatelessWidget {
  const _Pin();
  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        gradient: const LinearGradient(colors: [GsColors.primary, GsColors.primary2]),
        border: Border.all(color: Colors.white, width: 2),
        borderRadius: const BorderRadius.only(
          topLeft: Radius.circular(18),
          topRight: Radius.circular(18),
          bottomRight: Radius.circular(18),
        ),
        boxShadow: const [BoxShadow(color: Colors.black26, blurRadius: 6, offset: Offset(0, 3))],
      ),
      child: const Icon(Icons.storefront, color: Colors.white, size: 18),
    );
  }
}

// ============================================================ Catalogue
class CatalogueScreen extends StatelessWidget {
  const CatalogueScreen({super.key});
  @override
  Widget build(BuildContext context) {
    final cats = context.watch<AppState>().categories;
    return Scaffold(
      appBar: AppBar(title: const Text('Catalogue')),
      body: GridView.count(
        crossAxisCount: 2,
        padding: const EdgeInsets.all(16),
        mainAxisSpacing: 12,
        crossAxisSpacing: 12,
        childAspectRatio: 1.15,
        children: [
          for (final c in cats)
            Card(
              child: InkWell(
                borderRadius: BorderRadius.circular(16),
                onTap: () => Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => _ServicesParCategorie(c)),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(14),
                  child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                    Icon(iconForPi(c.icone), color: GsColors.primary, size: 28),
                    const Spacer(),
                    Text(c.libelle, style: const TextStyle(fontWeight: FontWeight.w700)),
                    Text('${c.nbServices} prestataire${c.nbServices > 1 ? 's' : ''}',
                        style: const TextStyle(fontSize: 12, color: GsColors.textSoft)),
                  ]),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

class _ServicesParCategorie extends StatelessWidget {
  final Categorie c;
  const _ServicesParCategorie(this.c);
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(c.libelle)),
      body: FutureBuilder<List<ServiceListe>>(
        future: context.read<AppState>().api.services(categorieId: c.id),
        builder: (context, snap) {
          if (!snap.hasData) return const Center(child: CircularProgressIndicator());
          final list = snap.data!;
          if (list.isEmpty) return const Center(child: Text('Aucun prestataire.'));
          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: list.length,
            separatorBuilder: (_, __) => const SizedBox(height: 12),
            itemBuilder: (_, i) => ServiceCard(list[i], onTap: () => openService(context, list[i].id)),
          );
        },
      ),
    );
  }
}

// ============================================================ Fiche service
class ServiceDetailScreen extends StatefulWidget {
  final int id;
  const ServiceDetailScreen(this.id, {super.key});
  @override
  State<ServiceDetailScreen> createState() => _ServiceDetailScreenState();
}

class _ServiceDetailScreenState extends State<ServiceDetailScreen> {
  late Future<ServiceDetail> _future;
  bool? _favori;
  void _reload() => setState(() => _future = context.read<AppState>().api.serviceDetail(widget.id));

  @override
  void initState() {
    super.initState();
    _future = context.read<AppState>().api.serviceDetail(widget.id);
    _chargerFavori();
  }

  Future<void> _chargerFavori() async {
    final s = context.read<AppState>();
    if (!s.connecte) return;
    try {
      final v = await s.api.estFavori(widget.id);
      if (mounted) setState(() => _favori = v);
    } catch (_) {}
  }

  Future<void> _toggleFavori() async {
    requireAuth(context, () async {
      try {
        final v = await context.read<AppState>().api.basculerFavori(widget.id);
        if (mounted) setState(() => _favori = v);
      } catch (e) {
        if (mounted) showError(context, e);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: FutureBuilder<ServiceDetail>(
        future: _future,
        builder: (context, snap) {
          if (snap.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snap.hasError) return Scaffold(appBar: AppBar(), body: _ErrorView(snap.error!, _reload));
          final d = snap.data!;
          return CustomScrollView(slivers: [
            SliverAppBar(
              expandedHeight: 150,
              pinned: true,
              actions: [
                IconButton(
                  tooltip: 'Ajouter/retirer des favoris',
                  onPressed: _toggleFavori,
                  icon: Icon(_favori == true ? Icons.favorite : Icons.favorite_border,
                      color: _favori == true ? GsColors.accent : Colors.white),
                ),
              ],
              flexibleSpace: FlexibleSpaceBar(
                title: Text(d.service.libelle, style: const TextStyle(fontSize: 15)),
                background: Container(
                  decoration: const BoxDecoration(
                    gradient: LinearGradient(colors: [GsColors.primary, GsColors.primary2]),
                  ),
                ),
              ),
            ),
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Row(children: [
                    Stars(d.moyenne),
                    const SizedBox(width: 6),
                    Text('${d.moyenne.toStringAsFixed(1)} · ${d.totalAvis} avis',
                        style: const TextStyle(color: GsColors.textSoft, fontSize: 13)),
                  ]),
                  if (d.service.description != null) ...[
                    const SizedBox(height: 8),
                    Text(d.service.description!, style: const TextStyle(color: GsColors.textSoft)),
                  ],
                  const SizedBox(height: 18),

                  // ---- Offres
                  for (final cat in d.catalogues) ...[
                    Text(cat.libelle, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 16)),
                    const SizedBox(height: 8),
                    for (final o in cat.produits) _OffreBloc(o),
                    const SizedBox(height: 10),
                  ],

                  // ---- Commander
                  if (d.service.commandeADistance) ...[
                    FilledButton.icon(
                      onPressed: () => requireAuth(context, () {
                        Navigator.of(context).push(MaterialPageRoute(
                          builder: (_) => CommanderScreen(serviceId: d.service.id, titre: d.service.libelle),
                        ));
                      }),
                      icon: const Icon(Icons.shopping_bag_outlined),
                      label: const Text('Commander en ligne'),
                      style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(46)),
                    ),
                    const SizedBox(height: 16),
                  ],

                  // ---- Prestataire
                  if (d.prestataire != null) _PrestataireBloc(d.prestataire!),

                  // ---- Horaires
                  if (d.horaires.isNotEmpty) ...[
                    const SizedBox(height: 16),
                    const Text('Horaires', style: TextStyle(fontWeight: FontWeight.w700)),
                    const SizedBox(height: 6),
                    for (final h in d.horaires)
                      Padding(
                        padding: const EdgeInsets.symmetric(vertical: 2),
                        child: Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
                          Text(h.jourLibelle, style: const TextStyle(fontSize: 13)),
                          Text(h.texte, style: const TextStyle(fontSize: 13, color: GsColors.textSoft)),
                        ]),
                      ),
                  ],

                  // ---- Signaler
                  const SizedBox(height: 14),
                  Center(
                    child: TextButton.icon(
                      onPressed: () => requireAuth(context, () async {
                        await showDialog(context: context, builder: (_) => _SignalerDialog(widget.id));
                      }),
                      icon: const Icon(Icons.flag_outlined, size: 16, color: GsColors.textSoft),
                      label: const Text('Signaler ce service',
                          style: TextStyle(color: GsColors.textSoft, fontSize: 13)),
                    ),
                  ),

                  // ---- Avis
                  const SizedBox(height: 20),
                  Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
                    Text('Avis (${d.totalAvis})', style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 16)),
                    TextButton.icon(
                      onPressed: () => requireAuth(context, () async {
                        final ok = await showDialog<bool>(
                          context: context,
                          builder: (_) => _AvisDialog(widget.id),
                        );
                        if (ok == true) _reload();
                      }),
                      icon: const Icon(Icons.rate_review_outlined, size: 18),
                      label: const Text('Laisser un avis'),
                    ),
                  ]),
                  for (final a in d.avis) _AvisBloc(a),
                  if (d.avis.isEmpty)
                    const Padding(
                      padding: EdgeInsets.symmetric(vertical: 8),
                      child: Text('Aucun avis pour le moment.', style: TextStyle(color: GsColors.textSoft)),
                    ),
                  const SizedBox(height: 30),
                ]),
              ),
            ),
          ]);
        },
      ),
    );
  }
}

class _OffreBloc extends StatelessWidget {
  final Offre o;
  const _OffreBloc(this.o);
  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
          OffreImage(o.image, w: 96, h: 96),
          const SizedBox(width: 12),
          Expanded(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(o.libelle, style: const TextStyle(fontWeight: FontWeight.w700)),
              if (o.description != null)
                Text(o.description!, style: const TextStyle(fontSize: 12, color: GsColors.textSoft)),
              const SizedBox(height: 6),
              for (final a in o.articles)
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 2),
                  child: Row(children: [
                    Expanded(child: Text(a.description ?? a.reference, style: const TextStyle(fontSize: 13))),
                    if (a.promotion) ...[
                      Text(money(a.prix, a.devise),
                          style: const TextStyle(
                              fontSize: 11, color: GsColors.textSoft, decoration: TextDecoration.lineThrough)),
                      const SizedBox(width: 4),
                    ],
                    Text(money(a.prixNet, a.devise),
                        style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w700)),
                  ]),
                ),
            ]),
          ),
        ]),
      ),
    );
  }
}

class _PrestataireBloc extends StatelessWidget {
  final Prestataire p;
  const _PrestataireBloc(this.p);
  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('Coordonnées', style: TextStyle(fontWeight: FontWeight.w700)),
          const SizedBox(height: 8),
          if (p.adresse != null) _line(Icons.place_outlined, p.adresse!),
          if (p.telephone1 != null) _line(Icons.phone_outlined, p.telephone1!),
          if (p.email1 != null) _line(Icons.mail_outline, p.email1!),
          if (p.latitude != null) ...[
            const SizedBox(height: 10),
            SizedBox(
              height: 150,
              child: ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: FlutterMap(
                  options: MapOptions(
                    initialCenter: LatLng(p.latitude!, p.longitude!),
                    initialZoom: 15,
                    interactionOptions: const InteractionOptions(flags: InteractiveFlag.none),
                  ),
                  children: [
                    TileLayer(
                      urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                      userAgentPackageName: 'sn.gservices.mobile',
                    ),
                    MarkerLayer(markers: [
                      Marker(point: LatLng(p.latitude!, p.longitude!), width: 40, height: 40, child: const _Pin()),
                    ]),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 8),
            OutlinedButton.icon(
              onPressed: () => launchUrl(
                Uri.parse('https://www.openstreetmap.org/directions?to=${p.latitude},${p.longitude}'),
                mode: LaunchMode.externalApplication,
              ),
              icon: const Icon(Icons.directions_outlined),
              label: const Text('Itinéraire'),
            ),
          ],
        ]),
      ),
    );
  }

  Widget _line(IconData i, String t) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 3),
        child: Row(children: [
          Icon(i, size: 16, color: GsColors.textSoft),
          const SizedBox(width: 8),
          Expanded(child: Text(t, style: const TextStyle(fontSize: 13))),
        ]),
      );
}

class _AvisBloc extends StatelessWidget {
  final Avis a;
  const _AvisBloc(this.a);
  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 6),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: GsColors.surface, borderRadius: BorderRadius.circular(12)),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(children: [
          Text(a.personneNom, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
          const Spacer(),
          Stars(a.note.toDouble(), size: 13),
        ]),
        if (a.commentaire != null) ...[
          const SizedBox(height: 4),
          Text(a.commentaire!, style: const TextStyle(fontSize: 13)),
        ],
        if (a.reponsePrestataire != null) ...[
          const SizedBox(height: 8),
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(color: GsColors.bg, borderRadius: BorderRadius.circular(8)),
            child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
              const Icon(Icons.reply, size: 14, color: GsColors.textSoft),
              const SizedBox(width: 6),
              Expanded(child: Text(a.reponsePrestataire!, style: const TextStyle(fontSize: 12))),
            ]),
          ),
        ],
      ]),
    );
  }
}

class _AvisDialog extends StatefulWidget {
  final int serviceId;
  const _AvisDialog(this.serviceId);
  @override
  State<_AvisDialog> createState() => _AvisDialogState();
}

class _AvisDialogState extends State<_AvisDialog> {
  int _note = 5;
  final _c = TextEditingController();
  bool _busy = false;

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Votre avis'),
      content: Column(mainAxisSize: MainAxisSize.min, children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            for (var i = 1; i <= 5; i++)
              IconButton(
                onPressed: () => setState(() => _note = i),
                icon: Icon(i <= _note ? Icons.star_rounded : Icons.star_border_rounded, color: GsColors.accent),
              ),
          ],
        ),
        TextField(
          controller: _c,
          maxLines: 3,
          decoration: const InputDecoration(hintText: 'Partagez votre expérience…'),
        ),
      ]),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Annuler')),
        FilledButton(
          onPressed: _busy
              ? null
              : () async {
                  setState(() => _busy = true);
                  try {
                    await context.read<AppState>().api.deposerAvis(widget.serviceId, _note, _c.text.trim());
                    if (context.mounted) Navigator.pop(context, true);
                  } catch (e) {
                    if (context.mounted) {
                      showError(context, e);
                      setState(() => _busy = false);
                    }
                  }
                },
          child: const Text('Publier'),
        ),
      ],
    );
  }
}

class _SignalerDialog extends StatefulWidget {
  final int serviceId;
  const _SignalerDialog(this.serviceId);
  @override
  State<_SignalerDialog> createState() => _SignalerDialogState();
}

class _SignalerDialogState extends State<_SignalerDialog> {
  late Future<List<String>> _motifsFuture;
  String? _motif;
  final _c = TextEditingController();
  bool _busy = false;
  bool _envoye = false;

  @override
  void initState() {
    super.initState();
    _motifsFuture = context.read<AppState>().api.signalementMotifs();
  }

  @override
  Widget build(BuildContext context) {
    if (_envoye) {
      return AlertDialog(
        title: const Text('Signalement envoyé'),
        content: const Text("Merci, votre signalement a été transmis à l'équipe de modération."),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('Fermer')),
        ],
      );
    }
    return AlertDialog(
      title: const Text('Signaler ce service'),
      content: FutureBuilder<List<String>>(
        future: _motifsFuture,
        builder: (context, snap) {
          if (!snap.hasData) {
            return const SizedBox(height: 60, child: Center(child: CircularProgressIndicator()));
          }
          return Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.start, children: [
            const Text('Vous avez repéré un problème sur cette fiche ?',
                style: TextStyle(fontSize: 13, color: GsColors.textSoft)),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              initialValue: _motif,
              decoration: const InputDecoration(labelText: 'Motif'),
              items: [for (final m in snap.data!) DropdownMenuItem(value: m, child: Text(libelleMotifSignalement(m)))],
              onChanged: (v) => setState(() => _motif = v),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _c,
              maxLines: 3,
              decoration: const InputDecoration(hintText: 'Précisions (facultatif)'),
            ),
          ]);
        },
      ),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context), child: const Text('Annuler')),
        FilledButton(
          onPressed: _motif == null || _busy
              ? null
              : () async {
                  setState(() => _busy = true);
                  try {
                    await context.read<AppState>().api.signalerService(widget.serviceId, _motif!, _c.text.trim());
                    if (mounted) setState(() { _envoye = true; _busy = false; });
                  } catch (e) {
                    if (mounted) {
                      showError(context, e);
                      setState(() => _busy = false);
                    }
                  }
                },
          child: _busy
              ? const SizedBox(
                  width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
              : const Text('Envoyer'),
        ),
      ],
    );
  }
}

// ============================================================ util partagé
class _ErrorView extends StatelessWidget {
  final Object error;
  final VoidCallback onRetry;
  const _ErrorView(this.error, this.onRetry);
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          const Icon(Icons.cloud_off, size: 40, color: GsColors.textSoft),
          const SizedBox(height: 10),
          Text('$error', textAlign: TextAlign.center),
          const SizedBox(height: 12),
          FilledButton(onPressed: onRetry, child: const Text('Réessayer')),
        ]),
      ),
    );
  }
}
