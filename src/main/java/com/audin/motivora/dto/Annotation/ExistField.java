package com.audin.motivora.dto.Annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.audin.motivora.dto.Validation.ExistFieldValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = ExistFieldValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistField {
    String message() default "Field don't exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    Class<?> entity();

    String fieldName();
}
