package fr.castello.census.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de la sécurité.
 *
 * <p>{@code securedEnabled = true} active la prise en compte de {@code @Secured} sur les
 * méthodes. Ce n'est <strong>pas</strong> le cas par défaut (seul {@code @PreAuthorize}
 * l'est) : sans ce réglage, les {@code @Secured} des contrôleurs seraient ignorés
 * silencieusement, et tout utilisateur authentifié aurait accès à tout.</p>
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Authentification par HTTP Basic (identifiants dans l'en-tete Authorization).
        http.httpBasic(Customizer.withDefaults());

        // API sans cookie de session : la protection CSRF n'a pas lieu d'etre.
        http.csrf(AbstractHttpConfigurer::disable);

        // Le filtre ne verifie plus que l'AUTHENTIFICATION ("qui es-tu ?").
        // L'AUTORISATION ("as-tu le droit ?") est desormais portee par les @Secured
        // places sur chaque methode de controleur : la regle vit a cote du code concerne.
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}
