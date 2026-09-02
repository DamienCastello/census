package fr.castello.census.service;

import fr.castello.census.dao.CityDao;
import fr.castello.census.dao.DepartmentDao;
import fr.castello.census.dto.DepartmentDto;
import fr.castello.census.entity.City;
import fr.castello.census.entity.Department;
import fr.castello.census.exception.FunctionalException;
import fr.castello.census.exception.NotFoundException;
import fr.castello.census.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentDao departmentDao;
    private final CityDao cityDao;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentDao departmentDao, CityDao cityDao, DepartmentMapper departmentMapper) {
        this.departmentDao = departmentDao;
        this.cityDao = cityDao;
        this.departmentMapper = departmentMapper;
    }

    /**
     * Retourne tous les départements présents en base.
     *
     * @return la liste des départements
     */
    @Transactional(readOnly = true)
    public List<DepartmentDto> extractAll() {
        return departmentMapper.toDtoList(departmentDao.extractAll());
    }

    /**
     * Retourne le département correspondant à l'identifiant.
     *
     * @param id identifiant du département
     * @return le département trouvé
     * @throws NotFoundException si aucun département ne correspond
     */
    @Transactional(readOnly = true)
    public DepartmentDto extractById(Long id) throws NotFoundException {
        Department department = departmentDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Département non trouvé"));
        return departmentMapper.toDto(department);
    }

    /**
     * Crée un nouveau département après contrôles métier.
     *
     * @param dto données du département à créer
     * @return le département créé (avec son identifiant généré)
     * @throws FunctionalException si les données sont invalides ou si le code est déjà pris
     */
    @Transactional
    public DepartmentDto createDepartment(DepartmentDto dto) throws FunctionalException {
        validateDepartment(dto);

        if (departmentDao.existsByCode(dto.code())) {
            throw new FunctionalException("Le département existe déjà");
        }

        Department created = departmentDao.create(departmentMapper.toEntity(dto));
        return departmentMapper.toDto(created);
    }

    /**
     * Modifie un département existant.
     *
     * @param id  identifiant du département à modifier
     * @param dto nouvelles données du département
     * @return le département modifié
     * @throws NotFoundException   si aucun département ne correspond à l'identifiant
     * @throws FunctionalException si les données sont invalides ou si le code est déjà pris
     */
    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentDto dto) throws FunctionalException {
        validateDepartment(dto);

        Department existing = departmentDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Département non trouvé"));

        // Le code doit rester unique, sauf s'il s'agit du code actuel du département modifié.
        if (!existing.getCode().equals(dto.code()) && departmentDao.existsByCode(dto.code())) {
            throw new FunctionalException("Le département existe déjà");
        }

        existing.setCode(dto.code());
        existing.setNom(dto.nom());
        return departmentMapper.toDto(existing);
    }

    /**
     * Supprime le département correspondant à l'identifiant.
     *
     * <p>Refusé si le département possède encore des villes (le rattachement est obligatoire).</p>
     *
     * @param id identifiant du département à supprimer
     * @throws NotFoundException   si aucun département ne correspond
     * @throws FunctionalException si le département possède encore des villes
     */
    @Transactional
    public void deleteDepartment(Long id) throws FunctionalException {
        Department existing = departmentDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Département non trouvé"));

        if (!existing.getCities().isEmpty()) {
            throw new FunctionalException("Impossible de supprimer un département qui possède des villes");
        }

        departmentDao.delete(existing);
    }

    /**
     * Rattache (ou déplace) une ville existante vers un département existant.
     *
     * @param departmentId identifiant du département cible
     * @param cityId       identifiant de la ville à rattacher
     * @return le département cible, villes incluses
     * @throws NotFoundException si le département ou la ville n'existe pas
     */
    @Transactional
    public DepartmentDto assignCity(Long departmentId, Long cityId) throws NotFoundException {
        Department department = departmentDao.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Département non trouvé"));
        City city = cityDao.findById(cityId)
                .orElseThrow(() -> new NotFoundException("Ville non trouvée"));

        Department current = city.getDepartment();
        if (current != null && current.getId() == department.getId()) {
            return departmentMapper.toDto(department);
        }
        if (current != null) {
            current.getCities().remove(city);
        }
        department.addCity(city);

        return departmentMapper.toDto(department);
    }

    /**
     * Contrôles métier communs à la création et à la modification d'un département.
     *
     * @param department département à valider
     * @throws FunctionalException si le code ou le nom sont invalides
     */
    private void validateDepartment(DepartmentDto department) throws FunctionalException {
        if (department.code() == null || department.code().isBlank()) {
            throw new FunctionalException("Le code du département est obligatoire");
        }
        if (department.nom() == null || department.nom().isBlank() || department.nom().length() < 2) {
            throw new FunctionalException("Le nom du département doit contenir au moins 2 caractères");
        }
    }
}
