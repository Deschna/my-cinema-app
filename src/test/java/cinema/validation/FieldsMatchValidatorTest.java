package cinema.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class FieldsMatchValidatorTest {
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation
            .byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "same value"})
    void acceptsEqualValues(String value) {
        assertTrue(violations(value, value).isEmpty());
    }

    @Test
    void rejectsDifferentValuesAndReportsMatchingField() {
        Set<ConstraintViolation<FieldsFixture>> violations = violations("first", "second");

        assertEquals(1, violations.size());
        ConstraintViolation<FieldsFixture> violation = violations.iterator().next();
        assertEquals("matchingValue", violation.getPropertyPath().toString());
        assertEquals("fields must match", violation.getMessage());
        assertEquals(FieldsMatch.class,
                violation.getConstraintDescriptor().getAnnotation().annotationType());
    }

    @Test
    void rejectsWhenOnlyOneValueIsNull() {
        assertFalse(violations(null, "value").isEmpty());
        assertFalse(violations("value", null).isEmpty());
    }

    @Test
    void acceptsNullObject() {
        assertTrue(new FieldsMatchValidator().isValid(null, null));
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    private static Set<ConstraintViolation<FieldsFixture>> violations(
            String value, String matchingValue) {
        return VALIDATOR.validate(new FieldsFixture(value, matchingValue));
    }

    @FieldsMatch(field = "value", matchingField = "matchingValue")
    private record FieldsFixture(String value, String matchingValue) {
    }
}
