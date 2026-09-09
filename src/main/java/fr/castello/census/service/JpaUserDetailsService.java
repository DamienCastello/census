package fr.castello.census.service;

import fr.castello.census.entity.Role;
import fr.castello.census.entity.User;
import fr.castello.census.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Charge les utilisateurs depuis la base de données.
 *
 * <p>Remplace l'implémentation en mémoire du TP précédent : Spring Security appelle
 * {@link #loadUserByUsername(String)} à chaque authentification, et compare le mot de
 * passe fourni au hash BCrypt stocké en base.</p>
 */
@Service
public class JpaUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(JpaUserDetailsService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public JpaUserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Notre entité User implémente déjà UserDetails : elle est renvoyée telle quelle,
     * aucune conversion nécessaire.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur inconnu : " + username));
    }

    /**
     * Crée les deux utilisateurs de démonstration si la table est vide.
     *
     * <p>Le mot de passe n'est <strong>jamais</strong> stocké en clair : BCrypt en garde
     * une empreinte non réversible, que Spring Security compare à la saisie.</p>
     *
     * <p>Appelée par {@code DataConfig}, qui décide s'il faut l'exécuter. Le test
     * « table vide » la rend rejouable sans risque.</p>
     */
    @Transactional
    public void initData() {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.save(new User("john", passwordEncoder.encode("123"), List.of(Role.USER)));
        userRepository.save(new User("mozinor", passwordEncoder.encode("000"), List.of(Role.ADMIN)));

        log.info("Utilisateurs de démonstration créés : john (USER), mozinor (ADMIN).");
    }
}
