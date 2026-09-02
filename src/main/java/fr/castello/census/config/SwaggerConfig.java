package fr.castello.census.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Census")
                        .version("1.0")
                        .description("Cette API fournit des données sur les recensements de villes.")
                        .contact(new Contact().name("Gamma").email("shinigamma@gmail.com")));
    }
}