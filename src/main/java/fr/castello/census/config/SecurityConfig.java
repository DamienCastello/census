package fr.castello.census.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 1) Active HTTP Basic pour les requêtes protégées
        http.httpBasic(Customizer.withDefaults());

        // Dans un contexte d’API: désactiver la protection CSRF car vous
        //n’avez pas de cookie
        http.csrf(AbstractHttpConfigurer::disable);

        // 2) Règles d'autorisation HTTP
        http.authorizeHttpRequests(auth -> auth
                // 2a) Toutes les requêtes HTTP GET sont accessibles sans authentification
                .requestMatchers(HttpMethod.GET, "/cities").permitAll()
                // 2b) Toute autre requête (POST, PUT, DELETE...) nécessite une authentification
                .anyRequest().authenticated()
        );
        return http.build();
    }
}
