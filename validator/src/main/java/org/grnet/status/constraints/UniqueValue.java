package org.grnet.status.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.grnet.status.validators.UniqueValueValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueValueValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueValue {

    String message() default "Value must be unique";

    Class<? extends org.grnet.status.repositories.Repository<?, ?>> repository();

    /**
     * The field name inside the entity to check for uniqueness.
     * Example: "name", "email", "code"
     */
    String fieldName();

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
