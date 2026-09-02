package fr.castello.census.config;

import fr.castello.census.dto.CityDto;
import fr.castello.census.exception.FunctionalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CityControllerDoc {

    /**
     * Retourne la liste de toutes les villes.
     *
     * @return Liste de CityDto
     */
    @Operation(summary = "Retourne la liste de toutes les villes")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des villes au format JSON",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = CityDto.class)
                            )
                    )
            )
    })
    List<CityDto> getAll();


    /**
     * Retourne une ville correspondant à l'id passé en paramètre.
     *
     * @param id Identifiant de la ville
     * @return Ville correspondante
     */
    @Operation(summary = "Retourne une ville à partir de son identifiant")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ville au format JSON",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CityDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ville non trouvée",
                    content = @Content()
            )
    })
    CityDto getCity(
            @Parameter(
                    description = "Identifiant de la ville à récupérer",
                    example = "34",
                    required = true
            )
            Long id
    ) throws FunctionalException;


    /**
     * Retourne les villes dont le nom commence par la chaîne recherchée.
     *
     * @param name Début du nom de la ville
     * @return Liste des villes correspondantes
     */
    @Operation(summary = "Recherche les villes dont le nom commence par une chaîne")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des villes correspondantes (éventuellement vide)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = CityDto.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Préfixe de recherche absent",
                    content = @Content()
            )
    })
    List<CityDto> getCitiesStartWith(
            @Parameter(
                    description = "Début du nom de la ville",
                    example = "Arl",
                    required = true
            )
            String name
    ) throws FunctionalException;


    /**
     * Retourne les villes ayant une population supérieure à la population donnée.
     *
     * @param population Population minimale
     * @return Liste des villes correspondantes
     */
    @Operation(summary = "Retourne les villes ayant une population supérieure à une valeur")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des villes correspondantes (éventuellement vide)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = CityDto.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La population ne peut pas être négative",
                    content = @Content()
            )
    })
    List<CityDto> getCitiesGreater(
            @Parameter(
                    description = "Population minimale recherchée",
                    example = "50000",
                    required = true
            )
            int population
    ) throws FunctionalException;


    /**
     * Retourne les villes dont la population est comprise entre deux valeurs.
     *
     * @param minPop Population minimale
     * @param maxPop Population maximale
     * @return Liste des villes correspondantes
     */
    @Operation(summary = "Retourne les villes dont la population est comprise entre deux valeurs")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des villes correspondantes (éventuellement vide)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = CityDto.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bornes de population invalides",
                    content = @Content()
            )
    })
    List<CityDto> getCitiesBetween(
            @Parameter(
                    description = "Population minimale",
                    example = "10000",
                    required = true
            )
            int minPop,

            @Parameter(
                    description = "Population maximale",
                    example = "100000",
                    required = true
            )
            int maxPop
    ) throws FunctionalException;


    /**
     * Crée une nouvelle ville.
     *
     * @param city Ville à créer
     * @return La ville créée
     */
    @Operation(summary = "Crée une nouvelle ville")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Ville créée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CityDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de la ville invalides",
                    content = @Content()
            )
    })
    ResponseEntity<CityDto> createCity(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Informations de la ville à créer",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CityDto.class)
                    )
            )
            CityDto city
    ) throws FunctionalException;


    /**
     * Modifie une ville existante.
     *
     * @param id   Identifiant de la ville
     * @param city Nouvelles informations de la ville
     * @return La ville modifiée
     */
    @Operation(summary = "Modifie une ville existante")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ville modifiée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CityDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de la ville invalides",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ville non trouvée",
                    content = @Content()
            )
    })
    CityDto updateCity(
            @Parameter(
                    description = "Identifiant de la ville à modifier",
                    example = "1",
                    required = true
            )
            Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nouvelles informations de la ville",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CityDto.class)
                    )
            )
            CityDto city
    ) throws FunctionalException;


    /**
     * Supprime une ville.
     *
     * @param id Identifiant de la ville
     * @return Réponse sans contenu
     */
    @Operation(summary = "Supprime une ville")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Ville supprimée avec succès",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ville non trouvée",
                    content = @Content()
            )
    })
    ResponseEntity<Void> deleteCity(
            @Parameter(
                    description = "Identifiant de la ville à supprimer",
                    example = "1",
                    required = true
            )
            Long id
    ) throws FunctionalException;
}
