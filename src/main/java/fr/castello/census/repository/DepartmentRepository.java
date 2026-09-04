package fr.castello.census.repository;

import fr.castello.census.entity.Department;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux départements.
 *
 * <p>Remplace l'ancien {@code DepartmentDao} : les opérations de base sont héritées de
 * {@link JpaRepository}, les recherches spécifiques sont dérivées du nom des méthodes.</p>
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Tous les départements, <strong>villes comprises</strong>.
     *
     * <p>{@code @EntityGraph} charge la collection {@code cities} dans la même requête :
     * sans lui, le mapping vers {@code DepartmentDto} déclencherait une requête par
     * département (problème N+1 : 101 requêtes pour 100 départements).</p>
     */
    @Override
    @EntityGraph(attributePaths = "cities")
    List<Department> findAll();

    /** Recherche un département par son code (ex. « 34 »). */
    Optional<Department> findByCode(String code);

    /** Indique si un département porte déjà ce code. */
    boolean existsByCode(String code);
}
