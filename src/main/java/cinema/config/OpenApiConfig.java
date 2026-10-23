package cinema.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SpringDocConfiguration;
import org.springdoc.core.SpringDocUIConfiguration;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springdoc.core.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
// Plain Spring MVC does not load springdoc Boot autoconfiguration automatically.
@Import({SpringDocConfiguration.class, SpringDocConfigProperties.class,
        SpringDocWebMvcConfiguration.class, SpringDocUIConfiguration.class,
        SwaggerConfig.class, SwaggerUiConfigProperties.class, SwaggerUiOAuthProperties.class})
public class OpenApiConfig {
    private static final String BASIC_AUTH_SCHEME = "basicAuth";

    @Bean
    public OpenAPI cinemaOpenApi() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(BASIC_AUTH_SCHEME,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH_SCHEME));
    }
}
