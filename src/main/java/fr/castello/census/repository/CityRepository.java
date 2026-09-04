package fr.castello.census.repository;

import fr.castello.census.entity.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Accès aux villes.
 *
 * <p>Spring Data génère l'implémentation à partir du nom des méthodes : aucune requête
 * JPQL n'est écrite ici. Les opérations de base ({@code findById}, {@code save},
 * {@code deleteById}, {@code count}…) sont héritées de {@link JpaRepository}.</p>
 *
 * <p>{@code @EntityGraph(attributePaths = "department")} sur les lectures : le mapping vers
 * {@code CityDto} lit le code du département, ce qui déclencherait sinon une requête par
 * ville (problème N+1). L'annotation ramène le département dans la même requête, sans avoir
 * à réécrire les requêtes dérivées en JPQL.</p>
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    /** Page de villes, département compris. */
    @Override
    @EntityGraph(attributePaths = "department")
    Page<City> findAll(Pageable pageable);

    /** Toutes les villes, département compris (utilisé par l'export CSV). */
    @Override
    @EntityGraph(attributePaths = "department")
    List<City> findAll();

    /** Villes dont le nom commence par le préfixe donné (insensible à la casse). */
    @EntityGraph(attributePaths = "department")
    List<City> findByNameStartingWithIgnoreCase(String prefix);

    /** Villes dont la population est strictement supérieure à {@code min}, les plus peuplées d'abord. */
    @EntityGraph(attributePaths = "department")
    List<City> findByPopulationGreaterThanOrderByPopulationDesc(int min);

    /** Villes dont la population est comprise entre {@code min} et {@code max}, les plus peuplées d'abord. */
    @EntityGraph(attributePaths = "department")
    List<City> findByPopulationBetweenOrderByPopulationDesc(int min, int max);

    /** Villes d'un département dont la population dépasse {@code min}, les plus peuplées d'abord. */
    @EntityGraph(attributePaths = "department")
    List<City> findByDepartmentIdAndPopulationGreaterThanOrderByPopulationDesc(Long departmentId, int min);

    /** Villes d'un département dont la population est entre {@code min} et {@code max}, les plus peuplées d'abord. */
    @EntityGraph(attributePaths = "department")
    List<City> findByDepartmentIdAndPopulationBetweenOrderByPopulationDesc(Long departmentId, int min, int max);

    /**
     * Villes d'un département triées par population décroissante.
     *
     * <p>Le {@link Pageable} sert à limiter le nombre de résultats : pour obtenir les
     * {@code n} plus grandes villes, passer {@code PageRequest.of(0, n)}.</p>
     */
    @EntityGraph(attributePaths = "department")
    List<City> findByDepartmentIdOrderByPopulationDesc(Long departmentId, Pageable pageable);

    /** Indique si une ville de ce nom existe déjà dans ce département. */
    boolean existsByNameAndDepartmentId(String name, Long departmentId);
}
