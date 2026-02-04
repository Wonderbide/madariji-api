# Variables d'environnement - Backcover

## Légende
- **REQUIS** : Doit être défini, pas de default
- **OPTIONNEL** : A un default, peut être omis en dev
- **ACC/PROD** : Doit être défini en acceptance et production

---

## Base de données

| Variable | Requis | Default | Description |
|----------|--------|---------|-------------|
| `DB_URL` | REQUIS | - | URL JDBC PostgreSQL (ex: `jdbc:postgresql://host:5432/db`) |
| `DB_USERNAME` | REQUIS | - | Utilisateur PostgreSQL |
| `DB_PASSWORD` | REQUIS | - | Mot de passe PostgreSQL |

---

## Authentification (Supabase)

| Variable | Requis | Default | Description |
|----------|--------|---------|-------------|
| `SUPABASE_URL` | REQUIS | - | URL du projet Supabase (ex: `https://xxx.supabase.co`) |
| `SUPABASE_JWT_SECRET` | REQUIS | - | Secret JWT pour validation des tokens |
| `SUPABASE_ANON_KEY` | REQUIS | - | Clé anonyme Supabase |

---

## Stockage R2 (Cloudflare)

| Variable | Requis | Default | Description |
|----------|--------|---------|-------------|
| `R2_ENDPOINT` | REQUIS | - | Endpoint S3-compatible R2 |
| `R2_ACCESS_KEY_ID` | REQUIS | - | Access key R2 |
| `R2_SECRET_ACCESS_KEY` | REQUIS | - | Secret key R2 |
| `R2_BUCKET_NAME` | REQUIS | - | Nom du bucket R2 |
| `R2_REGION` | OPTIONNEL | `us-east-1` | Région R2 |

---

## Paiement (Stripe)

| Variable | Requis | Default | Description |
|----------|--------|---------|-------------|
| `STRIPE_SECRET_KEY` | ACC/PROD | vide | Clé secrète Stripe |
| `STRIPE_PUBLISHABLE_KEY` | ACC/PROD | vide | Clé publique Stripe |
| `STRIPE_PRICE_ID` | ACC/PROD | vide | ID du prix d'abonnement |
| `STRIPE_WEBHOOK_SECRET` | ACC/PROD | vide | Secret pour webhooks Stripe |

---

## IA / Enrichissement

| Variable | Requis | Default | Description |
|----------|--------|---------|-------------|
| `GEMINI_API_KEY` | REQUIS | - | Clé API Google Gemini |

---

## Frontend

| Variable | Requis | Default | Description |
|----------|--------|---------|-------------|
| `FRONTEND_URL` | ACC/PROD | `http://localhost:5173` | URL du frontend (pour redirections Stripe) |
| `CORS_ALLOWED_ORIGINS` | ACC/PROD | `http://localhost:5173,https://localhost:5173` | Origins CORS autorisées (séparées par virgules) |

---

## GCP (désactivé - legacy)

| Variable | Requis | Default | Description |
|----------|--------|---------|-------------|
| `GCP_PROJECT_ID` | OPTIONNEL | `disabled` | ID projet GCP (Pub/Sub désactivé) |
| `GCP_CREDENTIALS_PATH` | OPTIONNEL | vide | Chemin vers credentials JSON |
| `GCS_BUCKET_NAME` | OPTIONNEL | `disabled` | Bucket GCS (non utilisé, R2 est primary) |

---

## Monitoring (Production)

| Variable | Requis | Default | Description |
|----------|--------|---------|-------------|
| `SENTRY_DSN` | PROD | vide | DSN Sentry pour error tracking |

---

## Configuration par environnement

### Développement (local)
```bash
# Via Doppler: doppler run -- ./mvnw spring-boot:run
# Ou définir manuellement les variables REQUIS
```

### Acceptance (ACC)
```bash
SPRING_PROFILES_ACTIVE=production
# + Toutes les variables REQUIS et ACC/PROD
FRONTEND_URL=https://acc.takrar.com
CORS_ALLOWED_ORIGINS=https://acc.takrar.com
```

### Production (PROD)
```bash
SPRING_PROFILES_ACTIVE=production
# + Toutes les variables REQUIS et ACC/PROD
FRONTEND_URL=https://takrar.com
CORS_ALLOWED_ORIGINS=https://takrar.com,https://www.takrar.com
SENTRY_DSN=https://xxx@sentry.io/xxx
```
