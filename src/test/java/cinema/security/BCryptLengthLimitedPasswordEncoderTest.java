package cinema.security;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cinema.config.RootConfig;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class BCryptLengthLimitedPasswordEncoderTest {
    private static final int MAX_PASSWORD_LENGTH_BYTES = 72;

    private final PasswordEncoder lengthLimitedEncoder =
            new BCryptLengthLimitedPasswordEncoder();

    @Test
    void encodesAndMatchesPasswordAtMaximumByteLength() {
        String passwordAtLimit = asciiPasswordAtLimit();

        assertEquals(MAX_PASSWORD_LENGTH_BYTES, passwordAtLimit.getBytes(UTF_8).length);

        String hashOfPasswordAtLimit = lengthLimitedEncoder.encode(passwordAtLimit);

        assertTrue(lengthLimitedEncoder.matches(passwordAtLimit, hashOfPasswordAtLimit));
    }

    @Test
    void rejectsMultibytePasswordAboveMaximumByteLengthWhenEncoding() {
        String passwordAtLimit = multibytePasswordAtLimit();
        String passwordAboveLimit = passwordAtLimit + "a";

        assertEquals(MAX_PASSWORD_LENGTH_BYTES, passwordAtLimit.getBytes(UTF_8).length);
        assertTrue(passwordAboveLimit.getBytes(UTF_8).length > MAX_PASSWORD_LENGTH_BYTES);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> lengthLimitedEncoder.encode(passwordAboveLimit));
        assertEquals("Password must not exceed " + MAX_PASSWORD_LENGTH_BYTES
                + " bytes in UTF-8", exception.getMessage());
    }

    @Test
    void returnsFalseForMultibytePasswordAboveMaximumByteLengthWhenMatching() {
        String passwordAtLimit = multibytePasswordAtLimit();
        String passwordAboveLimit = passwordAtLimit + "a";

        String hashOfPasswordAtLimit = lengthLimitedEncoder.encode(passwordAtLimit);
        assertTrue(lengthLimitedEncoder.matches(passwordAtLimit, hashOfPasswordAtLimit));

        // Both values share the maximum-length prefix; the extra byte must prevent a match.
        assertFalse(lengthLimitedEncoder.matches(passwordAboveLimit, hashOfPasswordAtLimit));
    }

    @Test
    void remainsCompatibleWithStandardBCryptHashes() {
        PasswordEncoder standardEncoder = new BCryptPasswordEncoder();
        String password = "existing-password";
        String hashCreatedByStandardEncoder = standardEncoder.encode(password);
        String hashCreatedByLengthLimitedEncoder = lengthLimitedEncoder.encode(password);

        assertTrue(lengthLimitedEncoder.matches(password, hashCreatedByStandardEncoder));
        assertTrue(standardEncoder.matches(password, hashCreatedByLengthLimitedEncoder));
        assertFalse(lengthLimitedEncoder.matches("different-password",
                hashCreatedByStandardEncoder));
    }

    @Test
    void rootConfigExposesLengthLimitedPasswordEncoder() {
        PasswordEncoder configuredEncoder = new RootConfig(new StandardEnvironment()).getEncoder();

        assertInstanceOf(BCryptLengthLimitedPasswordEncoder.class, configuredEncoder);
    }

    private static String asciiPasswordAtLimit() {
        // Each ASCII character occupies one byte in UTF-8, so this fills the limit exactly.
        return "a".repeat(MAX_PASSWORD_LENGTH_BYTES);
    }

    private static String multibytePasswordAtLimit() {
        String multibyteCharacter = "é";
        int characterByteLength = multibyteCharacter.getBytes(UTF_8).length;
        // "é" occupies two bytes in UTF-8, so this repeat count fills the limit exactly.
        return multibyteCharacter.repeat(MAX_PASSWORD_LENGTH_BYTES / characterByteLength);
    }
}
