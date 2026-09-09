package fr.castello.census.service;

import fr.castello.census.dto.DepartmentDto;
import fr.castello.census.entity.City;
import fr.castello.census.entity.Department;
import fr.castello.census.exception.FunctionalException;
import fr.castello.census.exception.NotFoundException;
import fr.castello.census.mapper.DepartmentMapper;
import fr.castello.census.repository.CityRepository;
import fr.castello.census.repository.DepartmentRepository;
import fr.castello.census.util.CsvUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);
    private static final String GEO_API_URL = "https://geo.api.gouv.fr/departements";

    private final DepartmentRepository departmentRepository;
    private final CityRepository cityRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentRepository departmentRepository,
                             CityRepository cityRepository,
                             DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.cityRepository = cityRepository;
        this.departmentMapper = departmentMapper;
    }

    /**
     * Complète en base les noms de départements absents du jeu de données importé,
     * en les récupérant sur l'API publique geo.api.gouv.fr.
     *
     * <p>Les départements présents dans l'API mais absents de la base sont ignorés
     * (l'API en renvoie 101, la base en contient 100 : Mayotte n'y figure pas).</p>
     *
     * <p>Appelée par {@code DataConfig}, qui décide s'il faut l'exécuter. Comme l'appel
     * vient d'un <em>autre</em> bean, il passe par le proxy Spring et {@code @Transactional}
     * s'applique : les 100 mises à jour forment une seule transaction.</p>
     */
    @Transactional
    public void initData() {
        DepartmentDto[] departments;
        try {
            departments = new RestTemplate().getForObject(GEO_API_URL, DepartmentDto[].class);
        } catch (RestClientException e) {
            log.warn("API {} injoignable, noms de départements non initialisés.", GEO_API_URL);
            return;
        }
        if (departments == null) {
            return;
        }

        int updated = 0;
        for (DepartmentDto dto : departments) {
            // ifPresent : l'API renvoie 101 départements, la base en contient 100
            // (Mayotte absente). Les codes inconnus sont simplement ignorés.
            Optional<Department> found = departmentRepository.findByCode(dto.code());
            if (found.isPresent()) {
                found.get().setName(dto.name());
                departmentRepository.save(found.get());
                updated++;
            }
        }
        log.info("Noms de départements initialisés : {} mis à jour.", updated);
    }

    /**
     * Retourne tous les départements présents en base.
     *
     * @return la liste des départements
     */
    @Transactional(readOnly = true)
    public List<DepartmentDto> extractAll() {
        return departmentMapper.toDtoList(departmentRepository.findAll());
    }

    /**
     * Produit l'export CSV de tous les départements.
     *
     * <p>Le service génère uniquement le <em>contenu</em> : le transport (en-têtes HTTP,
     * nom du fichier, encodage) relève de la couche web.</p>
     *
     * @return le contenu CSV : une ligne d'en-tête puis une ligne par département
     */
    @Transactional(readOnly = true)
    public String exportCsv() {
        StringBuilder csv = new StringBuilder(
                CsvUtils.line("id", "code", "nom", "cityCount"));

        for (DepartmentDto department : departmentMapper.toDtoList(departmentRepository.findAll())) {
            csv.append(CsvUtils.line(
                    department.id(), department.code(), department.name(), department.cities().size()));
        }
        return csv.toString();
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
        Department department = departmentRepository.findById(id)
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

        if (departmentRepository.existsByCode(dto.code())) {
            throw new FunctionalException("Le département existe déjà");
        }

        Department created = departmentRepository.save(departmentMapper.toEntity(dto));
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

        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Département non trouvé"));

        // Le code doit rester unique, sauf s'il s'agit du code actuel du département modifié.
        if (!existing.getCode().equals(dto.code()) && departmentRepository.existsByCode(dto.code())) {
            throw new FunctionalException("Le département existe déjà");
        }

        existing.setCode(dto.code());
        existing.setName(dto.name());
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
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Département non trouvé"));

        if (!existing.getCities().isEmpty()) {
            throw new FunctionalException("Impossible de supprimer un département qui possède des villes");
        }

        departmentRepository.delete(existing);
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
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Département non trouvé"));
        City city = cityRepository.findById(cityId)
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
        if (department.name() == null || department.name().isBlank() || department.name().length() < 2) {
            throw new FunctionalException("Le nom du département doit contenir au moins 2 caractères");
        }
    }
}
