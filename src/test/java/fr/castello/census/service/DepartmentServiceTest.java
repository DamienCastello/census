package fr.castello.census.service;

import fr.castello.census.CensusApplication;
import fr.castello.census.dto.DepartmentDto;
import fr.castello.census.exception.FunctionalException;
import fr.castello.census.exception.NotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CensusApplication.class)
@ActiveProfiles("test")
@Transactional
public class DepartmentServiceTest {

    @Autowired
    private DepartmentService departmentService;

    @Test
    void testExtractAll() {
        List<DepartmentDto> departments = departmentService.extractAll();

        assertEquals(100, departments.size());
    }

    @Test
    void testExtractById() throws Exception {
        DepartmentDto department = departmentService.extractById(1L);

        assertEquals("34", department.code());
        assertEquals(11, department.cities().size());
    }

    @Test
    void testExtractById_failIfUnknown() {
        assertThrows(NotFoundException.class,
                () -> departmentService.extractById(999_999L));
    }

    @Test
    void testCreateDepartment() throws Exception {
        DepartmentDto created = departmentService.createDepartment(
                new DepartmentDto(null, "999", "Test", null));

        assertNotNull(created.id());
        assertEquals("999", created.code());
        assertEquals("Test", created.name());
    }

    @Test
    void testCreateDepartment_failIfCodeAlreadyExists() {
        // le code 34 existe déjà
        DepartmentDto doublon = new DepartmentDto(null, "34", "Hérault", null);

        FunctionalException e = assertThrows(FunctionalException.class,
                () -> departmentService.createDepartment(doublon));

        assertEquals("Le département existe déjà", e.getMessage());
    }

    @Test
    void testCreateDepartment_failIfNameTooShort() {
        DepartmentDto invalide = new DepartmentDto(null, "999", "X", null);

        FunctionalException e = assertThrows(FunctionalException.class,
                () -> departmentService.createDepartment(invalide));

        assertEquals("Le nom du département doit contenir au moins 2 caractères", e.getMessage());
    }

    @Test
    void testUpdateDepartment() throws Exception {
        DepartmentDto result = departmentService.updateDepartment(
                1L, new DepartmentDto(null, "34", "Hérault", null));

        assertEquals("Hérault", result.name());
        assertEquals("Hérault", departmentService.extractById(1L).name());
    }

    @Test
    void testUpdateDepartment_failIfUnknown() {
        DepartmentDto dto = new DepartmentDto(null, "999", "Test", null);

        assertThrows(NotFoundException.class,
                () -> departmentService.updateDepartment(999_999L, dto));
    }

    @Test
    void testDeleteDepartment() throws Exception {
        // on crée un département vide, donc supprimable
        DepartmentDto created = departmentService.createDepartment(
                new DepartmentDto(null, "999", "Test", null));

        departmentService.deleteDepartment(created.id());

        assertThrows(NotFoundException.class,
                () -> departmentService.extractById(created.id()));
    }

    @Test
    void testDeleteDepartment_failIfHasCities() {
        // le département 1 possède 11 villes
        FunctionalException e = assertThrows(FunctionalException.class,
                () -> departmentService.deleteDepartment(1L));

        assertEquals("Impossible de supprimer un département qui possède des villes", e.getMessage());
    }

    @Test
    void testAssignCity() throws Exception {
        // on déplace Paris (13321, département 37) vers le département 1
        DepartmentDto result = departmentService.assignCity(1L, 13321L);

        assertTrue(result.cities().stream()
                .anyMatch(city -> city.name().equals("Paris")));
    }

    @Test
    void testAssignCity_failIfUnknownCity() {
        assertThrows(NotFoundException.class,
                () -> departmentService.assignCity(1L, 999_999L));
    }
}