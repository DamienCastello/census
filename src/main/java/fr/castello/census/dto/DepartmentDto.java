package fr.castello.census.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
 * @param name   nom du département (peut être absent sur les données importées)
 * @param cities villes rattachées (ignoré en entrée, renseigné en sortie)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DepartmentDto(
        Long id,
        String code,
        // @JsonAlias : accepte aussi "nom" EN ENTREE, pour lire l'API geo.api.gouv.fr
        // sans classe supplementaire. La sortie JSON reste "name" (contrairement a
        // @JsonProperty, qui renommerait aussi le champ expose par notre API).
        @JsonAlias("nom") String name,
        List<CityDto> cities) {
}
