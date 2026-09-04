package fr.castello.census.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Page de résultats exposée par l'API.
 *
 * <p>Évite de sérialiser directement le type {@code Page} de Spring Data : le contrat
 * JSON reste stable et indépendant du framework.</p>
 *
 * @param content       éléments de la page courante
 * @param page          numéro de la page (à partir de 0)
 * @param size          taille de page demandée
 * @param totalElements nombre total d'éléments, toutes pages confondues
 * @param totalPages    nombre total de pages
 * @param <T>           type des éléments
 */
public record PageDto<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    /**
     * Construit une page d'API à partir d'une page Spring Data et de son contenu déjà converti.
     *
     * @param source  page technique (pour les métadonnées de pagination)
     * @param content contenu converti en DTO
     * @param <T>     type des éléments exposés
     * @return la page prête à être sérialisée
     */
    public static <T> PageDto<T> of(Page<?> source, List<T> content) {
        return new PageDto<>(
                content,
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages());
    }
}
