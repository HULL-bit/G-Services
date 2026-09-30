import 'package:flutter/material.dart';

/// Palette G-SERVICES (identique au front web : navy + or).
class GsColors {
  static const primary = Color(0xFF1F3A5F);
  static const primary2 = Color(0xFF2F6FED);
  static const accent = Color(0xFFC9A227);
  static const bg = Color(0xFFF1F5F9);
  static const surface = Colors.white;
  static const textSoft = Color(0xFF64748B);
  static const success = Color(0xFF2F855A);
  static const danger = Color(0xFFE63946);
}

ThemeData gsTheme() {
  final scheme = ColorScheme.fromSeed(
    seedColor: GsColors.primary,
    primary: GsColors.primary,
    secondary: GsColors.accent,
    surface: GsColors.surface,
  );
  return ThemeData(
    useMaterial3: true,
    colorScheme: scheme,
    scaffoldBackgroundColor: GsColors.bg,
    appBarTheme: const AppBarTheme(
      backgroundColor: GsColors.primary,
      foregroundColor: Colors.white,
      elevation: 0,
      centerTitle: false,
    ),
    cardTheme: CardThemeData(
      color: GsColors.surface,
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      margin: EdgeInsets.zero,
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        backgroundColor: GsColors.primary,
        foregroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(999)),
        padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 22),
      ),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: GsColors.bg,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: BorderSide(color: Colors.grey.shade300),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: BorderSide(color: Colors.grey.shade300),
      ),
    ),
  );
}
