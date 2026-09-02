package fr.castello.census.dto;

/**
 * Représentation d'une ville exposée par l'API.
 *
 * <p>Découple le contrat HTTP de l'entité JPA {@code City} : l'entité ne quitte
 * jamais la couche service. Le département est référencé à plat par son seul
 * identifiant ({@code departmentId}), ce qui évite tout cycle de sérialisation
 * lorsque {@code DepartmentDto} embarque la liste de ses villes.</p>
 *
 * @param id           identifiant de la ville (ignoré en création, positionné en sortie)
 * @param name         nom de la ville
 * @param population   nombre d'habitants
 * @param departmentId identifiant du département de rattachement (obligatoire en création)
 */
public record CityDto(Long id, String name, int population, Long departmentId) {
}
