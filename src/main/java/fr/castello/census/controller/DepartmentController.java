package fr.castello.census.controller;

import fr.castello.census.config.DepartmentControllerDoc;
import fr.castello.census.dto.CityDto;
import fr.castello.census.dto.DepartmentDto;
import fr.castello.census.exception.FunctionalException;
import fr.castello.census.service.CityService;
import fr.castello.census.service.DepartmentService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/departments")
public class DepartmentController implements DepartmentControllerDoc {

    private final DepartmentService departmentService;
    private final CityService cityService;

    public DepartmentController(DepartmentService departmentService, CityService cityService) {
        this.departmentService = departmentService;
        this.cityService = cityService;
    }

    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping
    public List<DepartmentDto> getAll() {
        return departmentService.extractAll();
    }

    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping("/{id}")
    public DepartmentDto getDepartment(@PathVariable Long id) throws FunctionalException {
        return departmentService.extractById(id);
    }

    @Secured("ROLE_ADMIN")
    @PostMapping
    public ResponseEntity<DepartmentDto> createDepartment(@RequestBody DepartmentDto department) throws FunctionalException {
        DepartmentDto created = departmentService.createDepartment(department);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    public DepartmentDto updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentDto department
    ) throws FunctionalException {
        return departmentService.updateDepartment(id, department);
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) throws FunctionalException {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{departmentId}/cities/{cityId}")
    public DepartmentDto assignCity(
            @PathVariable Long departmentId,
            @PathVariable Long cityId
    ) throws FunctionalException {
        return departmentService.assignCity(departmentId, cityId);
    }

    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping("/{departmentId}/cities/largest")
    public List<CityDto> getLargestCities(
            @PathVariable Long departmentId,
            @RequestParam int count
    ) throws FunctionalException {
        return cityService.extractLargestByDepartment(departmentId, count);
    }

    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping(value = "/{departmentId}/cities", params = {"minPop", "maxPop"})
    public List<CityDto> getCitiesByPopulation(
            @PathVariable Long departmentId,
            @RequestParam int minPop,
            @RequestParam int maxPop
    ) throws FunctionalException {
        return cityService.extractByPopulationBetweenInDepartment(departmentId, minPop, maxPop);
    }

    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping(value = "/{departmentId}/cities", params = {"population", "!minPop", "!maxPop"})
    public List<CityDto> getCitiesGreaterInDepartment(
            @PathVariable Long departmentId,
            @RequestParam int population
    ) throws FunctionalException {
        return cityService.extractByPopulationGreaterThanInDepartment(departmentId, population);
    }

    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv() {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("departments.csv").build().toString())
                .body(departmentService.exportCsv());
    }
}
