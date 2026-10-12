package cinema.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailFormatValidator implements ConstraintValidator<EmailFormat, String> {
    private static final String EMAIL_VALIDATION_REGEX = "^(.+)@(.+)$";

    @Override
    public boolean isValid(String field, ConstraintValidatorContext context) {
        return field != null && field.matches(EMAIL_VALIDATION_REGEX);
    }
}
