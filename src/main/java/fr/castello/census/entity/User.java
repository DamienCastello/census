package fr.castello.census.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
// "user" est un mot reserve dans plusieurs bases (dont H2, utilise par les tests) : on nomme la table "users".
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    /*
     * Les roles forment un ensemble FIXE, connu a la compilation : un enum suffit,
     * pas besoin d'une entite Role ni d'un RoleRepository.
     *
     * @ElementCollection cree la table de liaison user_roles(user_id, role) : la meme
     * forme qu'un @ManyToMany, mais sans table "role" a maintenir et sans risque de
     * doublons. (@ManyToMany serait de toute facon impossible : il cible une entite.)
     *
     * EnumType.STRING : indispensable. Par defaut JPA stocke l'ORDINAL (0, 1) ;
     * reordonner l'enum changerait alors le sens de toutes les donnees deja en base.
     *
     * FetchType.EAGER : Spring Security lit les roles APRES la fin de la transaction
     * de loadUserByUsername(). En LAZY -> LazyInitializationException.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private List<Role> roles = new ArrayList<>();

    /** Constructeur sans argument obligatoire pour JPA. */
    public User() {}

    public User(String username, String password, List<Role> roles) {
        this.username = username;
        this.password = password;
        // copie mutable : Hibernate doit pouvoir gerer la collection lui-meme
        this.roles = new ArrayList<>(roles);
    }

    public long getId() {
        return id;
    }

    public List<Role> getRoles() {
        return roles;
    }

    /** Les roles servent directement d'autorites : Role implemente GrantedAuthority. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Non geres pour l'instant : on renvoie true (comptes toujours valides).

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
