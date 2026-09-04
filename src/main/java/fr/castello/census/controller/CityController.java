package fr.castello.census.controller;

import fr.castello.census.config.CityControllerDoc;
import fr.castello.census.dto.CityDto;
import fr.castello.census.exception.FunctionalException;
import fr.castello.census.service.CityService;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/cities")
public class CityController implements CityControllerDoc {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostConstruct
    public void initData() {
        cityService.initData();
    }

    @GetMapping(params = {"!name", "!population", "!minPop", "!maxPop"})
    public List<CityDto> getAll() {
        return cityService.extractAll();
    }

    @GetMapping("/{id}")
    public CityDto getCity(@PathVariable Long id) throws FunctionalException {
        return cityService.extractById(id);
    }

    @GetMapping(params = {"name", "!population", "!minPop", "!maxPop"})
    public List<CityDto> getCitiesStartWith(@RequestParam String name) throws FunctionalException {
        return cityService.extractByNameStartingWith(name);
    }

    @GetMapping(params = {"population", "!name", "!minPop", "!maxPop"})
    public List<CityDto> getCitiesGreater(@RequestParam int population) throws FunctionalException {
        return cityService.extractByPopulationGreaterThan(population);
    }

    @GetMapping(params = {"minPop", "maxPop", "!population", "!name"})
    public List<CityDto> getCitiesBetween(
            @RequestParam int minPop,
            @RequestParam int maxPop
    ) throws FunctionalException {
        return cityService.extractByPopulationBetween(minPop, maxPop);
    }

    @PostMapping
    public ResponseEntity<CityDto> createCity(@RequestBody CityDto city) throws FunctionalException {
        CityDto created = cityService.createCity(city);
        return ResponseEntity
                .created(URI.create("/cities/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public CityDto updateCity(
            @PathVariable Long id,
            @RequestBody CityDto city
    ) throws FunctionalException {
        return cityService.updateCity(id, city);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) throws FunctionalException {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv() {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("cities.csv").build().toString())
                .body(cityService.exportCsv());
    }
}
