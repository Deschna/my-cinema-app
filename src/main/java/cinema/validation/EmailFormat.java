package cinema.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validates a conservative ASCII email format.
 *
 * <p>This constraint checks syntax only; it does not verify that the mailbox
 * or domain exists. Null values are valid and must be rejected separately
 * when the field is required.
 */
@Documented
@Constraint(validatedBy = EmailFormatValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailFormat {
    String message() default "must have a valid email format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
