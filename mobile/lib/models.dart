// Modèles alignés sur les DTO de l'API (com.gservices.dto.*).

class AuthUser {
  final int id;
  final String login;
  final String nomComplet;
  final List<String> profils;

  AuthUser({required this.id, required this.login, required this.nomComplet, required this.profils});

  factory AuthUser.fromJson(Map<String, dynamic> j) => AuthUser(
        id: j['id'],
        login: j['login'] ?? '',
        nomComplet: j['nomComplet'] ?? '',
        profils: (j['profils'] as List?)?.map((e) => e.toString()).toList() ?? const [],
      );

  bool get estFournisseur => profils.contains('PRESTATAIRE');
  bool get estClient => profils.contains('CLIENT');
}

class Categorie {
  final int id;
  final String libelle;
  final String? description;
  final String icone;
  final int nbServices;

  Categorie({required this.id, required this.libelle, this.description, required this.icone, required this.nbServices});

  factory Categorie.fromJson(Map<String, dynamic> j) => Categorie(
        id: j['id'],
        libelle: j['libelle'] ?? '',
        description: j['description'],
        icone: j['icone'] ?? '',
        nbServices: j['nbServices'] ?? 0,
      );
}

class ServiceListe {
  final int id;
  final String libelle;
  final String? description;
  final String? categorieLibelle;
  final String? villeLibelle;
  final bool disponible;
  final double noteMoyenne;
  final int nombreAvis;
  final double? latitude;
  final double? longitude;
  final String? adresse;
  final bool commandeADistance;

  ServiceListe({
    required this.id,
    required this.libelle,
    this.description,
    this.categorieLibelle,
    this.villeLibelle,
    required this.disponible,
    required this.noteMoyenne,
    required this.nombreAvis,
    this.latitude,
    this.longitude,
    this.adresse,
    required this.commandeADistance,
  });

  factory ServiceListe.fromJson(Map<String, dynamic> j) => ServiceListe(
        id: j['id'],
        libelle: j['libelle'] ?? '',
        description: j['description'],
        categorieLibelle: j['categorieLibelle'],
        villeLibelle: j['villeLibelle'],
        disponible: j['disponible'] ?? true,
        noteMoyenne: (j['noteMoyenne'] ?? 0).toDouble(),
        nombreAvis: j['nombreAvis'] ?? 0,
        latitude: (j['latitude'] as num?)?.toDouble(),
        longitude: (j['longitude'] as num?)?.toDouble(),
        adresse: j['adresse'],
        commandeADistance: j['commandeADistance'] ?? false,
      );
}

class Article {
  final int id;
  final String reference;
  final String? description;
  final double prix;
  final double prixNet;
  final String devise;
  final bool promotion;
  final double tauxRemise;
  final bool disponibilite;

  Article({
    required this.id,
    required this.reference,
    this.description,
    required this.prix,
    required this.prixNet,
    required this.devise,
    required this.promotion,
    required this.tauxRemise,
    required this.disponibilite,
  });

  factory Article.fromJson(Map<String, dynamic> j) => Article(
        id: j['id'],
        reference: j['reference'] ?? '',
        description: j['description'],
        prix: (j['prix'] ?? 0).toDouble(),
        prixNet: (j['prixNet'] ?? j['prix'] ?? 0).toDouble(),
        devise: j['devise'] ?? 'XOF',
        promotion: j['promotion'] ?? false,
        tauxRemise: (j['tauxRemisePourcentage'] ?? 0).toDouble(),
        disponibilite: j['disponibilite'] ?? true,
      );
}

class Offre {
  final int id;
  final String libelle;
  final String? description;
  final String? image;
  final List<Article> articles;

  Offre({required this.id, required this.libelle, this.description, this.image, required this.articles});

  factory Offre.fromJson(Map<String, dynamic> j) => Offre(
        id: j['id'],
        libelle: j['libelle'] ?? '',
        description: j['description'],
        image: j['image'],
        articles: (j['articles'] as List?)?.map((e) => Article.fromJson(e)).toList() ?? const [],
      );
}

class Catalogue {
  final int id;
  final String libelle;
  final String? description;
  final String? icone;
  final List<Offre> produits;

  Catalogue({required this.id, required this.libelle, this.description, this.icone, required this.produits});

  factory Catalogue.fromJson(Map<String, dynamic> j) => Catalogue(
        id: j['id'],
        libelle: j['libelle'] ?? '',
        description: j['description'],
        icone: j['icone'],
        produits: (j['produits'] as List?)?.map((e) => Offre.fromJson(e)).toList() ?? const [],
      );
}

class Horaire {
  final String jourLibelle;
  final bool ouvert24h;
  final String? heureOuverture;
  final String? heureFermeture;

  Horaire({required this.jourLibelle, required this.ouvert24h, this.heureOuverture, this.heureFermeture});

  factory Horaire.fromJson(Map<String, dynamic> j) => Horaire(
        jourLibelle: j['jourLibelle'] ?? '',
        ouvert24h: j['ouvert24h'] ?? false,
        heureOuverture: j['heureOuverture'],
        heureFermeture: j['heureFermeture'],
      );

  String get texte => ouvert24h ? '24h/24' : '${heureOuverture ?? '—'} – ${heureFermeture ?? '—'}';
}

class Avis {
  final int id;
  final String personneNom;
  final int note;
  final String? commentaire;
  final String? reponsePrestataire;
  final DateTime? date;

  Avis({required this.id, required this.personneNom, required this.note, this.commentaire, this.reponsePrestataire, this.date});

  factory Avis.fromJson(Map<String, dynamic> j) => Avis(
        id: j['id'],
        personneNom: j['personneNom'] ?? 'Client',
        note: j['note'] ?? 0,
        commentaire: j['commentaire'],
        reponsePrestataire: j['reponsePrestataire'],
        date: j['date'] != null ? DateTime.tryParse(j['date'].toString()) : null,
      );
}

class Prestataire {
  final String? adresse;
  final String? telephone1;
  final String? email1;
  final String? siteWeb;
  final double? latitude;
  final double? longitude;
  final String? positionLibelle;

  Prestataire({this.adresse, this.telephone1, this.email1, this.siteWeb, this.latitude, this.longitude, this.positionLibelle});

  factory Prestataire.fromJson(Map<String, dynamic> j) => Prestataire(
        adresse: j['adresse'],
        telephone1: j['telephone1'],
        email1: j['email1'],
        siteWeb: j['siteWeb'],
        latitude: (j['latitude'] as num?)?.toDouble(),
        longitude: (j['longitude'] as num?)?.toDouble(),
        positionLibelle: j['positionLibelle'],
      );
}

class ServiceDetail {
  final ServiceListe service;
  final Prestataire? prestataire;
  final List<Horaire> horaires;
  final List<Catalogue> catalogues;
  final List<Avis> avis;
  final double moyenne;
  final int totalAvis;

  ServiceDetail({
    required this.service,
    this.prestataire,
    required this.horaires,
    required this.catalogues,
    required this.avis,
    required this.moyenne,
    required this.totalAvis,
  });

  factory ServiceDetail.fromJson(Map<String, dynamic> j) {
    final synth = j['synthese'] as Map<String, dynamic>?;
    return ServiceDetail(
      service: ServiceListe.fromJson(j['service']),
      prestataire: j['prestataire'] != null ? Prestataire.fromJson(j['prestataire']) : null,
      horaires: (j['horaires'] as List?)?.map((e) => Horaire.fromJson(e)).toList() ?? const [],
      catalogues: (j['catalogues'] as List?)?.map((e) => Catalogue.fromJson(e)).toList() ?? const [],
      avis: (j['avis'] as List?)?.map((e) => Avis.fromJson(e)).toList() ?? const [],
      moyenne: (synth?['moyenne'] ?? j['service']?['noteMoyenne'] ?? 0).toDouble(),
      totalAvis: synth?['total'] ?? (j['avis'] as List?)?.length ?? 0,
    );
  }
}

class LigneCommande {
  final String articleReference;
  final String? articleDescription;
  final int quantite;
  final double sousTotal;

  LigneCommande({required this.articleReference, this.articleDescription, required this.quantite, required this.sousTotal});

  factory LigneCommande.fromJson(Map<String, dynamic> j) => LigneCommande(
        articleReference: j['articleReference'] ?? '',
        articleDescription: j['articleDescription'],
        quantite: j['quantite'] ?? 1,
        sousTotal: (j['sousTotal'] ?? 0).toDouble(),
      );
}

class Favori {
  final int id;
  final int serviceId;
  final String serviceLibelle;
  final String? serviceDescription;
  final String? categorieLibelle;
  final String? categorieIcone;
  final String? villeLibelle;
  final bool disponible;
  final double noteMoyenne;
  final int nombreAvis;
  final DateTime? dateAjout;

  Favori({
    required this.id,
    required this.serviceId,
    required this.serviceLibelle,
    this.serviceDescription,
    this.categorieLibelle,
    this.categorieIcone,
    this.villeLibelle,
    required this.disponible,
    required this.noteMoyenne,
    required this.nombreAvis,
    this.dateAjout,
  });

  factory Favori.fromJson(Map<String, dynamic> j) => Favori(
        id: j['id'],
        serviceId: j['serviceId'],
        serviceLibelle: j['serviceLibelle'] ?? '',
        serviceDescription: j['serviceDescription'],
        categorieLibelle: j['categorieLibelle'],
        categorieIcone: j['categorieIcone'],
        villeLibelle: j['villeLibelle'],
        disponible: j['disponible'] ?? true,
        noteMoyenne: (j['noteMoyenne'] ?? 0).toDouble(),
        nombreAvis: j['nombreAvis'] ?? 0,
        dateAjout: j['dateAjout'] != null ? DateTime.tryParse(j['dateAjout'].toString()) : null,
      );

  /// Vue simplifiée pour réutiliser [ServiceCard].
  ServiceListe get commeService => ServiceListe(
        id: serviceId,
        libelle: serviceLibelle,
        description: serviceDescription,
        categorieLibelle: categorieLibelle,
        villeLibelle: villeLibelle,
        disponible: disponible,
        noteMoyenne: noteMoyenne,
        nombreAvis: nombreAvis,
        commandeADistance: false,
      );
}

class Commande {
  final int id;
  final String reference;
  final String statut;
  final String? serviceLibelle;
  final double montantTotal;
  final DateTime? date;
  final List<LigneCommande> lignes;

  Commande({
    required this.id,
    required this.reference,
    required this.statut,
    this.serviceLibelle,
    required this.montantTotal,
    this.date,
    required this.lignes,
  });

  factory Commande.fromJson(Map<String, dynamic> j) => Commande(
        id: j['id'],
        reference: j['reference'] ?? '',
        statut: j['statut'] ?? 'NOUVELLE',
        serviceLibelle: j['serviceLibelle'],
        montantTotal: (j['montantTotal'] ?? 0).toDouble(),
        date: j['dateCommande'] != null ? DateTime.tryParse(j['dateCommande'].toString()) : null,
        lignes: (j['lignes'] as List?)?.map((e) => LigneCommande.fromJson(e)).toList() ?? const [],
      );
}
