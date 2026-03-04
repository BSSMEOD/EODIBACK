package com.eod.eod.common.validation;

import com.eod.eod.domain.item.model.Item;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ItemStatusesValueValidator implements ConstraintValidator<ItemStatusesValue, List<String>> {

    @Override
    public void initialize(ItemStatusesValue constraintAnnotation) {
    }

    @Override
    public boolean isValid(List<String> values, ConstraintValidatorContext context) {
        if (values == null || values.isEmpty()) {
            return true;
        }

        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }

            String[] splitValues = value.split(",");
            for (String splitValue : splitValues) {
                String trimmedValue = splitValue.trim();
                if (trimmedValue.isEmpty()) {
                    continue;
                }

                try {
                    Item.ItemStatus.valueOf(trimmedValue.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        }

        return true;
    }
}
