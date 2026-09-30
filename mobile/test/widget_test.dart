import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:gservices_mobile/theme.dart';
import 'package:gservices_mobile/widgets.dart';

void main() {
  test('le thème G-SERVICES est construit', () {
    final t = gsTheme();
    expect(t.useMaterial3, isTrue);
    expect(t.colorScheme.primary, GsColors.primary);
  });

  test('formatage monétaire XOF', () {
    expect(money(12000), '12 000 XOF');
  });

  testWidgets('le widget Stars affiche 5 icônes', (tester) async {
    await tester.pumpWidget(const MaterialApp(home: Scaffold(body: Stars(3.5))));
    expect(find.byType(Icon), findsNWidgets(5));
  });
}
