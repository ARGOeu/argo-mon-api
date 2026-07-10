package org.grnet.status.constraints;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.grnet.status.validators.ContactTypeValidator;
import org.grnet.status.validators.DowntimeClassificationValidator;
import org.grnet.status.validators.DowntimeSeverityValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@RegisterForReflection
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DowntimeClassificationValidator.class)
public @interface ValidDowntimeClassification {
    String message() default "Invalid Classification type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

