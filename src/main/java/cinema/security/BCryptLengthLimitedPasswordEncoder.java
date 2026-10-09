package cinema.security;

import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class BCryptLengthLimitedPasswordEncoder implements PasswordEncoder {
    private static final int MAX_PASSWORD_LENGTH_BYTES = 72;

    private final PasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        if (exceedsMaximumByteLength(rawPassword)) {
            throw new IllegalArgumentException("Password must not exceed "
                    + MAX_PASSWORD_LENGTH_BYTES + " bytes in UTF-8");
        }
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return !exceedsMaximumByteLength(rawPassword)
                && delegate.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return delegate.upgradeEncoding(encodedPassword);
    }

    private static boolean exceedsMaximumByteLength(CharSequence rawPassword) {
        return rawPassword != null
                && rawPassword.toString().getBytes(StandardCharsets.UTF_8).length
                > MAX_PASSWORD_LENGTH_BYTES;
    }
}
