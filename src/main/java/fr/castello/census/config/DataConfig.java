package fr.castello.census.config;

import fr.castello.census.service.DepartmentService;
import fr.castello.census.service.JpaUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Amorce les données au démarrage de l'application.
 *
 * <p>{@link CommandLineRunner} : Spring exécute {@link #run(String...)} une fois le
 * contexte entièrement démarré — donc <strong>après</strong> la création des tables et
 * le chargement de {@code data.sql}. C'est indispensable ici : avec un
 * {@code @PostConstruct}, les tables seraient encore vides.</p>
 *
 * <p>Séparer le déclencheur (cette classe) de la logique (les services) a un second
 * avantage : l'appel {@code service.initData()} vient d'un <em>autre</em> bean, il passe
 * donc par le proxy Spring et le {@code @Transactional} des services s'applique
 * réellement — ce qui ne serait pas le cas d'un appel interne à la même classe.</p>
 */
@Component
public class DataConfig implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataConfig.class);

    /** Active ou non l'amorçage (voir application.properties). */
    @Value("${application.init}")
    private boolean initData;

    private final DepartmentService departmentService;
    private final JpaUserDetailsService userDetailsService;

    public DataConfig(DepartmentService departmentService, JpaUserDetailsService userDetailsService) {
        this.departmentService = departmentService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void run(String... args) {
        if (!initData) {
            log.info("Amorçage des données désactivé (application.init=false).");
            return;
        }

        departmentService.initData();   // noms des départements, via l'API geo.api.gouv.fr
        userDetailsService.initData();  // utilisateurs de démonstration
    }
}
