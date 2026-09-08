package fr.castello.census.service;

import fr.castello.census.dto.CityDto;
import fr.castello.census.dto.PageDto;
import fr.castello.census.entity.City;
import fr.castello.census.entity.Department;
import fr.castello.census.exception.FunctionalException;
import fr.castello.census.exception.NotFoundException;
import fr.castello.census.mapper.CityMapper;
import fr.castello.census.repository.CityRepository;
import fr.castello.census.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  TESTS AVEC MOCKITO  —  aucune base de données
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * <p>Les repositories et le mapper sont <strong>simulés</strong> : on décide nous-mêmes
 * de ce qu'ils renvoient, avec {@code when(...).thenReturn(...)}. Spring n'est pas
 * démarré et aucune base n'est ouverte — les tests durent quelques millisecondes.</p>
 *
 * <p>On teste la <strong>logique du service isolée</strong> : enchaînement des appels,
 * règles métier, exceptions. Avantage propre à Mockito : on peut prouver qu'une méthode
 * n'a <em>pas</em> été appelée ({@code verify(..., never())}), donc qu'aucune écriture
 * n'a eu lieu en cas d'erreur — impossible à vérifier avec une vraie base.</p>
 *
 * <p>Voir {@code DepartmentServiceTest} pour l'approche inverse, avec une base H2.</p>
 */
@ExtendWith(MockitoExtension.class)
public class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private CityMapper cityMapper;

    /** Construit CityService en lui injectant les trois mocks ci-dessus. */
    @InjectMocks
    private CityService cityService;

    @Test
    void testExtractAll_returnFirstPage() {
        Pageable pageable = PageRequest.of(0, 20);
        City entity = new City("Paris", 2190327);
        CityDto dto = new CityDto(1L, "Paris", 2190327, 37L, "75");
        Page<City> page = new PageImpl<>(List.of(entity), pageable, 1);

        when(cityRepository.findAll(pageable)).thenReturn(page);
        when(cityMapper.toDtoList(page.getContent())).thenReturn(List.of(dto));

        PageDto<CityDto> result = cityService.extractAll(pageable);

        assertEquals(1, result.content().size());
        assertEquals("Paris", result.content().get(0).name());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(1, result.totalElements());
    }

    @Test
    void testExtractById() throws Exception {
        City entity = new City("Paris", 2190327);
        CityDto dto = new CityDto(1L, "Paris", 2190327, 37L, "75");

        when(cityRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(cityMapper.toDto(entity)).thenReturn(dto);

        CityDto result = cityService.extractById(1L);

        assertEquals("Paris", result.name());
        verify(cityRepository).findById(1L);
    }

    @Test
    void testExtractById_failIfUnknown() {
        when(cityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cityService.extractById(999L));
    }

    @Test
    void testCreateCity() throws Exception {
        CityDto input = new CityDto(null, "Testville", 5000, 37L, null);
        Department department = new Department("75", "Paris");
        City entity = new City("Testville", 5000);
        CityDto saved = new CityDto(14322L, "Testville", 5000, 37L, "75");

        when(departmentRepository.findById(37L)).thenReturn(Optional.of(department));
        when(cityRepository.existsByNameAndDepartmentId(anyString(), anyLong())).thenReturn(false);
        when(cityMapper.toEntity(input)).thenReturn(entity);
        when(cityRepository.save(entity)).thenReturn(entity);
        when(cityMapper.toDto(entity)).thenReturn(saved);

        CityDto result = cityService.createCity(input);

        assertEquals(14322L, result.id());
        assertEquals("Testville", result.name());
        verify(cityRepository).save(entity);
        // la ville a bien été rattachée au département
        assertEquals(department, entity.getDepartment());
    }

    @Test
    void testCreateCity_refuseIfAlreadyExistsInDepartment() {
        CityDto doublon = new CityDto(null, "Paris", 50000, 37L, null);

        when(departmentRepository.findById(37L)).thenReturn(Optional.of(new Department("75", "Paris")));
        when(cityRepository.existsByNameAndDepartmentId(anyString(), anyLong())).thenReturn(true);

        FunctionalException e = assertThrows(FunctionalException.class,
                () -> cityService.createCity(doublon));

        assertEquals("La ville existe déjà dans ce département", e.getMessage());
        verify(cityRepository, never()).save(any());   // rien n'a été écrit
    }

    @Test
    void testCreateCity_failIfNameTooShort() {
        CityDto invalide = new CityDto(null, "X", 5000, 37L, null);

        FunctionalException e = assertThrows(FunctionalException.class,
                () -> cityService.createCity(invalide));

        assertEquals("Le nom de la ville doit contenir au moins 2 caractères", e.getMessage());
        // la validation échoue avant tout accès aux données
        verifyNoInteractions(cityRepository, departmentRepository);
    }

    @Test
    void testCreateCity_failIfPopulationTooLow() {
        CityDto invalide = new CityDto(null, "Testville", 1, 37L, null);

        FunctionalException e = assertThrows(FunctionalException.class,
                () -> cityService.createCity(invalide));

        assertEquals("La population doit être supérieure à 1", e.getMessage());
    }

    @Test
    void testUpdateCity_modifyCity() throws Exception {
        Department department = new Department("75", "Paris");
        City existing = new City("Paris", 2190327);
        department.addCity(existing);

        CityDto input = new CityDto(null, "Paris", 2_200_000, 37L, null);
        CityDto updated = new CityDto(13321L, "Paris", 2_200_000, 37L, "75");

        when(cityRepository.findById(13321L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findById(37L)).thenReturn(Optional.of(department));
        when(cityMapper.toDto(existing)).thenReturn(updated);

        CityDto result = cityService.updateCity(13321L, input);

        assertEquals(2_200_000, result.population());
        // l'entité elle-même a bien été modifiée (c'est le dirty checking qui persiste en prod)
        assertEquals(2_200_000, existing.getPopulation());
    }

    @Test
    void testUpdateCity_failIfUnknownCity() {
        CityDto dto = new CityDto(null, "Testville", 5000, 37L, null);

        when(cityRepository.findById(999_999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> cityService.updateCity(999_999L, dto));
    }

    @Test
    void testDeleteCity() throws Exception {
        City existing = new City("Paris", 2190327);
        when(cityRepository.findById(13321L)).thenReturn(Optional.of(existing));

        cityService.deleteCity(13321L);

        verify(cityRepository).delete(existing);
    }

    @Test
    void testDeleteCity_failIfUnknownCity() {
        when(cityRepository.findById(999_999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cityService.deleteCity(999_999L));
        verify(cityRepository, never()).delete(any());
    }
}
