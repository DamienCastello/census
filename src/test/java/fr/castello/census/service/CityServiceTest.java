package fr.castello.census.service;

import fr.castello.census.CensusApplication;
import fr.castello.census.dto.CityDto;
import fr.castello.census.dto.PageDto;
import fr.castello.census.exception.FunctionalException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CensusApplication.class)
@ActiveProfiles("test")
@Transactional
public class CityServiceTest {

    @Autowired
    private CityService cityService;

    @Test
    void testExtractAll_returnFirstPage() {
        PageDto<CityDto> page = cityService.extractAll(
                PageRequest.of(0, 20, Sort.by("population").descending()));

        assertEquals(20, page.content().size());
        assertEquals(1001, page.totalElements());
        assertEquals(51, page.totalPages());
        assertEquals("Paris", page.content().get(0).name());   // la plus peuplée
    }

    @Test
    void testCreateCity_refuseIfAlreadyExistsInDepartment() {
        // Paris existe déjà dans le département 37
        CityDto doublon = new CityDto(null, "Paris", 50000, 37L, null);

        FunctionalException e = assertThrows(FunctionalException.class,
                () -> cityService.createCity(doublon));

        assertEquals("La ville existe déjà dans ce département", e.getMessage());
    }

    @Test
    void testCreateCity() throws Exception {
        CityDto created = cityService.createCity(
                new CityDto(null, "Testville", 5000, 37L, null));

        assertNotNull(created.id());          // l'id a été généré
        assertEquals("Testville", created.name());
        assertEquals(37L, created.departmentId());
    }
}
