package com.audin.motivora.dto.Validation;

import org.springframework.stereotype.Component;

import com.audin.motivora.dto.Annotation.UniqueField;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UniqueFieldValidator implements ConstraintValidator<UniqueField, Object>{

    private final EntityManager entityManager;
    private Class<?> entityClass;
    private String fieldName;

    @Override
    public void initialize(UniqueField constraintAnnotation){
        this.entityClass = constraintAnnotation.entity();
        this.fieldName = constraintAnnotation.fieldName();
    }
    @Override
    public boolean isValid(Object fieldValue, ConstraintValidatorContext context) {
        if(fieldValue == null){
            return true;
        }
        String query = String.format(
            "SELECT COUNT(e) FROM %s e WHERE e.%s = :value",
            entityClass.getSimpleName(),
            fieldName
        );

        Long count = entityManager.createQuery(query, Long.class)
            .setParameter("value", fieldValue)
            .getSingleResult();

        return count == 0;
    }

}
