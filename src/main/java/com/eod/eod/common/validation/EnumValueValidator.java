package com.eod.eod.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {

    private Enum<?>[] acceptedValues;
    private boolean ignoreCase;
    private boolean allowBlank;

    @Override
    public void initialize(EnumValue constraintAnnotation) {
        acceptedValues = constraintAnnotation.enumClass().getEnumConstants();
        ignoreCase = constraintAnnotation.ignoreCase();
        allowBlank = constraintAnnotation.allowBlank();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return allowBlank;
        }

        for (Enum<?> enumConstant : acceptedValues) {
            if (ignoreCase) {
                if (enumConstant.name().equalsIgnoreCase(value)) {
                    return true;
                }
            } else if (enumConstant.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
