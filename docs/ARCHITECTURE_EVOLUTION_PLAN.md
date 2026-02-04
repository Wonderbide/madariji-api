# Plan d'Évolution Architecture Backcover

## Contexte et Contraintes

### Métriques Cibles
- **Volume journalier** : 1700 livres/jour
- **Distribution** : 80% du volume entre 18h-22h (340 livres/heure en pic)
- **Taille moyenne** : 200 pages/livre
- **Temps acceptable** : 15 min/livre
- **Équipe** : 1 développeur + IA

### État Actuel - DEUX FLUX DISTINCTS

#### Application Principale (Synchrone)
- Spring Boot monolithe
- Vision API Google (OCR page par page)
- Enrichissement immédiat via Gemini/GPT
- PostgreSQL pour métadonnées
- **Fonctionnel mais lent** (20 min/livre)

#### Module Batch (NOUVEAU - Asynchrone)
- Vision traite TOUT le PDF → JSON complet dans R2
- CloudRequestFormatter crée JSONL pour OpenAI Batch API
- OpenAI traite en batch (moins cher, plus efficace)
- CloudResultProcessor structure les résultats
- **En cours d'intégration**

### Problème Principal
- Les deux flux coexistent sans coordination
- Duplication de traitement
- Pas de fallback entre les deux

## Phase 1 : Migration vers Batch Module (Q1 2025)
**Objectif** : Le module Batch devient le flux principal

### Actions
1. **Unification du flux**
   - Upload PDF → Vision complet → R2
   - Module Batch prend le relais automatiquement
   - App principale lit les résultats depuis R2

2. **Intégration CloudFlare Events**
   - R2 trigger → Module Batch démarre
   - Pas de polling, réaction immédiate
   - Retry automatique si échec

3. **Gestion des pics (340 livres/heure)**
   - OpenAI Batch gère naturellement le volume
   - File d'attente dans R2 (pas de perte)
   - Coût réduit de 50% vs API synchrone

**Résultat** : Un seul flux, plus rapide, moins cher, scalable

## Phase 2 : Extraction Module Enrichissement (Q2 2025)
**Objectif** : Découpler le processing lourd

### Architecture
```
┌─────────────┐       ┌──────────────┐
│   App Core  │──API──│ Enrichissement│
│  (Library)  │       │   (Process)   │
└─────────────┘       └──────────────┘
       │                      │
       └──────── R2 ──────────┘
```

### Responsabilités
- **App Core** : CRUD livres, users, business logic
- **Enrichissement** : OCR, structuration, extraction

### Communication
- API REST pour démarrage job
- Webhook pour completion
- R2 pour data exchange

**Résultat** : Processing parallèle, scale indépendant

## Phase 3 : Modularisation Services Secondaires (Q3 2025)
**Objectif** : Services autonomes pour features spécifiques

### Modules Prioritaires
1. **Dictionary Service**
   - Extraction mots après enrichissement
   - API indépendante
   - Cache propre

2. **TTS Service** 
   - Génération audio à la demande
   - Streaming direct depuis R2
   - Queue pour batch generation

3. **Cover Service**
   - CRUD covers
   - Génération IA
   - Multiple résolutions

### Pattern
```
Event Bus (Simple - PostgreSQL LISTEN/NOTIFY)
     ↓
[book.enriched] → Dictionary extrait mots
                → TTS génère preview
                → Cover génère thumbnail
```

**Résultat** : Features isolées, maintenance simplifiée

## Phase 4 : Event-Driven Architecture (Q4 2025)
**Objectif** : Résilience et scalabilité automatique

### Évolution
```
User Upload → R2 → Event [book.uploaded]
                          ↓
                   ┌──────┴──────┐
              Enrichment    Analytics
                   ↓            ↓
            [book.ready]  [stats.updated]
```

### Bénéfices
- Retry automatique
- Processing parallèle
- Tolérance aux pannes
- Monitoring simplifié

## Phase 5 : Consolidation (2026+)
**Objectif** : Système mature et stable

### Focus
1. Réduction des coûts opérationnels
2. Amélioration de l'expérience utilisateur
3. Features différenciantes (IA avancée)
4. Optimisation des coûts cloud

## Décisions Architecturales Clés

### Ce qu'on garde simple
- **Pas de microservices** : Modules, pas micro
- **Pas de Kubernetes** : Trop complexe pour 1 personne
- **Pas de multi-région** : CDN suffit
- **PostgreSQL central** : Pas de DB par module

### Ce qu'on priorise
- **Idempotence** : Tout peut être rejoué
- **Observabilité** : Logs structurés partout
- **Async par défaut** : Jamais bloquer l'API
- **Données immutables** : Append-only où possible

## Métriques de Succès par Phase

| Phase | Capacité Pic | Latence P50 | Latence P99 | Uptime |
|-------|--------------|-------------|-------------|--------|
| Actuel | 50 livres/h | 20 min | 45 min | 95% |
| Phase 1 | 340 livres/h | 10 min | 20 min | 99% |
| Phase 2 | 500 livres/h | 8 min | 15 min | 99.5% |
| Phase 3 | 600 livres/h | 6 min | 12 min | 99.9% |
| Phase 4 | 700 livres/h | 5 min | 10 min | 99.9% |

## Risques et Mitigations

### Risque Principal : Pics de Charge
**Problème** : 80% du trafic entre 18h-22h
**Mitigation** :
- Queue élastique qui absorbe les pics
- Traitement différé acceptable (users comprennent le délai)
- Notification push quand livre prêt

### Risque 2 : Sur-ingénierie
**Mitigation** : Rester sur des solutions éprouvées

### Risque 3 : Coût Cloud pendant pics
**Mitigation** : 
- Auto-scaling horaire programmé
- Spot instances pour workers non-critiques

## Prochaines Étapes Immédiates

1. **Semaine 1** : Mesurer précisément les patterns de charge actuels
2. **Semaine 2** : Implémenter queue avec workers parallèles
3. **Semaine 3** : Ajouter monitoring des pics (Grafana)
4. **Semaine 4** : Test de charge simulant 340 livres/heure

## Notes pour l'Implémentation

- Chaque module = 1 repository Git
- CI/CD simple (GitHub Actions)
- Feature flags pour rollout progressif
- Rollback toujours possible en < 5 min
- Documentation API first (OpenAPI)