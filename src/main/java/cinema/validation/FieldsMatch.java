package cinema.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validates that two properties of an object have equal values.
 *
 * <p>A null object is valid. Each property's nullability must be constrained separately.
 */
@Documented
@Constraint(validatedBy = FieldsMatchValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldsMatch {
    String message() default "fields must match";

    String field();

    String matchingField();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
