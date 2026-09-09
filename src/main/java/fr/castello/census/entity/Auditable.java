package fr.castello.census.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Champs de traçabilité communs : qui a modifié, et quand.
 *
 * <p>{@code @MappedSuperclass} : cette classe n'est pas une entité et n'a pas de table.
 * Ses colonnes sont simplement ajoutées à celles de chaque entité qui en hérite — on
 * évite ainsi de dupliquer les deux champs partout.</p>
 *
 * <p>{@code @EntityListeners(AuditingEntityListener.class)} branche le mécanisme
 * d'auditing de Spring Data : les champs sont remplis <strong>automatiquement</strong>
 * à chaque enregistrement, aucun code n'est nécessaire dans les services.</p>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    /** Renseigné par l'AuditorAware déclaré dans AuditConfig (utilisateur connecté). */
    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /** Horodatage de la dernière modification, posé par Spring Data. */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getUpdatedBy() {
        return updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
