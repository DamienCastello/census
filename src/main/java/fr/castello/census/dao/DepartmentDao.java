package fr.castello.census.dao;

import fr.castello.census.entity.Department;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DepartmentDao {

    @PersistenceContext
    private EntityManager em;

    public List<Department> extractAll() {
        return em.createQuery("SELECT d FROM Department d", Department.class).getResultList();
    }

    public Optional<Department> findById(Long id) {
        return Optional.ofNullable(em.find(Department.class, id));
    }

    public boolean existsByCode(String code) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(d) FROM Department d WHERE d.code = :code", Long.class);
        query.setParameter("code", code);
        return query.getSingleResult() > 0;
    }

    public Department create(Department department) {
        em.persist(department);
        return department;
    }

    public void delete(Department department) {
        em.remove(department);
    }
}
