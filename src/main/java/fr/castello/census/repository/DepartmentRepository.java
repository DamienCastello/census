package fr.castello.census.repository;

import fr.castello.census.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Accès aux départements.
 *
 * <p>Remplace l'ancien {@code DepartmentDao} : les opérations de base sont héritées de
 * {@link JpaRepository}, les recherches spécifiques sont dérivées du nom des méthodes.</p>
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /** Recherche un département par son code (ex. « 34 »). */
    Optional<Department> findByCode(String code);

    /** Indique si un département porte déjà ce code. */
    boolean existsByCode(String code);
}
