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

class EmailFormatValidatorTest {
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_DOMAIN_LABEL_LENGTH = 63;
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation
            .byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "User2@Example2.COM",
            "a@x.co",
            "first.last+tag@sub-domain.example.co.uk",
            "a.b_c+d'e-f@example.travel"
    })
    void acceptsSupportedEmailFormats(String email) {
        assertTrue(violations(email).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "user",
            "@example.com",
            "user@",
            "user@@example.com",
            ".user@example.com",
            "user.@example.com",
            "user..name@example.com",
            "user%tag@example.com",
            " user@example.com",
            "user@example.com ",
            "user name@example.com",
            "user\n@example.com",
            "user@localhost",
            "user@-example.com",
            "user@example-.com",
            "user@example..com",
            "user@example.c",
            "user@example.123",
            "naïve@example.com",
            "user@exämple.com"
    })
    void rejectsUnsupportedEmailFormats(String email) {
        assertFalse(violations(email).isEmpty());
    }

    @Test
    void enforcesIndependentLengthLimits() {
        String localPartAtLimit = "a".repeat(MAX_LOCAL_PART_LENGTH);
        String domainLabelAtLimit = "b".repeat(MAX_DOMAIN_LABEL_LENGTH);

        assertTrue(violations(localPartAtLimit + "@example.com").isEmpty());
        assertFalse(violations(localPartAtLimit + "a@example.com").isEmpty());
        assertTrue(violations("user@" + domainLabelAtLimit + ".com").isEmpty());
        assertFalse(violations("user@" + domainLabelAtLimit + "b.com").isEmpty());
        assertTrue(violations("user@example." + domainLabelAtLimit).isEmpty());
        assertFalse(violations("user@example." + domainLabelAtLimit + "b").isEmpty());

        String emailAtLimit = emailAtTotalLengthLimit();
        assertEquals(MAX_EMAIL_LENGTH, emailAtLimit.length());
        assertTrue(violations(emailAtLimit).isEmpty());

        String emailAboveLimit = emailAtLimit + "d";
        assertEquals(MAX_EMAIL_LENGTH + 1, emailAboveLimit.length());
        assertFalse(violations(emailAboveLimit).isEmpty());
    }

    @Test
    void reportsViolationOnEmailField() {
        Set<ConstraintViolation<EmailFixture>> violations = violations("invalid");

        assertEquals(1, violations.size());
        ConstraintViolation<EmailFixture> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("must have a valid email format", violation.getMessage());
        assertEquals(EmailFormat.class,
                violation.getConstraintDescriptor().getAnnotation().annotationType());
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    private static Set<ConstraintViolation<EmailFixture>> violations(String email) {
        return VALIDATOR.validate(new EmailFixture(email));
    }

    private static String emailAtTotalLengthLimit() {
        String emailWithoutFinalLabel = "a".repeat(MAX_LOCAL_PART_LENGTH)
                + "@"
                + "b".repeat(MAX_DOMAIN_LABEL_LENGTH)
                + "."
                + "c".repeat(MAX_DOMAIN_LABEL_LENGTH)
                + ".";
        return emailWithoutFinalLabel
                + "d".repeat(MAX_EMAIL_LENGTH - emailWithoutFinalLabel.length());
    }

    private record EmailFixture(@EmailFormat String email) {
    }
}
