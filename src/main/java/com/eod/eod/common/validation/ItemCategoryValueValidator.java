package com.eod.eod.common.validation;

import com.eod.eod.domain.item.model.Item;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ItemCategoryValueValidator implements ConstraintValidator<ItemCategoryValue, String> {

    private boolean allowBlank;

    @Override
    public void initialize(ItemCategoryValue constraintAnnotation) {
        allowBlank = constraintAnnotation.allowBlank();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return allowBlank;
        }

        try {
            // ItemCategory.from() 메서드를 사용하여 한글 이름으로 검증
            Item.ItemCategory.from(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
