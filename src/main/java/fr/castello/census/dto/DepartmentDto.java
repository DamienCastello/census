package fr.castello.census.dto;

import java.util.List;

/**
 * Représentation d'un département exposée par l'API.
 *
 * <p>Embarque la liste de ses villes sous forme de {@link CityDto}. Comme
 * {@code CityDto} ne référence son département que par un identifiant, aucune
 * boucle de sérialisation n'est possible (c'est l'apport des DTO par rapport à
 * {@code @JsonIgnore}).</p>
 *
 * @param id     identifiant du département (ignoré en création, positionné en sortie)
 * @param code   code du département
 * @param nom    nom du département
 * @param cities villes rattachées (ignoré en entrée, renseigné en sortie)
 */
public record DepartmentDto(Long id, String code, String nom, List<CityDto> cities) {
}
