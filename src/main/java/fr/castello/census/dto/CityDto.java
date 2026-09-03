package fr.castello.census.dto;

/**
 * Représentation d'une ville exposée par l'API.
 *
 * <p>Découple le contrat HTTP de l'entité JPA {@code City} : l'entité ne quitte
 * jamais la couche service. Le département est référencé à plat par son
 * identifiant et/ou son code, ce qui évite tout cycle de sérialisation lorsque
 * {@code DepartmentDto} embarque la liste de ses villes.</p>
 *
 * <p>En entrée (POST/PUT), au moins l'un des deux — {@code departmentId} ou
 * {@code departmentCode} — doit être renseigné. En sortie, les deux sont
 * fournis.</p>
 *
 * @param id             identifiant de la ville (ignoré en création, positionné en sortie)
 * @param name           nom de la ville
 * @param population     nombre d'habitants
 * @param departmentId   identifiant du département de rattachement
 * @param departmentCode code du département de rattachement
 */
public record CityDto(Long id, String name, int population, Long departmentId, String departmentCode) {
}
