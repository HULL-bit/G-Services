import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

import 'app_state.dart';
import 'models.dart';
import 'theme.dart';

final _fmt = NumberFormat.decimalPattern('fr');

String money(num v, [String devise = 'XOF']) => '${_fmt.format(v.round())} $devise';

class Stars extends StatelessWidget {
  final double note;
  final double size;
  const Stars(this.note, {super.key, this.size = 16});
  @override
  Widget build(BuildContext context) {
    return Row(mainAxisSize: MainAxisSize.min, children: [
      for (var i = 1; i <= 5; i++)
        Icon(
          note >= i ? Icons.star_rounded : (note >= i - .5 ? Icons.star_half_rounded : Icons.star_border_rounded),
          size: size,
          color: GsColors.accent,
        ),
    ]);
  }
}

/// Illustration d'offre (SVG servi par le backend) avec repli sur une icône.
class OffreImage extends StatelessWidget {
  final String? path;
  final double w, h;
  const OffreImage(this.path, {super.key, this.w = 132, this.h = 92});
  @override
  Widget build(BuildContext context) {
    final origin = context.read<AppState>().api.origin;
    Widget fallback = Container(
      width: w,
      height: h,
      decoration: BoxDecoration(color: GsColors.bg, borderRadius: BorderRadius.circular(12)),
      child: const Icon(Icons.local_offer_outlined, color: GsColors.textSoft),
    );
    if (path == null || path!.isEmpty) return fallback;
    final url = '$origin/jakarta.faces.resource/$path';
    return ClipRRect(
      borderRadius: BorderRadius.circular(12),
      child: SvgPicture.network(
        url,
        width: w,
        height: h,
        fit: BoxFit.cover,
        placeholderBuilder: (_) => fallback,
      ),
    );
  }
}

class ServiceCard extends StatelessWidget {
  final ServiceListe s;
  final VoidCallback onTap;
  const ServiceCard(this.s, {super.key, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(children: [
              Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  gradient: const LinearGradient(colors: [GsColors.primary, GsColors.primary2]),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(Icons.storefront_outlined, color: Colors.white, size: 20),
              ),
              const Spacer(),
              _Badge(s.disponible ? 'Ouvert' : 'Fermé', ok: s.disponible),
            ]),
            const SizedBox(height: 10),
            Text(s.libelle, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15)),
            const SizedBox(height: 2),
            Row(children: [
              const Icon(Icons.place_outlined, size: 13, color: GsColors.textSoft),
              const SizedBox(width: 3),
              Expanded(
                child: Text(
                  '${s.categorieLibelle ?? ''}${s.villeLibelle != null ? ' · ${s.villeLibelle}' : ''}',
                  style: const TextStyle(fontSize: 12, color: GsColors.textSoft),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ]),
            const SizedBox(height: 8),
            Row(children: [
              Stars(s.noteMoyenne, size: 14),
              const SizedBox(width: 4),
              Text('(${s.nombreAvis})', style: const TextStyle(fontSize: 11, color: GsColors.textSoft)),
              const Spacer(),
              if (s.commandeADistance)
                const Icon(Icons.shopping_bag_outlined, size: 16, color: GsColors.accent),
            ]),
          ]),
        ),
      ),
    );
  }
}

class _Badge extends StatelessWidget {
  final String text;
  final bool ok;
  const _Badge(this.text, {required this.ok});
  @override
  Widget build(BuildContext context) {
    final c = ok ? GsColors.success : GsColors.danger;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(color: c.withValues(alpha: .12), borderRadius: BorderRadius.circular(999)),
      child: Text(text, style: TextStyle(fontSize: 11, color: c, fontWeight: FontWeight.w600)),
    );
  }
}

IconData iconForPi(String? pi) {
  switch (pi) {
    case 'pi pi-shopping-bag':
      return Icons.restaurant_outlined;
    case 'pi pi-tag':
      return Icons.checkroom_outlined;
    case 'pi pi-home':
      return Icons.home_repair_service_outlined;
    case 'pi pi-palette':
      return Icons.brush_outlined;
    case 'pi pi-building':
      return Icons.apartment_outlined;
    case 'pi pi-heart':
      return Icons.favorite_border;
    case 'pi pi-star':
      return Icons.auto_awesome_outlined;
    case 'pi pi-car':
      return Icons.directions_car_outlined;
    case 'pi pi-calendar':
      return Icons.celebration_outlined;
    case 'pi pi-send':
      return Icons.local_shipping_outlined;
    case 'pi pi-book':
      return Icons.school_outlined;
    case 'pi pi-desktop':
      return Icons.computer_outlined;
    default:
      return Icons.category_outlined;
  }
}

/// Libellé FR d'un motif de signalement (miroir de vitrine.signaler.motif.* côté web).
String libelleMotifSignalement(String code) {
  switch (code) {
    case 'CONTENU_INAPPROPRIE':
      return 'Contenu inapproprié';
    case 'ARNAQUE_SUSPECTEE':
      return 'Arnaque suspectée';
    case 'INFORMATIONS_ERRONEES':
      return 'Informations erronées';
    case 'SERVICE_INDISPONIBLE':
      return 'Service indisponible';
    case 'AUTRE':
      return 'Autre';
    default:
      return code;
  }
}

void showError(BuildContext context, Object e) {
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(content: Text(e.toString()), backgroundColor: GsColors.danger),
  );
}
