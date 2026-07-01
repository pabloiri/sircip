package com.cabreras.sircip;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/**").permitAll() // Rutas libres
//                .requestMatchers("/api/v1/padron/**").permitAll() // Rutas libres
//                .anyRequest().authenticated()                  // Todo lo demás requiere Token
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {}) // Habilita la validación de tokens JWT
            );
        return http.build();
    }
}
