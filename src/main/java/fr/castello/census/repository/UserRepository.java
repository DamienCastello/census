package fr.castello.census.repository;

import fr.castello.census.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Accès aux utilisateurs.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Recherche un utilisateur par son identifiant de connexion. */
    Optional<User> findByUsername(String username);
}
