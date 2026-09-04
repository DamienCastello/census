package fr.castello.census.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "cities")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Pas d'unicité globale : deux départements peuvent avoir une ville homonyme
    // (Saint-Denis, La Trinité…). L'unicité est contrôlée par département dans le service.
    @Column(length = 30, nullable = false)
    private String name;

    @Column(nullable = false)
    private int population;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    public City() {}

    public City(String name, int population) {
        this.name = name;
        this.population = population;
    }

    public long getId() {
        return id;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    // NB : department est volontairement exclu de equals/hashCode/toString (cycle + lazy).

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return id == city.id && population == city.population && Objects.equals(name, city.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, population);
    }

    @Override
    public String toString() {
        return "Ville{" +
                "name='" + name + '\'' +
                ", population=" + population +
                '}';
    }
}
