package fr.castello.census.mapper;

import fr.castello.census.dto.CityDto;
import fr.castello.census.entity.City;
import fr.castello.census.entity.Department;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Convertit les villes entre l'entité JPA {@link City} et le DTO {@link CityDto}.
 */
@Component
public class CityMapper {

    /**
     * Convertit une entité en DTO.
     *
     * @param city entité à convertir
     * @return le DTO correspondant
     */
    public CityDto toDto(City city) {
        Department department = city.getDepartment();
        Long departmentId = department != null ? department.getId() : null;
        String departmentCode = department != null ? department.getCode() : null;
        return new CityDto(city.getId(), city.getName(), city.getPopulation(), departmentId, departmentCode);
    }

    /**
     * Convertit une liste d'entités en liste de DTO.
     *
     * @param cities entités à convertir
     * @return la liste des DTO correspondants
     */
    public List<CityDto> toDtoList(List<City> cities) {
        return cities.stream().map(this::toDto).toList();
    }

    /**
     * Construit une nouvelle entité à partir d'un DTO (l'identifiant du DTO est ignoré).
     *
     * @param dto DTO source
     * @return une entité non persistée
     */
    public City toEntity(CityDto dto) {
        return new City(dto.name(), dto.population());
    }
}
