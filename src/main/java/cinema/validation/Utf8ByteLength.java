package cinema.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validates the maximum length of a string's UTF-8 representation.
 *
 * <p>The configured maximum must be non-negative. The value is measured without
 * normalization or truncation. Null values are valid and must be rejected
 * separately when the field is required.
 */
@Documented
@Constraint(validatedBy = Utf8ByteLengthValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Utf8ByteLength {
    String message() default "must be at most {max} bytes in UTF-8";

    int max();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
