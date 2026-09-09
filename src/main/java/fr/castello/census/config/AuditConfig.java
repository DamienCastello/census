package fr.castello.census.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Active la traçabilité des modifications (voir {@code Auditable}).
 *
 * <p>{@code @EnableJpaAuditing} met en route le mécanisme ; l'{@link AuditorAware}
 * ci-dessous lui indique <strong>qui</strong> est l'utilisateur courant.</p>
 */
@Configuration
@EnableJpaAuditing
public class AuditConfig {

    /**
     * Fournit le nom de l'utilisateur à inscrire dans {@code updated_by}.
     *
     * <p>Spring Security range l'utilisateur authentifié dans le {@code SecurityContext} :
     * il suffit de l'y lire. Quand personne n'est authentifié — l'amorçage au démarrage,
     * par exemple — on inscrit « system » plutôt que de laisser la colonne vide.</p>
     *
     * @return le nom de l'auteur de la modification
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                return Optional.of("system");
            }
            return Optional.of(authentication.getName());
        };
    }
}
