package fr.castello.census.controller;

import fr.castello.census.config.DepartmentControllerDoc;
import fr.castello.census.dto.CityDto;
import fr.castello.census.dto.DepartmentDto;
import fr.castello.census.exception.FunctionalException;
import fr.castello.census.service.CityService;
import fr.castello.census.service.DepartmentService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @GetMapping
    public List<DepartmentDto> getAll() {
        return departmentService.extractAll();
    }

    @GetMapping("/{id}")
    public DepartmentDto getDepartment(@PathVariable Long id) throws FunctionalException {
        return departmentService.extractById(id);
    }

    @PostMapping
    public ResponseEntity<DepartmentDto> createDepartment(@RequestBody DepartmentDto department) throws FunctionalException {
        DepartmentDto created = departmentService.createDepartment(department);
        return ResponseEntity
                .created(URI.create("/departments/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public DepartmentDto updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentDto department
    ) throws FunctionalException {
        return departmentService.updateDepartment(id, department);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) throws FunctionalException {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{departmentId}/cities/{cityId}")
    public DepartmentDto assignCity(
            @PathVariable Long departmentId,
            @PathVariable Long cityId
    ) throws FunctionalException {
        return departmentService.assignCity(departmentId, cityId);
    }

    @GetMapping("/{departmentId}/cities/largest")
    public List<CityDto> getLargestCities(
            @PathVariable Long departmentId,
            @RequestParam int count
    ) throws FunctionalException {
        return cityService.extractLargestByDepartment(departmentId, count);
    }

    @GetMapping("/{departmentId}/cities")
    public List<CityDto> getCitiesByPopulation(
            @PathVariable Long departmentId,
            @RequestParam int minPop,
            @RequestParam int maxPop
    ) throws FunctionalException {
        return cityService.extractByPopulationBetweenInDepartment(departmentId, minPop, maxPop);
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv() {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("departments.csv").build().toString())
                .body(departmentService.exportCsv());
    }
}
