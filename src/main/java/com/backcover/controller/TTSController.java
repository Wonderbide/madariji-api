package com.backcover.controller;

import com.backcover.dto.TTSRequest;
import com.backcover.dto.TTSResponse;
import com.backcover.model.User;
import com.backcover.service.QuotaService;
import com.backcover.service.UserService;
import com.backcover.service.tts.TTSService;
import com.backcover.util.security.AuthenticationHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
public class TTSController {

    private final TTSService ttsService;
    private final AuthenticationHelper authHelper;
    private final UserService userService;
    private final QuotaService quotaService;

    @PostMapping("/pronounce")
    public ResponseEntity<TTSResponse> pronounceWord(
            @Valid @RequestBody TTSRequest request,
            @AuthenticationPrincipal Jwt jwtPrincipal) {

        String email = jwtPrincipal.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email not found in token.");
        }

        User currentUser = userService.findOrCreateUserByEmail(email);

        // Check quota - TTS is blocked if word consultation quota is exceeded
        if (!quotaService.canConsultWord(currentUser)) {
            log.info("User {} TTS blocked - daily quota exceeded", email);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Limite quotidienne atteinte. Passez à Premium pour un accès illimité.");
        }

        log.info("TTS request for word: {} by user: {}", request.getText(), email);

        try {
            byte[] audioData = ttsService.synthesizeSpeech(request.getText());
            
            String base64Audio = Base64.getEncoder().encodeToString(audioData);
            
            TTSResponse response = TTSResponse.builder()
                    .text(request.getText())
                    .audioBase64(base64Audio)
                    .audioFormat("audio/mpeg")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error synthesizing speech for text: {}", request.getText(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/pronounce/{word}")
    public ResponseEntity<byte[]> pronounceWordDirect(
            @PathVariable String word,
            @AuthenticationPrincipal Jwt jwtPrincipal) {

        String email = jwtPrincipal.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email not found in token.");
        }

        User currentUser = userService.findOrCreateUserByEmail(email);

        // Check quota - TTS is blocked if word consultation quota is exceeded
        if (!quotaService.canConsultWord(currentUser)) {
            log.info("User {} TTS blocked - daily quota exceeded", email);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Limite quotidienne atteinte. Passez à Premium pour un accès illimité.");
        }

        log.info("Direct TTS request for word: {} by user: {}", word, email);

        try {
            byte[] audioData = ttsService.synthesizeSpeech(word);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            headers.setContentLength(audioData.length);
            headers.set("Cache-Control", "public, max-age=3600");
            
            return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            log.error("Error synthesizing speech for word: {}", word, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/cache")
    public ResponseEntity<Void> clearCache(@AuthenticationPrincipal Jwt jwtPrincipal) {
        String userId = authHelper.getSupabaseUserId(jwtPrincipal);
        log.info("Cache clear requested by user: {}", userId);
        
        ttsService.clearCache();
        return ResponseEntity.ok().build();
    }
}