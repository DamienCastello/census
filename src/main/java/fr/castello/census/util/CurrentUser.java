package fr.castello.census.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Donne le nom de l'utilisateur à l'origine de la requête en cours, pour la traçabilité.
 *
 * <p>Spring Security range l'utilisateur authentifié dans le {@code SecurityContext}
 * (une variable liée au thread courant) : il suffit de l'y lire. Quand personne n'est
 * authentifié — amorçage au démarrage, tâche planifiée, test — on renvoie « system ».</p>
 */
public final class CurrentUser {

    private CurrentUser() {
        // Classe utilitaire : pas d'instanciation.
    }

    /**
     * @return le nom de l'utilisateur courant, ou « system » si aucun n'est authentifié
     */
    public static String name() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        return authentication.getName();
    }
}
