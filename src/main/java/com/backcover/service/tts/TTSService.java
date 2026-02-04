package com.backcover.service.tts;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TTSService {

    private final TextToSpeechClient ttsClient;
    private final TTSCacheService ttsCacheService;
    
    @Value("${tts.voice.language:ar-XA}")
    private String voiceLanguage;
    
    @Value("${tts.voice.name:ar-XA-Wavenet-B}")
    private String voiceName;
    
    @Value("${tts.audio.encoding:MP3}")
    private String audioEncoding;
    
    @PostConstruct
    public void init() {
        log.info("[STARTUP] TTSService @PostConstruct - START");
        log.info("[TTS] Using R2 for TTS cache storage");
        log.info("[TTS] Voice configuration: language={}, name={}, encoding={}", 
                voiceLanguage, voiceName, audioEncoding);
        log.info("[STARTUP] TTSService @PostConstruct - SUCCESS");
    }

    public byte[] synthesizeSpeech(String text) throws IOException {
        String cacheKey = generateCacheKey(text);
        
        // Vérifier le cache R2
        Optional<byte[]> cachedAudio = ttsCacheService.getCachedAudio(cacheKey);
        if (cachedAudio.isPresent()) {
            log.debug("[TTS] Cache hit for text: {}", text);
            return cachedAudio.get();
        }
        
        log.info("[TTS] Cache miss - Synthesizing speech for text: {}", text);
        
        SynthesisInput input = SynthesisInput.newBuilder()
                .setText(text)
                .build();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(voiceLanguage)
                .setName(voiceName)
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.valueOf(audioEncoding))
                .build();

        SynthesizeSpeechResponse response = ttsClient.synthesizeSpeech(input, voice, audioConfig);
        
        ByteString audioContents = response.getAudioContent();
        byte[] audioData = audioContents.toByteArray();
        
        // Sauvegarder dans le cache R2
        ttsCacheService.cacheAudio(cacheKey, audioData, voiceName, text);
        
        return audioData;
    }
    
    private String generateCacheKey(String text) {
        return DigestUtils.md5Hex(text + "_" + voiceName + "_v1");
    }
    
    /**
     * Méthode clearCache conservée pour compatibilité
     * Le nettoyage du cache R2 devrait être géré différemment (TTL, politique de rétention)
     */
    public void clearCache() {
        log.info("[TTS] Cache clearing not implemented for R2 - manage via R2 lifecycle policies");
    }
}