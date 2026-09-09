package fr.castello.census.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authentifie chaque requête à partir du jeton JWT.
 *
 * <p>Spring Security ne fournit aucun filtre pour ce cas : il faut l'écrire. À chaque
 * requête, ce filtre lit l'en-tête {@code Authorization: Bearer <jeton>}, vérifie le
 * jeton, puis place l'utilisateur dans le {@code SecurityContext} — c'est-à-dire
 * exactement ce que faisait l'authentification HTTP Basic, mais sans redemander le mot
 * de passe.</p>
 *
 * <p>{@link OncePerRequestFilter} garantit une seule exécution par requête : la chaîne
 * de filtres peut être parcourue plusieurs fois (redirection interne, page d'erreur).</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HEADER);

        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length());
            try {
                String username = jwtUtil.extractUsername(token);
                UserDetails user = userDetailsService.loadUserByUsername(username);

                // On reconstitue l'authentification sans mot de passe (null) : la preuve
                // d'identite, c'est la signature du jeton, deja verifiee ci-dessus.
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        username, null, user.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException e) {
                // Jeton invalide, modifie ou expire : on laisse le contexte vide.
                // La chaine de filtres repondra 401 puisque personne n'est authentifie.
                SecurityContextHolder.clearContext();
            }
        }

        // Toujours poursuivre : l'absence de jeton n'est pas une erreur ici
        // (la route /login, par exemple, est publique).
        filterChain.doFilter(request, response);
    }
}
