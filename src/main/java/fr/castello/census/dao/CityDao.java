package fr.castello.census.dao;

import fr.castello.census.entity.City;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CityDao {

    @PersistenceContext
    private EntityManager em;

    public List<City> extractAll() {
        TypedQuery<City> query = em.createQuery("SELECT c FROM City c", City.class);
        return query.getResultList();
    }

    public Optional<City> findById(Long id) {
        return Optional.ofNullable(em.find(City.class, id));
    }

    public List<City> findByNameStartingWith(String prefix) {
        return em.createQuery(
                        "SELECT c FROM City c WHERE LOWER(c.name) LIKE LOWER(CONCAT(:prefix, '%'))",
                        City.class)
                .setParameter("prefix", prefix)
                .getResultList();
    }

    public List<City> findByPopulationGreaterThan(int min) {
        return em.createQuery("SELECT c FROM City c WHERE c.population > :min", City.class)
                .setParameter("min", min)
                .getResultList();
    }

    public List<City> findByPopulationBetween(int min, int max) {
        return em.createQuery("SELECT c FROM City c WHERE c.population BETWEEN :min AND :max", City.class)
                .setParameter("min", min)
                .setParameter("max", max)
                .getResultList();
    }

    public List<City> findLargestByDepartment(Long departmentId, int count) {
        return em.createQuery(
                        "SELECT c FROM City c WHERE c.department.id = :depId ORDER BY c.population DESC",
                        City.class)
                .setParameter("depId", departmentId)
                .setMaxResults(count)
                .getResultList();
    }

    public List<City> findByPopulationBetweenAndDepartment(Long departmentId, int min, int max) {
        return em.createQuery(
                        "SELECT c FROM City c WHERE c.department.id = :depId AND c.population BETWEEN :min AND :max",
                        City.class)
                .setParameter("depId", departmentId)
                .setParameter("min", min)
                .setParameter("max", max)
                .getResultList();
    }

    public boolean existsByName(String name) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM City c WHERE c.name = :name", Long.class);
        query.setParameter("name", name);
        return query.getSingleResult() > 0;
    }

    public City create(City city) {
        em.persist(city);
        return city;
    }

    public void delete(City city) {
        em.remove(city);
    }
}
