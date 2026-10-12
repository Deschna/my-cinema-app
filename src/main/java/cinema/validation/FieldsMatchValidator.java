package cinema.validation;

import java.util.Objects;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class FieldsMatchValidator
        implements ConstraintValidator<FieldsMatch, Object> {
    private String field;
    private String matchingField;

    public void initialize(FieldsMatch constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.matchingField = constraintAnnotation.matchingField();
    }

    public boolean isValid(Object value,
                           ConstraintValidatorContext context) {
        Object fieldValue = new BeanWrapperImpl(value)
                .getPropertyValue(field);
        Object matchingFieldValue = new BeanWrapperImpl(value)
                .getPropertyValue(matchingField);

        return Objects.equals(fieldValue, matchingFieldValue);
    }
}
