package org.grnet.status.validators;

import io.vavr.control.Try;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.grnet.status.constraints.UniqueValue;
import org.grnet.status.exceptions.CustomValidationException;
import org.grnet.status.repositories.Repository;

import java.util.Objects;

public class UniqueValueValidator implements ConstraintValidator<UniqueValue, Object> {

    private String message;
    private String fieldName;
    private Class<? extends Repository<?, ?>> repositoryClass;

    @Override
    public void initialize(UniqueValue annotation) {
        this.message = annotation.message();
        this.fieldName = annotation.fieldName();
        this.repositoryClass = annotation.repository();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (Objects.isNull(value)) {
            return true;
        }

        var repository = CDI.current()
                .select(repositoryClass)
                .get();

        StringBuilder builder = new StringBuilder();
        builder.append(message)
                .append(StringUtils.SPACE)
                .append(value);

        Try.run(() -> {
                    boolean exists = repository.existsByField(fieldName, value);
                    if (exists) {
                        throw new CustomValidationException(builder.toString(), 409);
                    }
                })
                .getOrElseThrow(() -> new CustomValidationException(builder.toString(), 409));


        return true;
    }
}
