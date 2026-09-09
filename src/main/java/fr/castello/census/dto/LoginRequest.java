package fr.castello.census.dto;

/**
 * Identifiants envoyés au endpoint {@code POST /login}.
 *
 * <p>C'est le seul endroit où le mot de passe circule en clair : une fois le jeton
 * obtenu, les requêtes suivantes n'envoient plus que celui-ci.</p>
 *
 * @param username identifiant de connexion
 * @param password mot de passe en clair
 */
public record LoginRequest(String username, String password) {
}
