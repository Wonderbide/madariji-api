# Guide d'activation de l'API Text-to-Speech

## 🚨 Erreur : PERMISSION_DENIED

Si vous voyez cette erreur :
```
PERMISSION_DENIED: Cloud Text-to-Speech API has not been used in project 105619486306 before or it is disabled
```

## ✅ Solution

### 1. Activer l'API dans Google Cloud Console

1. Allez sur : https://console.cloud.google.com/apis/library
2. Recherchez "Cloud Text-to-Speech API"
3. Cliquez sur le résultat
4. Cliquez sur le bouton **ENABLE**
5. Attendez 2-3 minutes pour la propagation

### 2. Vérifier les permissions

Assurez-vous que votre service account a l'un de ces rôles :
- `Cloud Text-to-Speech User` (recommandé)
- `Cloud Text-to-Speech Admin`
- `Editor` ou `Owner` (plus de permissions que nécessaire)

### 3. Vérifier la facturation

L'API Text-to-Speech nécessite un compte de facturation actif :
- Allez dans : Billing > Link a billing account
- Assurez-vous qu'un compte de facturation est lié au projet

## 🧪 Mode Mock (pour tester sans TTS)

Pour tester l'application sans l'API TTS réelle :

```bash
# Avec Doppler
doppler run --config dev -- ./mvnw spring-boot:run -Dspring.profiles.active=mock-tts

# Sans Doppler
./mvnw spring-boot:run -Dspring.profiles.active=mock-tts
```

Le mode mock :
- Retourne un fichier MP3 silencieux
- Permet de tester l'intégration frontend
- N'utilise pas l'API Google Cloud

## 💰 Coûts

- **Gratuit** : 1 million de caractères par mois
- **Payant** : $4 par million de caractères supplémentaires
- **Voix WaveNet** : Même prix que les voix standard

## 🔍 Vérification

Pour vérifier que l'API est activée :

```bash
# Avec gcloud CLI
gcloud services list --enabled | grep texttospeech

# Ou visitez
https://console.cloud.google.com/apis/api/texttospeech.googleapis.com/overview
```

## 📝 Configuration Doppler

Assurez-vous que ces variables sont définies dans Doppler :
- `GCP_CREDENTIALS_JSON` : Le contenu JSON du service account
- Ou `GOOGLE_APPLICATION_CREDENTIALS` : Le chemin vers le fichier JSON

## 🚀 Commandes rapides

```bash
# Activer l'API via gcloud
gcloud services enable texttospeech.googleapis.com

# Vérifier le projet actuel
gcloud config get-value project

# Lister les APIs activées
gcloud services list --enabled
```