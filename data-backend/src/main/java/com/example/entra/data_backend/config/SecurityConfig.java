package com.example.entra.data_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Convert scope claim to authorities
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);
            Collection<GrantedAuthority> authorities = new JwtGrantedAuthoritiesConverter()
                .convert(jwt);

            // Extract scopes from 'scp' claim (Entra ID format)
            String scopes = jwt.getClaimAsString("scp");
            if (scopes != null && !scopes.isEmpty()) {
                var scopeAuthorities = Arrays.stream(scopes.split(" "))
                    .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                    .collect(Collectors.toList());
                authorities.addAll(scopeAuthorities);
            }

            // Extract roles from 'roles' claim
            java.util.List<String> roles = jwt.getClaimAsStringList("roles");
            logger.info("Raw 'roles' claim: {}", roles);
            if (roles != null) {
                var roleAuthorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role)) // Map directly to authority (e.g., "role.alpha")
                    .collect(Collectors.toList());
                authorities.addAll(roleAuthorities);
            }
            logger.info("Final Authorities: {}", authorities);

            return authorities;
        });

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v1/data/**").hasAuthority("SCOPE_Data.Read")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }
}
