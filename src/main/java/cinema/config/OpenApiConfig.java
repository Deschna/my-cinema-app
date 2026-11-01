package cinema.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SpringDocConfiguration;
import org.springdoc.core.SpringDocUIConfiguration;
import org.springdoc.core.SwaggerUiConfigParameters;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springdoc.core.SwaggerUiOAuthProperties;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.core.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
// Plain Spring MVC does not load springdoc Boot autoconfiguration automatically.
@Import({SpringDocConfiguration.class, SpringDocConfigProperties.class,
        SpringDocWebMvcConfiguration.class, SpringDocUIConfiguration.class,
        SwaggerConfig.class, SwaggerUiOAuthProperties.class})
public class OpenApiConfig {
    private static final String BASIC_AUTH_SCHEME = "basicAuth";
    private static final List<PathItem.HttpMethod> METHOD_ORDER = List.of(
            PathItem.HttpMethod.GET, PathItem.HttpMethod.HEAD, PathItem.HttpMethod.OPTIONS,
            PathItem.HttpMethod.POST, PathItem.HttpMethod.PUT, PathItem.HttpMethod.PATCH,
            PathItem.HttpMethod.DELETE, PathItem.HttpMethod.TRACE);

    @Bean
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();
        properties.setDocExpansion("list");
        properties.setLayout("BaseLayout");
        properties.setDefaultModelsExpandDepth(-1);
        properties.setFilter("true");
        properties.setDeepLinking(true);
        properties.setTryItOutEnabled(true);
        properties.setPersistAuthorization(false);
        properties.setValidatorUrl("none");
        return properties;
    }

    @Bean
    public SwaggerIndexTransformer indexPageTransformer(SwaggerUiConfigProperties properties,
            SwaggerUiOAuthProperties oauthProperties, SwaggerUiConfigParameters parameters,
            SwaggerWelcomeCommon welcome, ObjectMapperProvider objectMapperProvider) {
        return new SwaggerUiTransformer(properties, oauthProperties, parameters,
                welcome, objectMapperProvider);
    }

    @Bean
    public OpenApiCustomiser demoPathOrder() {
        return openApi -> {
            Paths paths = openApi.getPaths();
            if (paths == null) {
                return;
            }
            // Swagger's method sorter is alphabetical (DELETE first); keep schema order.
            Map<String, PathItem> orderedPaths = new LinkedHashMap<>();
            paths.entrySet().stream()
                    .sorted(Comparator.comparingInt(
                            (Map.Entry<String, PathItem> entry) ->
                                    firstMethodOrder(entry.getValue()))
                            .thenComparing(Map.Entry::getKey))
                    .forEach(entry -> orderedPaths.put(entry.getKey(), entry.getValue()));
            paths.clear();
            paths.putAll(orderedPaths);
        };
    }

    @Bean
    public OpenAPI cinemaOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Cinema API").version("1.0-SNAPSHOT")
                        .description("""
                                [Sign in](../login) · [Sign out](../logout)

                                Sign in with your email, or use **Authorize** for HTTP Basic. \
                                Check your active account with **GET /me**.

                                **Switch accounts:** sign in again, or change Basic credentials \
                                in **Authorize**, then execute **GET /me**.

                                **Sign out:** **Sign out** ends your session. \
                                **Authorize → Logout** only clears Basic credentials \
                                in Swagger. Basic requests can sign you in again."""))
                .tags(List.of(
                        new Tag().name("Authentication"),
                        new Tag().name("Movies"),
                        new Tag().name("Cinema halls"),
                        new Tag().name("Movie sessions"),
                        new Tag().name("Shopping cart"),
                        new Tag().name("Orders"),
                        new Tag().name("Users")))
                .components(new Components().addSecuritySchemes(BASIC_AUTH_SCHEME,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")
                                .description("Use your email as username. "
                                        + "Basic requests can change the account "
                                        + "in your session.")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH_SCHEME));
    }

    private static int firstMethodOrder(PathItem path) {
        return path.readOperationsMap().keySet().stream()
                .mapToInt(METHOD_ORDER::indexOf)
                .min()
                .orElse(METHOD_ORDER.size());
    }
}
