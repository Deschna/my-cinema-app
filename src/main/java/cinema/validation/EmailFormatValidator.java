package cinema.validation;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailFormatValidator implements ConstraintValidator<EmailFormat, String> {
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final String LOCAL_SEGMENT = "[A-Za-z0-9]+";
    private static final String DOMAIN_LABEL =
            "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            LOCAL_SEGMENT + "(?:[._+'-]" + LOCAL_SEGMENT + ")*@"
                    + DOMAIN_LABEL + "(?:\\." + DOMAIN_LABEL + ")*\\.[A-Za-z]{2,63}");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true;
        }
        if (email.length() > MAX_EMAIL_LENGTH
                || !EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }
        return email.indexOf('@') <= MAX_LOCAL_PART_LENGTH;
    }
}
