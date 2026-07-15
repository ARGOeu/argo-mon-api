package org.grnet.status.validators;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.grnet.status.constraints.ValidDowntimeClassification;
import org.grnet.status.enums.DowntimeClassification;

import java.util.Arrays;

@RegisterForReflection
public class DowntimeClassificationValidator implements ConstraintValidator<ValidDowntimeClassification, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.equals("")) {
            return true;
        }

        return Arrays.stream(DowntimeClassification.values())
                .anyMatch(e -> e.name().equals(value));
    }
}