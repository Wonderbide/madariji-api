# Backcover

Application Spring Boot pour l'apprentissage de l'arabe par la lecture extensive avec système de flashcards.

## Prérequis

- Java 21
- PostgreSQL
- Doppler CLI (pour la gestion des variables d'environnement)
- Google Cloud SDK (pour les services GCP)

## Installation

1. Cloner le repository
2. Configurer Doppler : `doppler login`
3. Installer les dépendances : `./mvnw clean install`

## Lancement

### Avec Doppler (recommandé)
```bash
doppler run -- ./mvnw spring-boot:run
```

### Avec IntelliJ
```bash
./scripts/run-intellij.sh
```

## Structure du projet

```
backcover/
├── src/                    # Code source Java
├── scripts/               # Scripts utilitaires
├── docs/                  # Documentation
├── local/                 # Stockage local (ignoré par git)
├── pom.xml               # Configuration Maven
├── CLAUDE.md             # Instructions pour Claude AI
└── CHANGELOG.md          # Historique des modifications
```

## Documentation

- [Architecture](docs/ARCHITECTURE_DOCUMENTATION.md)
- [Guide IntelliJ](docs/INTELLIJ_QUICKSTART.md)
- [Système d'analyse de mots](docs/WORD_ANALYSIS_SYSTEM_ARCHITECTURE.md)

## Déploiement

Le déploiement se fait automatiquement via GitHub Actions sur Railway.