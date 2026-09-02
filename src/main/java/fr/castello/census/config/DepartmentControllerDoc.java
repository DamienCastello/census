package fr.castello.census.config;

import fr.castello.census.dto.DepartmentDto;
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

public interface DepartmentControllerDoc {

    /**
     * Retourne la liste de tous les départements.
     *
     * @return Liste de DepartmentDto
     */
    @Operation(summary = "Retourne la liste de tous les départements")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des départements au format JSON",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = DepartmentDto.class)
                            )
                    )
            )
    })
    List<DepartmentDto> getAll();


    /**
     * Retourne un département à partir de son identifiant.
     *
     * @param id Identifiant du département
     * @return Département correspondant
     */
    @Operation(summary = "Retourne un département à partir de son identifiant")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Département au format JSON",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Département non trouvé",
                    content = @Content()
            )
    })
    DepartmentDto getDepartment(
            @Parameter(
                    description = "Identifiant du département à récupérer",
                    example = "1",
                    required = true
            )
            Long id
    ) throws FunctionalException;


    /**
     * Crée un nouveau département.
     *
     * @param department Département à créer
     * @return Le département créé
     */
    @Operation(summary = "Crée un nouveau département")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Département créé avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données du département invalides",
                    content = @Content()
            )
    })
    ResponseEntity<DepartmentDto> createDepartment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Informations du département à créer",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDto.class)
                    )
            )
            DepartmentDto department
    ) throws FunctionalException;


    /**
     * Modifie un département existant.
     *
     * @param id         Identifiant du département
     * @param department Nouvelles informations du département
     * @return Le département modifié
     */
    @Operation(summary = "Modifie un département existant")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Département modifié avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données du département invalides",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Département non trouvé",
                    content = @Content()
            )
    })
    DepartmentDto updateDepartment(
            @Parameter(
                    description = "Identifiant du département à modifier",
                    example = "1",
                    required = true
            )
            Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nouvelles informations du département",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDto.class)
                    )
            )
            DepartmentDto department
    ) throws FunctionalException;


    /**
     * Supprime un département.
     *
     * @param id Identifiant du département
     * @return Réponse sans contenu
     */
    @Operation(summary = "Supprime un département")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Département supprimé avec succès",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Le département possède encore des villes",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Département non trouvé",
                    content = @Content()
            )
    })
    ResponseEntity<Void> deleteDepartment(
            @Parameter(
                    description = "Identifiant du département à supprimer",
                    example = "1",
                    required = true
            )
            Long id
    ) throws FunctionalException;


    /**
     * Rattache une ville existante à un département existant.
     *
     * @param departmentId Identifiant du département cible
     * @param cityId       Identifiant de la ville à rattacher
     * @return Le département cible, villes incluses
     */
    @Operation(summary = "Rattache une ville existante à un département")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ville rattachée ; département au format JSON",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Département ou ville non trouvé",
                    content = @Content()
            )
    })
    DepartmentDto assignCity(
            @Parameter(
                    description = "Identifiant du département cible",
                    example = "1",
                    required = true
            )
            Long departmentId,

            @Parameter(
                    description = "Identifiant de la ville à rattacher",
                    example = "2",
                    required = true
            )
            Long cityId
    ) throws FunctionalException;
}
