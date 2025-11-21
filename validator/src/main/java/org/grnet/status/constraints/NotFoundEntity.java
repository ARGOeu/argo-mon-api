package  org.grnet.status.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.grnet.status.repositories.Repository;
import org.grnet.status.validators.NotFoundEntityValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotFoundEntityValidator.class)
@Documented
public @interface NotFoundEntity {

    String message() default "Not founded:";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<? extends Repository<?,?>> repository();
}
