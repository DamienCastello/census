package fr.castello.census.service;

import fr.castello.census.dao.CityDao;
import fr.castello.census.dao.DepartmentDao;
import fr.castello.census.dto.CityDto;
import fr.castello.census.entity.City;
import fr.castello.census.entity.Department;
import fr.castello.census.exception.FunctionalException;
import fr.castello.census.exception.NotFoundException;
import fr.castello.census.mapper.CityMapper;
import fr.castello.census.util.CsvUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CityService {

    private final CityDao cityDao;
    private final DepartmentDao departmentDao;
    private final CityMapper cityMapper;

    public CityService(CityDao cityDao, DepartmentDao departmentDao, CityMapper cityMapper) {
        this.cityDao = cityDao;
        this.departmentDao = departmentDao;
        this.cityMapper = cityMapper;
    }

    /**
     * Insère un jeu de données de départ en base (un département et ses villes).
     */
    @Transactional
    public void initData() {
        Department dep = departmentDao.create(new Department("13", "Bouches-du-Rhône"));

        City totocity = new City("Totocity", 10);
        City arles = new City("Arles", 55000);
        dep.addCity(totocity);
        dep.addCity(arles);

        cityDao.create(totocity);
        cityDao.create(arles);
    }

    /**
     * Retourne toutes les villes présentes en base.
     *
     * @return la liste des villes
     */
    @Transactional(readOnly = true)
    public List<CityDto> extractAll() {
        return cityMapper.toDtoList(cityDao.extractAll());
    }

    /**
     * Retourne la ville correspondant à l'identifiant.
     *
     * @param id identifiant de la ville
     * @return la ville trouvée
     * @throws NotFoundException si aucune ville ne correspond
     */
    @Transactional(readOnly = true)
    public CityDto extractById(Long id) throws NotFoundException {
        City city = cityDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Ville non trouvée"));
        return cityMapper.toDto(city);
    }

    /**
     * Retourne les villes dont le nom commence par le préfixe donné.
     *
     * @param prefix début du nom recherché
     * @return la liste des villes correspondantes (éventuellement vide)
     * @throws FunctionalException si le préfixe est absent
     */
    @Transactional(readOnly = true)
    public List<CityDto> extractByNameStartingWith(String prefix) throws FunctionalException {
        if (prefix == null || prefix.isBlank()) {
            throw new FunctionalException("Le préfixe de recherche est obligatoire");
        }
        return cityMapper.toDtoList(cityDao.findByNameStartingWith(prefix));
    }

    /**
     * Retourne les villes dont la population est strictement supérieure à {@code min}.
     *
     * @param min population minimale (exclue)
     * @return la liste des villes correspondantes (éventuellement vide)
     * @throws FunctionalException si {@code min} est négatif
     */
    @Transactional(readOnly = true)
    public List<CityDto> extractByPopulationGreaterThan(int min) throws FunctionalException {
        if (min < 0) {
            throw new FunctionalException("La population ne peut pas être négative");
        }
        return cityMapper.toDtoList(cityDao.findByPopulationGreaterThan(min));
    }

    /**
     * Retourne les villes dont la population est comprise entre {@code min} et {@code max} (bornes incluses).
     *
     * @param min population minimale
     * @param max population maximale
     * @return la liste des villes correspondantes (éventuellement vide)
     * @throws FunctionalException si {@code min} est supérieur à {@code max}
     */
    @Transactional(readOnly = true)
    public List<CityDto> extractByPopulationBetween(int min, int max) throws FunctionalException {
        if (min > max) {
            throw new FunctionalException("La population minimale doit être inférieure ou égale à la maximale");
        }
        return cityMapper.toDtoList(cityDao.findByPopulationBetween(min, max));
    }

    /**
     * Crée une nouvelle ville après contrôles métier, rattachée à un département existant.
     *
     * @param dto données de la ville à créer
     * @return la ville créée (avec son identifiant généré)
     * @throws FunctionalException si les données sont invalides, si la ville existe déjà
     *                             ou si le département référencé n'existe pas
     */
    @Transactional
    public CityDto createCity(CityDto dto) throws FunctionalException {
        validateCity(dto);

        if (cityDao.existsByName(dto.name())) {
            throw new FunctionalException("La ville existe déjà");
        }

        Department department = resolveOrCreateDepartment(dto.departmentId(), dto.departmentCode());
        City city = cityMapper.toEntity(dto);
        department.addCity(city);

        City created = cityDao.create(city);
        return cityMapper.toDto(created);
    }

    /**
     * Modifie une ville existante (nom, population et département de rattachement).
     *
     * @param id  identifiant de la ville à modifier
     * @param dto nouvelles données de la ville
     * @return la ville modifiée
     * @throws NotFoundException   si aucune ville ne correspond à l'identifiant
     * @throws FunctionalException si les données sont invalides, si le nom est déjà pris
     *                             ou si le département référencé n'existe pas
     */
    @Transactional
    public CityDto updateCity(Long id, CityDto dto) throws FunctionalException {
        validateCity(dto);

        City existing = cityDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Ville non trouvée"));

        // Le nom doit rester unique, sauf s'il s'agit du nom actuel de la ville modifiée.
        if (!existing.getName().equals(dto.name()) && cityDao.existsByName(dto.name())) {
            throw new FunctionalException("La ville existe déjà");
        }

        Department department = resolveOrCreateDepartment(dto.departmentId(), dto.departmentCode());
        existing.setName(dto.name());
        existing.setPopulation(dto.population());
        reassignDepartment(existing, department);

        // Entité gérée : le dirty checking JPA persiste les changements au commit.
        return cityMapper.toDto(existing);
    }

    /**
     * Supprime la ville correspondant à l'identifiant.
     *
     * @param id identifiant de la ville à supprimer
     * @throws NotFoundException si aucune ville ne correspond
     */
    @Transactional
    public void deleteCity(Long id) throws NotFoundException {
        City existing = cityDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Ville non trouvée"));
        cityDao.delete(existing);
    }

    /**
     * Produit l'export CSV de toutes les villes.
     *
     * <p>Le service génère uniquement le <em>contenu</em> : le transport (en-têtes HTTP,
     * nom du fichier, encodage) relève de la couche web.</p>
     *
     * @return le contenu CSV : une ligne d'en-tête puis une ligne par ville
     */
    @Transactional(readOnly = true)
    public String exportCsv() {
        StringBuilder csv = new StringBuilder(
                CsvUtils.line("id", "name", "population", "departmentCode"));

        for (CityDto city : cityMapper.toDtoList(cityDao.extractAll())) {
            csv.append(CsvUtils.line(city.id(), city.name(), city.population(), city.departmentCode()));
        }
        return csv.toString();
    }

    /**
     * Contrôles métier communs à la création et à la modification d'une ville.
     *
     * @param city ville à valider
     * @throws FunctionalException si le nom ou la population sont invalides
     */
    private void validateCity(CityDto city) throws FunctionalException {
        if (city.name() == null || city.name().isBlank() || city.name().length() < 2) {
            throw new FunctionalException("Le nom de la ville doit contenir au moins 2 caractères");
        }
        if (city.population() <= 1) {
            throw new FunctionalException("La population doit être supérieure à 1");
        }
    }

    /**
     * Résout le département de rattachement (obligatoire) à partir de son id et/ou de son code.
     *
     * <p>Règles : on tente d'abord la résolution par identifiant, puis par code. Si le
     * département reste introuvable mais qu'un code est fourni, il est <strong>créé</strong> ;
     * sinon une exception « Département inconnu » est levée.</p>
     *
     * @param departmentId   identifiant du département (peut être {@code null})
     * @param departmentCode code du département (peut être {@code null})
     * @return le département géré correspondant (existant ou nouvellement créé)
     * @throws FunctionalException si le département est introuvable et qu'aucun code n'est fourni
     */
    private Department resolveOrCreateDepartment(Long departmentId, String departmentCode) throws FunctionalException {
        if (departmentId != null) {
            Optional<Department> byId = departmentDao.findById(departmentId);
            if (byId.isPresent()) {
                return byId.get();
            }
        }
        if (departmentCode != null && !departmentCode.isBlank()) {
            return departmentDao.findByCode(departmentCode)
                    .orElseGet(() -> departmentDao.create(new Department(departmentCode, departmentCode)));
        }
        throw new FunctionalException("Département inconnu");
    }

    /**
     * Retourne les {@code count} plus grandes villes (par population) d'un département.
     *
     * @param departmentId identifiant du département
     * @param count        nombre maximum de villes à retourner
     * @return la liste des villes correspondantes, de la plus peuplée à la moins peuplée
     * @throws FunctionalException si {@code count} n'est pas positif
     * @throws NotFoundException   si le département n'existe pas
     */
    @Transactional(readOnly = true)
    public List<CityDto> extractLargestByDepartment(Long departmentId, int count) throws FunctionalException {
        if (count <= 0) {
            throw new FunctionalException("Le nombre de villes demandé doit être supérieur à 0");
        }
        ensureDepartmentExists(departmentId);
        return cityMapper.toDtoList(cityDao.findLargestByDepartment(departmentId, count));
    }

    /**
     * Retourne les villes d'un département dont la population est comprise entre {@code min} et
     * {@code max} (bornes incluses).
     *
     * @param departmentId identifiant du département
     * @param min          population minimale
     * @param max          population maximale
     * @return la liste des villes correspondantes (éventuellement vide)
     * @throws FunctionalException si {@code min} est supérieur à {@code max}
     * @throws NotFoundException   si le département n'existe pas
     */
    @Transactional(readOnly = true)
    public List<CityDto> extractByPopulationBetweenInDepartment(Long departmentId, int min, int max)
            throws FunctionalException {
        if (min > max) {
            throw new FunctionalException("La population minimale doit être inférieure ou égale à la maximale");
        }
        ensureDepartmentExists(departmentId);
        return cityMapper.toDtoList(cityDao.findByPopulationBetweenAndDepartment(departmentId, min, max));
    }

    /**
     * Vérifie l'existence d'un département.
     *
     * @param departmentId identifiant du département
     * @throws NotFoundException si aucun département ne correspond
     */
    private void ensureDepartmentExists(Long departmentId) throws NotFoundException {
        if (departmentId == null || departmentDao.findById(departmentId).isEmpty()) {
            throw new NotFoundException("Département non trouvé");
        }
    }

    /**
     * Rattache la ville au département cible en maintenant la cohérence des deux côtés.
     *
     * @param city   ville à (re)rattacher
     * @param target département cible
     */
    private void reassignDepartment(City city, Department target) {
        Department current = city.getDepartment();
        if (current != null && current.getId() == target.getId()) {
            return;
        }
        if (current != null) {
            current.getCities().remove(city);
        }
        target.addCity(city);
    }
}
