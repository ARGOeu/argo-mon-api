package org.grnet.status.validators;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.grnet.status.constraints.ValidDowntimeSeverity;
import org.grnet.status.enums.DowntimeSeverity;

import java.util.Arrays;

@RegisterForReflection
public class DowntimeSeverityValidator implements ConstraintValidator<ValidDowntimeSeverity, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.equals("")) {
            return true;
        }

        return Arrays.stream(DowntimeSeverity.values())
                .anyMatch(e -> e.name().equals(value));
    }
}
