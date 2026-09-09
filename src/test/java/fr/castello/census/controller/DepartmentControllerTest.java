package fr.castello.census.controller;

import fr.castello.census.CensusApplication;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  TESTS AVEC LA BASE H2  —  application complète
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * <p>Rien n'est simulé : {@code @SpringBootTest} démarre toute l'application et
 * {@code @AutoConfigureMockMvc} fournit un {@link MockMvc} qui envoie de vraies requêtes
 * HTTP au contrôleur. Celui-ci appelle le vrai service, qui interroge la vraie base
 * H2 remplie par {@code data-test.sql} (100 départements, 1001 villes).</p>
 *
 * <p>On teste donc <strong>toute la chaîne</strong> : URL → contrôleur → service →
 * repository → base. C'est plus lent (Spring démarre) mais cela valide l'assemblage
 * réel des couches, ce qu'un test avec mocks ne peut pas faire.</p>
 *
 * <p>{@code @Transactional} annule les écritures après chaque test, pour qu'ils
 * n'interfèrent pas entre eux.</p>
 *
 * <p>Voir {@code CityControllerTest} pour l'approche inverse, avec le service simulé.</p>
 */
@SpringBootTest(classes = CensusApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DepartmentControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    /*
     * springSecurity() branche la chaine de filtres ET fait lire a MockMvc le
     * SecurityContext pose par @WithMockUser. Spring Boot 4 ne l'applique plus
     * automatiquement : sans cet appel, tous les tests recoivent un 401.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    /** Sans @WithMockUser, aucun utilisateur n'est authentifié : le filtre renvoie 401. */
    @Test
    void testGetAll_returns401IfNotAuthenticated() throws Exception {
        mockMvc.perform(get("/departments"))
                .andExpect(status().isUnauthorized());
    }

    /** Authentifié mais sans le rôle exigé par le @Secured de la méthode : 403. */
    @Test
    @WithMockUser(roles = "USER")
    void testCreateDepartment_returns403IfNotAdmin() throws Exception {
        mockMvc.perform(post("/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"999\",\"name\":\"Test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAll() throws Exception {
        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(100));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetDepartment() throws Exception {
        mockMvc.perform(get("/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("34"))
                .andExpect(jsonPath("$.cities.length()").value(11));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetDepartment_returns404IfUnknown() throws Exception {
        mockMvc.perform(get("/departments/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Département non trouvé"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateDepartment() throws Exception {
        mockMvc.perform(post("/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"999\",\"name\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("999"))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateDepartment_returns400IfCodeAlreadyExists() throws Exception {
        // le code 34 existe déjà dans data-test.sql
        mockMvc.perform(post("/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"34\",\"name\":\"Hérault\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Le département existe déjà"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateDepartment_returns400IfNameTooShort() throws Exception {
        mockMvc.perform(post("/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"999\",\"name\":\"X\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Le nom du département doit contenir au moins 2 caractères"));
    }
}
