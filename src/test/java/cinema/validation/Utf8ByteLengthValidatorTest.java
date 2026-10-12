package cinema.validation;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class Utf8ByteLengthValidatorTest {
    private static final int MAX_UTF8_BYTES = 72;
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation
            .byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "password"})
    void acceptsValuesWithinLimit(String value) {
        assertTrue(violations(value).isEmpty());
    }

    @Test
    void enforcesUtf8ByteLimitWithoutNormalization() {
        String asciiAtLimit = "a".repeat(MAX_UTF8_BYTES);
        assertEquals(MAX_UTF8_BYTES, asciiAtLimit.getBytes(UTF_8).length);
        assertTrue(violations(asciiAtLimit).isEmpty());

        String asciiAboveLimit = asciiAtLimit + "a";
        assertEquals(MAX_UTF8_BYTES + 1, asciiAboveLimit.getBytes(UTF_8).length);
        assertFalse(violations(asciiAboveLimit).isEmpty());

        // A decomposed accented e occupies three UTF-8 bytes; normalization reduces it to two.
        String decomposedCharacter = "e" + Character.toString(0x0301);
        String decomposedAtLimit = decomposedCharacter.repeat(MAX_UTF8_BYTES / 3);
        assertEquals(MAX_UTF8_BYTES, decomposedAtLimit.getBytes(UTF_8).length);
        assertTrue(violations(decomposedAtLimit).isEmpty());

        String decomposedAboveLimit = decomposedAtLimit + "a";
        assertEquals(MAX_UTF8_BYTES + 1, decomposedAboveLimit.getBytes(UTF_8).length);
        assertFalse(violations(decomposedAboveLimit).isEmpty());
    }

    @Test
    void reportsViolationOnConstrainedField() {
        Set<ConstraintViolation<ByteLengthFixture>> violations =
                violations("a".repeat(MAX_UTF8_BYTES + 1));

        assertEquals(1, violations.size());
        ConstraintViolation<ByteLengthFixture> violation = violations.iterator().next();
        assertEquals("value", violation.getPropertyPath().toString());
        assertEquals("must be at most 72 bytes in UTF-8", violation.getMessage());
        assertEquals(Utf8ByteLength.class,
                violation.getConstraintDescriptor().getAnnotation().annotationType());
    }

    @Test
    void rejectsNegativeMaximumConfiguration() {
        assertThrows(ValidationException.class,
                () -> VALIDATOR.validate(new InvalidConfigurationFixture("value")));
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    private static Set<ConstraintViolation<ByteLengthFixture>> violations(String value) {
        return VALIDATOR.validate(new ByteLengthFixture(value));
    }

    private record ByteLengthFixture(
            @Utf8ByteLength(max = MAX_UTF8_BYTES) String value) {
    }

    private record InvalidConfigurationFixture(
            @Utf8ByteLength(max = -1) String value) {
    }
}
