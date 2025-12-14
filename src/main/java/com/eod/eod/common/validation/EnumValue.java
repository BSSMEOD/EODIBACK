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
@Constraint(validatedBy = EnumValueValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface EnumValue {

    Class<? extends Enum<?>> enumClass();

    boolean ignoreCase() default true;

    boolean allowBlank() default true;

    String message() default "허용되지 않는 값입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
