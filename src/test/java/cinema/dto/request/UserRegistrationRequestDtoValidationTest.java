package cinema.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class UserRegistrationRequestDtoValidationTest {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 40;
    private static final int MAX_PASSWORD_UTF8_BYTES = 72;
    private static final RequestDtoValidationFixture VALIDATION =
            new RequestDtoValidationFixture();

    @Test
    void rejectsMissingAndBlankRegistrationFields() throws JsonProcessingException {
        Set<ConstraintViolation<UserRegistrationRequestDto>> missing = VALIDATION.validateJson(
                "{}",
                UserRegistrationRequestDto.class
        );
        VALIDATION.assertViolationOnProperty(missing, "email");
        VALIDATION.assertViolationOnProperty(missing, "password");
        VALIDATION.assertViolationOnProperty(missing, "repeatPassword");

        Set<ConstraintViolation<UserRegistrationRequestDto>> blank = VALIDATION.validateJson(
                registrationJson(
                        " ",
                        " ".repeat(MIN_PASSWORD_LENGTH),
                        " ".repeat(MIN_PASSWORD_LENGTH)
                ),
                UserRegistrationRequestDto.class
        );
        VALIDATION.assertViolationOnProperty(blank, "email");
        VALIDATION.assertViolationOnProperty(blank, "password");
        VALIDATION.assertViolationOnProperty(blank, "repeatPassword");
    }

    @Test
    void enforcesPasswordCharacterAndUtf8Limits() throws JsonProcessingException {
        String utf8TestCharacter = "é";
        int characterByteLength = utf8TestCharacter.getBytes(StandardCharsets.UTF_8).length;
        String passwordAtByteLimit = utf8TestCharacter.repeat(
                MAX_PASSWORD_UTF8_BYTES / characterByteLength
        );
        String passwordAboveByteLimit = passwordAtByteLimit + "a";
        assertEquals(MAX_PASSWORD_UTF8_BYTES,
                passwordAtByteLimit.getBytes(StandardCharsets.UTF_8).length);
        assertEquals(MAX_PASSWORD_UTF8_BYTES + 1,
                passwordAboveByteLimit.getBytes(StandardCharsets.UTF_8).length);

        assertTrue(VALIDATION.validateJson(
                registrationJson("user@example.com", passwordAtByteLimit, passwordAtByteLimit),
                UserRegistrationRequestDto.class
        ).isEmpty());
        String passwordAtCharacterLimit = "a".repeat(MAX_PASSWORD_LENGTH);
        assertTrue(VALIDATION.validateJson(
                registrationJson(
                        "user@example.com",
                        passwordAtCharacterLimit,
                        passwordAtCharacterLimit
                ),
                UserRegistrationRequestDto.class
        ).isEmpty());
        VALIDATION.assertViolationOnProperty(VALIDATION.validateJson(
                registrationJson(
                        "user@example.com",
                        passwordAboveByteLimit,
                        passwordAboveByteLimit
                ),
                UserRegistrationRequestDto.class
        ), "password");
        VALIDATION.assertViolationOnProperty(VALIDATION.validateJson(
                registrationJson(
                        "user@example.com",
                        "a".repeat(MIN_PASSWORD_LENGTH - 1),
                        "a".repeat(MIN_PASSWORD_LENGTH - 1)
                ),
                UserRegistrationRequestDto.class
        ), "password");
        VALIDATION.assertViolationOnProperty(VALIDATION.validateJson(
                registrationJson(
                        "user@example.com",
                        "a".repeat(MAX_PASSWORD_LENGTH + 1),
                        "a".repeat(MAX_PASSWORD_LENGTH + 1)
                ),
                UserRegistrationRequestDto.class
        ), "password");
    }

    @Test
    void reportsPasswordMismatchOnRepeatPassword() throws JsonProcessingException {
        Set<ConstraintViolation<UserRegistrationRequestDto>> violations = VALIDATION.validateJson(
                registrationJson("user@example.com", "password", "different"),
                UserRegistrationRequestDto.class
        );

        assertEquals(1, violations.size());
        assertEquals("repeatPassword", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void acceptsMixedCaseEmailAndRejectsLeadingWhitespace() throws JsonProcessingException {
        assertTrue(VALIDATION.validateJson(
                registrationJson("User@example.com", "password", "password"),
                UserRegistrationRequestDto.class
        ).isEmpty());
        VALIDATION.assertViolationOnProperty(VALIDATION.validateJson(
                registrationJson(" User@example.com", "password", "password"),
                UserRegistrationRequestDto.class
        ), "email");
    }

    @AfterAll
    static void closeValidationFixture() {
        VALIDATION.close();
    }

    private static String registrationJson(String email, String password, String repeatPassword)
            throws JsonProcessingException {
        return VALIDATION.toJson(Map.of(
                "email", email,
                "password", password,
                "repeatPassword", repeatPassword
        ));
    }
}
