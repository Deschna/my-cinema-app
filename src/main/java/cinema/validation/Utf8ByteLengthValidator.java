package cinema.validation;

import java.nio.charset.StandardCharsets;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Utf8ByteLengthValidator
        implements ConstraintValidator<Utf8ByteLength, String> {
    private int max;

    @Override
    public void initialize(Utf8ByteLength constraintAnnotation) {
        max = constraintAnnotation.max();
        if (max < 0) {
            throw new IllegalArgumentException("max must be non-negative");
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.getBytes(StandardCharsets.UTF_8).length <= max;
    }
}
