package cinema.dto.request;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

/**
 * Provides JSON binding and Bean Validation support for request DTO contract tests.
 */
final class RequestDtoValidationFixture implements AutoCloseable {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private final ValidatorFactory validatorFactory = Validation
            .byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    <T> Set<ConstraintViolation<T>> validateJson(String json, Class<T> type)
            throws JsonProcessingException {
        return validator.validate(objectMapper.readValue(json, type));
    }

    String toJson(Map<String, ?> properties) throws JsonProcessingException {
        return objectMapper.writeValueAsString(properties);
    }

    void assertViolationOnProperty(
            Set<? extends ConstraintViolation<?>> violations,
            String property) {
        assertTrue(violations.stream().anyMatch(
                violation -> property.equals(violation.getPropertyPath().toString())
        ), "Expected violation on " + property + ", got " + violations);
    }

    @Override
    public void close() {
        validatorFactory.close();
    }
}
