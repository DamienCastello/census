package fr.castello.census.controller;

import fr.castello.census.dto.LoginRequest;
import fr.castello.census.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Point d'entrée de l'authentification : échange un couple identifiant/mot de passe
 * contre un jeton JWT.
 *
 * <p>C'est la <strong>seule</strong> route publique. Toutes les autres exigent le jeton
 * obtenu ici.</p>
 */
@RestController
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public LoginController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Vérifie les identifiants et renvoie un jeton.
     *
     * @param request identifiant et mot de passe
     * @return le jeton JWT, à placer ensuite dans l'en-tête {@code Authorization: Bearer ...}
     */
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        // Declenche la verification du mot de passe : l'AuthenticationManager appelle
        // JpaUserDetailsService puis compare la saisie au hash BCrypt stocke en base.
        // Si les identifiants sont faux, une exception est levee et la reponse est 401.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        return jwtUtil.generateToken(request.username());
    }
}
