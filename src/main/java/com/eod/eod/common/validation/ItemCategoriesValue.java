package com.eod.eod.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = ItemCategoriesValueValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface ItemCategoriesValue {

    String message() default "유효하지 않은 카테고리 값입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
