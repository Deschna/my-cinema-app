package cinema.controller.validation;

import cinema.config.WebConfig;
import cinema.controller.GlobalExceptionHandler;
import java.util.function.Supplier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Provides method-validation proxy and MockMvc setup for controller parameter contract tests.
 */
final class RequestParameterValidationFixture implements AutoCloseable {
    private final LocalValidatorFactoryBean validator = createValidator();

    <T> AnnotationConfigApplicationContext controllerContext(
            Class<T> controllerType,
            Supplier<T> controllerSupplier) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(
                "methodValidationPostProcessor",
                MethodValidationPostProcessor.class,
                () -> WebConfig.methodValidationPostProcessor(validator)
        );
        context.registerBean(controllerType, controllerSupplier);
        context.refresh();
        return context;
    }

    MockMvc mockMvc(Object controller) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Override
    public void close() {
        validator.destroy();
    }

    private static LocalValidatorFactoryBean createValidator() {
        LocalValidatorFactoryBean validator = new WebConfig().validator();
        validator.afterPropertiesSet();
        return validator;
    }
}
