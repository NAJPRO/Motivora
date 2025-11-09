package com.audin.motivora.dto.Annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.audin.motivora.dto.Validation.UniqueFieldValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = UniqueFieldValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueField {
    String message() default "This value must be unique";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    Class<?> entity();

    String fieldName();
}
