package cinema.config;

import javax.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "cinema.controller")
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Match the exact paths used by the Security rules, without an optional trailing slash.
        configurer.setUseTrailingSlashMatch(false);
    }

    // Spring MVC creates a validator for @RequestBody DTOs automatically. We declare one
    // explicitly so MethodValidationPostProcessor validates @RequestParam and @PathVariable
    // with the same configuration. ParameterMessageInterpolator is used because Java EL is
    // not included in the project and is unnecessary for our validation messages.
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setMessageInterpolator(new ParameterMessageInterpolator());
        return validator;
    }

    @Bean
    public static MethodValidationPostProcessor methodValidationPostProcessor(
            Validator validator) {
        MethodValidationPostProcessor postProcessor = new MethodValidationPostProcessor();
        postProcessor.setValidator(validator);
        return postProcessor;
    }

    @Override
    public org.springframework.validation.Validator getValidator() {
        return validator();
    }
}
