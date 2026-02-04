# Text-to-Speech (TTS) Feature for Arabic Books

## Overview
This feature provides pronunciation assistance for Arabic words in books, helping users who can read Arabic but need help with pronunciation.

## API Endpoints

### 1. Pronounce Word (JSON Response)
```
POST /api/tts/pronounce
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
    "text": "مرحبا"
}

Response:
{
    "text": "مرحبا",
    "audioBase64": "base64_encoded_audio_data",
    "audioFormat": "audio/mpeg"
}
```

### 2. Direct Audio Response
```
GET /api/tts/pronounce/{word}
Authorization: Bearer {JWT_TOKEN}

Response: Binary audio file (MP3)
Headers:
  Content-Type: audio/mpeg
  Cache-Control: public, max-age=3600
```

### 3. Clear Cache
```
DELETE /api/tts/cache
Authorization: Bearer {JWT_TOKEN}
```

## Configuration

### Environment Variables
- `GOOGLE_APPLICATION_CREDENTIALS` or `GCP_CREDENTIALS_JSON` (via Doppler): Google Cloud credentials
- `TTS_VOICE_LANGUAGE`: Voice language code (default: ar-XA)
- `TTS_VOICE_NAME`: Voice name (default: ar-XA-Wavenet-B)
- `TTS_AUDIO_ENCODING`: Audio format (default: MP3)

### File Storage
The TTS cache follows the same pattern as other local storage in the application:
- Base directory: `local/` (configurable via `file.storage.base-dir-name`)
- TTS cache subdirectory: `tts/words/` (configurable via `file.storage.tts-subdir-name`)
- Full default path: `local/tts/words/`

### Available Arabic Voices
- `ar-XA-Wavenet-A`: Female voice
- `ar-XA-Wavenet-B`: Male voice (default)
- `ar-XA-Wavenet-C`: Male voice (alternative)
- `ar-XA-Wavenet-D`: Female voice (alternative)

## Frontend Integration

### Example JavaScript Implementation
```javascript
async function pronounceWord(word) {
    try {
        const response = await fetch(`/api/tts/pronounce/${encodeURIComponent(word)}`, {
            headers: {
                'Authorization': `Bearer ${getAuthToken()}`
            }
        });
        
        if (response.ok) {
            const audioBlob = await response.blob();
            const audioUrl = URL.createObjectURL(audioBlob);
            const audio = new Audio(audioUrl);
            await audio.play();
            
            // Clean up
            audio.addEventListener('ended', () => {
                URL.revokeObjectURL(audioUrl);
            });
        }
    } catch (error) {
        console.error('Error pronouncing word:', error);
    }
}
```

## Cache Management
- Audio files are cached locally to improve performance
- Cache files are stored in: `local/tts/words/{MD5_hash}.mp3`
- Files are served from cache on subsequent requests
- Cache can be cleared via the API endpoint
- The directory structure is created automatically on application startup

## Cost Estimation
- Google Cloud TTS pricing: $4 per 1 million characters
- Average Arabic word: ~5 characters
- Cost per word: ~$0.00002
- 50,000 word pronunciations = ~$1

## Performance
- First request: ~200-300ms (API call + cache)
- Cached requests: <10ms (file read)
- Cache-Control header allows browser caching