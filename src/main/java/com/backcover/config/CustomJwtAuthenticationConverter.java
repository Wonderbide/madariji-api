package com.backcover.config;

import com.backcover.model.User;
import com.backcover.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;

    @Autowired
    public CustomJwtAuthenticationConverter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Extraire l'email du JWT
        String email = jwt.getClaimAsString("email");
        
        if (email != null && !email.isBlank()) {
            // Récupérer le rôle depuis la base de données en utilisant l'email
            Optional<User> userOpt = userService.findUserByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                authorities.add(new SimpleGrantedAuthority(user.getRole()));
            } else {
                // Si l'utilisateur n'existe pas encore, lui donner ROLE_FREE par défaut
                authorities.add(new SimpleGrantedAuthority("ROLE_FREE"));
            }
        } else {
            // Si pas d'email dans le JWT, donner ROLE_FREE par défaut
            authorities.add(new SimpleGrantedAuthority("ROLE_FREE"));
        }
        
        return new JwtAuthenticationToken(jwt, authorities);
    }
}