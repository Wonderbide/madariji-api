package com.backcover.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class SupabaseJwtConfig {
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    
    @Bean
    public JwtDecoder jwtDecoder() {
        // Utiliser la validation par clé publique (JWKS) via l'URL de l'issuer
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);

        // Configurer un validateur qui vérifie l'émetteur mais ignore l'audience
        // pour éviter les problèmes de "aud" claim (souvent "authenticated" vs "account")
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        
        // On utilise uniquement le validateur d'issuer (et timestamp par défaut), sans ajouter de validateur d'audience
        jwtDecoder.setJwtValidator(withIssuer);
        
        return jwtDecoder;
    }
}