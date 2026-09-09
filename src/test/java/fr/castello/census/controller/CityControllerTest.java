package fr.castello.census.controller;

import fr.castello.census.dto.CityDto;
import fr.castello.census.dto.PageDto;
import fr.castello.census.exception.FunctionalException;
import fr.castello.census.exception.NotFoundException;
import fr.castello.census.security.JwtUtil;
import fr.castello.census.service.CityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  TESTS AVEC MOCKITO  —  aucune base de données
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * <p>{@code @WebMvcTest} ne charge que la couche web (contrôleur, sérialisation JSON,
 * gestionnaire d'exceptions) : ni base, ni JPA. Le service est remplacé par un mock
 * via {@code @MockitoBean} — on décide de ce qu'il renvoie, ou de l'exception qu'il lève.</p>
 *
 * <p>On vérifie donc uniquement ce qui relève du <strong>contrôleur</strong> : l'URL,
 * le code HTTP et la forme du JSON. La logique métier, elle, est couverte par
 * {@code CityServiceTest}. Chaque couche est testée à son niveau.</p>
 *
 * <p>Voir {@code DepartmentControllerTest} pour l'approche inverse, avec une base H2.</p>
 */
@WebMvcTest(CityController.class)
public class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /** Remplace le vrai CityService par un mock dans le contexte Spring. */
    @MockitoBean
    private CityService cityService;

    // Le filtre JWT est un @Component : @WebMvcTest le charge. Ses dependances ne font
    // pas partie de la tranche web, on les simule pour que le contexte demarre.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void testGetCity() throws Exception {
        when(cityService.extractById(1L))
                .thenReturn(new CityDto(1L, "Paris", 2190327, 37L, "75"));

        mockMvc.perform(get("/cities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Paris"))
                .andExpect(jsonPath("$.population").value(2190327))
                .andExpect(jsonPath("$.departmentCode").value("75"));
    }

    @Test
    void testGetCity_returns404IfUnknown() throws Exception {
        when(cityService.extractById(999L))
                .thenThrow(new NotFoundException("Ville non trouvée"));

        mockMvc.perform(get("/cities/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Ville non trouvée"));
    }

    @Test
    void testGetCitiesStartWith() throws Exception {
        when(cityService.extractByNameStartingWith("Mont"))
                .thenReturn(List.of(new CityDto(1L, "Montpellier", 281613, 1L, "34")));

        mockMvc.perform(get("/cities").param("name", "Mont"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Montpellier"));
    }

    @Test
    void testCreateCity() throws Exception {
        when(cityService.createCity(any()))
                .thenReturn(new CityDto(14322L, "Testville", 5000, 37L, "75"));

        mockMvc.perform(post("/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Testville\",\"population\":5000,\"departmentId\":37}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(14322))
                .andExpect(jsonPath("$.name").value("Testville"));
    }

    @Test
    void testCreateCity_returns400IfNameTooShort() throws Exception {
        when(cityService.createCity(any()))
                .thenThrow(new FunctionalException("Le nom de la ville doit contenir au moins 2 caractères"));

        mockMvc.perform(post("/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"population\":5000,\"departmentId\":37}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Le nom de la ville doit contenir au moins 2 caractères"));
    }

    @Test
    void testCreateCity_returns400IfDuplicate() throws Exception {
        when(cityService.createCity(any()))
                .thenThrow(new FunctionalException("La ville existe déjà dans ce département"));

        mockMvc.perform(post("/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Paris\",\"population\":50000,\"departmentId\":37}"))
                .andExpect(status().isBadRequest());
    }
}
