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

    @Column(length = 100, nullable = false)
    private String nom;

    @OneToMany(mappedBy = "department")
    private List<City> cities = new ArrayList<>();

    public Department() {}

    public Department(String code, String nom) {
        this.code = code;
        this.nom = nom;
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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
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
        return id == that.id && Objects.equals(code, that.code) && Objects.equals(nom, that.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, nom);
    }

    @Override
    public String toString() {
        return "Departement{code='" + code + "', nom='" + nom + "'}";
    }
}
