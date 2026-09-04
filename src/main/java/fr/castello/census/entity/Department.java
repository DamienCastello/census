package fr.castello.census.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 10, nullable = false, unique = true)
    private String code;

    // Nullable : le jeu de données importé ne renseigne pas le nom des départements.
    @Column(length = 100)
    private String name;

    @OneToMany(mappedBy = "department")
    private List<City> cities = new ArrayList<>();

    public Department() {}

    public Department(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<City> getCities() {
        return cities;
    }

    /**
     * Rattache une ville à ce département en maintenant les deux côtés de la relation.
     *
     * @param city ville à rattacher
     */
    public void addCity(City city) {
        cities.add(city);
        city.setDepartment(this);
    }

    // NB : la relation (cities) est volontairement exclue de equals/hashCode/toString
    // pour éviter les cycles et le chargement lazy involontaire.

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return id == that.id && Objects.equals(code, that.code) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name);
    }

    @Override
    public String toString() {
        return "Department{code='" + code + "', name='" + name + "'}";
    }
}
