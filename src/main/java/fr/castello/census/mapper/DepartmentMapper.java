package fr.castello.census.mapper;

import fr.castello.census.dto.DepartmentDto;
import fr.castello.census.entity.Department;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Convertit les départements entre l'entité JPA {@link Department} et le DTO {@link DepartmentDto}.
 */
@Component
public class DepartmentMapper {

    private final CityMapper cityMapper;

    public DepartmentMapper(CityMapper cityMapper) {
        this.cityMapper = cityMapper;
    }

    /**
     * Convertit une entité en DTO, villes incluses.
     *
     * <p>À appeler dans une transaction ouverte : l'accès à la collection lazy
     * {@code cities} déclenche son chargement.</p>
     *
     * @param department entité à convertir
     * @return le DTO correspondant
     */
    public DepartmentDto toDto(Department department) {
        return new DepartmentDto(
                department.getId(),
                department.getCode(),
                department.getNom(),
                cityMapper.toDtoList(department.getCities()));
    }

    /**
     * Convertit une liste d'entités en liste de DTO.
     *
     * @param departments entités à convertir
     * @return la liste des DTO correspondants
     */
    public List<DepartmentDto> toDtoList(List<Department> departments) {
        return departments.stream().map(this::toDto).toList();
    }

    /**
     * Construit une nouvelle entité à partir d'un DTO (id et villes ignorés).
     *
     * @param dto DTO source
     * @return une entité non persistée
     */
    public Department toEntity(DepartmentDto dto) {
        return new Department(dto.code(), dto.nom());
    }
}
