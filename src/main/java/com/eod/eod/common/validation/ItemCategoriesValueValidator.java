package com.eod.eod.common.validation;

import com.eod.eod.domain.item.model.Item;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ItemCategoriesValueValidator implements ConstraintValidator<ItemCategoriesValue, List<String>> {

    @Override
    public void initialize(ItemCategoriesValue constraintAnnotation) {
    }

    @Override
    public boolean isValid(List<String> values, ConstraintValidatorContext context) {
        if (values == null || values.isEmpty()) {
            return true;
        }

        for (String value : values) {
            if (value == null || value.isBlank()) {
                return false;
            }

            try {
                Item.ItemCategory.from(value);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        return true;
    }
}
