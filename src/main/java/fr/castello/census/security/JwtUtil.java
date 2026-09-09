package fr.castello.census.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Fabrique et vérifie les jetons JWT.
 *
 * <p>Un JWT est une chaîne en trois parties séparées par des points :
 * <em>en-tête.contenu.signature</em>. Les deux premières sont simplement encodées en
 * Base64 — <strong>lisibles par tout le monde</strong>. C'est la <em>signature</em>,
 * calculée avec une clé secrète que seul le serveur connaît, qui garantit que le jeton
 * n'a pas été modifié.</p>
 *
 * <p>Un JWT n'est donc <strong>pas chiffré</strong> : on n'y met jamais d'information
 * sensible, seulement de quoi identifier l'utilisateur.</p>
 */
@Component
public class JwtUtil {

    /** Durée de validité du jeton : 15 minutes. */
    private static final long VALIDITY_MS = 15L * 60 * 1000;

    // En production, ce secret n'aurait rien a faire dans le code : il viendrait
    // d'une variable d'environnement. Quiconque le connait peut forger un jeton.
    private static final String SECRET = "secret-secret-secret-secret-secret-secret";

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * Fabrique un jeton signé pour un utilisateur.
     *
     * @param username identifiant à inscrire dans le jeton
     * @return le jeton, à renvoyer au client
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + VALIDITY_MS))
                .signWith(key)
                .compact();
    }

    /**
     * Vérifie la signature et l'expiration du jeton, puis en extrait l'utilisateur.
     *
     * @param token jeton reçu dans l'en-tête Authorization
     * @return l'identifiant de l'utilisateur
     * @throws io.jsonwebtoken.JwtException si le jeton est invalide, modifié ou expiré
     */
    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)   // rejette tout jeton dont la signature ne correspond pas
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }
}
