package cinema.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import org.springdoc.core.SwaggerUiConfigParameters;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springdoc.core.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

public class SwaggerUiTransformer extends SwaggerIndexPageTransformer {
    private static final String STYLESHEET_PATH = "../ui/css/demo-ui.css";

    public SwaggerUiTransformer(SwaggerUiConfigProperties properties,
            SwaggerUiOAuthProperties oauthProperties, SwaggerUiConfigParameters parameters,
            SwaggerWelcomeCommon welcome, ObjectMapperProvider objectMapperProvider) {
        super(properties, oauthProperties, parameters, welcome, objectMapperProvider);
    }

    @Override
    @NonNull
    public Resource transform(@NonNull HttpServletRequest request, @NonNull Resource resource,
                              @NonNull ResourceTransformerChain chain) throws IOException {
        Resource transformed = super.transform(request, resource, chain);
        if (!"index.html".equals(resource.getFilename())) {
            return transformed;
        }
        try (InputStream input = transformed.getInputStream()) {
            String html = StreamUtils.copyToString(input, StandardCharsets.UTF_8);
            String stylesheet = "<link rel=\"stylesheet\" href=\"" + STYLESHEET_PATH + "\">\n";
            return new TransformedResource(transformed,
                    html.replace("</head>", stylesheet + "</head>")
                            .getBytes(StandardCharsets.UTF_8));
        }
    }
}
