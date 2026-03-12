package com.eod.eod.common.validation;

import com.eod.eod.common.util.KoreanHolidayChecker;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class VisitDateValidator implements ConstraintValidator<ValidVisitDate, LocalDate> {

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // @NotNull이 처리
        }
        if (!date.isAfter(LocalDate.now())) {
            return true; // @Future가 처리
        }
        return !KoreanHolidayChecker.isHoliday(date);
    }
}