package fr.castello.census.repository;

import fr.castello.census.entity.City;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Accès aux villes.
 *
 * <p>Spring Data génère l'implémentation à partir du nom des méthodes : aucune requête
 * JPQL n'est écrite ici. Les opérations de base ({@code findAll}, {@code findById},
 * {@code save}, {@code deleteById}, {@code count}…) sont héritées de
 * {@link JpaRepository}.</p>
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    /** Villes dont le nom commence par le préfixe donné (insensible à la casse). */
    List<City> findByNameStartingWithIgnoreCase(String prefix);

    /** Villes dont la population est strictement supérieure à {@code min}, les plus peuplées d'abord. */
    List<City> findByPopulationGreaterThanOrderByPopulationDesc(int min);

    /** Villes dont la population est comprise entre {@code min} et {@code max}, les plus peuplées d'abord. */
    List<City> findByPopulationBetweenOrderByPopulationDesc(int min, int max);

    /** Villes d'un département dont la population dépasse {@code min}, les plus peuplées d'abord. */
    List<City> findByDepartmentIdAndPopulationGreaterThanOrderByPopulationDesc(Long departmentId, int min);

    /** Villes d'un département dont la population est entre {@code min} et {@code max}, les plus peuplées d'abord. */
    List<City> findByDepartmentIdAndPopulationBetweenOrderByPopulationDesc(Long departmentId, int min, int max);

    /**
     * Villes d'un département triées par population décroissante.
     *
     * <p>Le {@link Pageable} sert à limiter le nombre de résultats : pour obtenir les
     * {@code n} plus grandes villes, passer {@code PageRequest.of(0, n)}.</p>
     */
    List<City> findByDepartmentIdOrderByPopulationDesc(Long departmentId, Pageable pageable);

    /** Indique si une ville de ce nom existe déjà dans ce département. */
    boolean existsByNameAndDepartmentId(String name, Long departmentId);
}
