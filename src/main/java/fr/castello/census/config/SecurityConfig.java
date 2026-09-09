package fr.castello.census.config;

import fr.castello.census.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de la sécurité, en mode JWT.
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
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthenticationFilter jwtFilter) throws Exception {

        // Plus de httpBasic : le mot de passe n'est envoye qu'une fois, sur /login.
        // Les requetes suivantes presentent le jeton obtenu en retour.

        // API sans cookie de session : la protection CSRF n'a pas lieu d'etre.
        http.csrf(AbstractHttpConfigurer::disable);

        // STATELESS : le serveur ne conserve aucune session. Chaque requete doit porter
        // son jeton — c'est ce qui permet de repartir la charge sur plusieurs serveurs.
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login").permitAll()   // seule route publique
                // /error doit rester libre : quand Spring renvoie une erreur (403 par
                // exemple), il redispatche en interne vers /error. Si cette route exigeait
                // une authentification, ce second passage renverrait 401 et ECRASERAIT
                // le code d'erreur d'origine.
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()            // l'autorisation fine est dans les @Secured
        );

        // Sans httpBasic, plus aucun "point d'entree d'authentification" n'est declare :
        // Spring repondrait 403 a une requete sans jeton. On retablit le 401, qui est la
        // reponse correcte pour "tu n'es pas authentifie" (403 = "authentifie mais interdit").
        http.exceptionHandling(ex -> ex.authenticationEntryPoint(
                (request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentification requise")));

        // Notre filtre doit s'executer AVANT le filtre standard de Spring Security :
        // il place l'utilisateur dans le SecurityContext a partir du jeton, sinon la
        // requete arriverait au controleur sans etre authentifiee.
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Expose l'AuthenticationManager pour que LoginController puisse vérifier
     * le mot de passe lors de l'appel à /login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}
