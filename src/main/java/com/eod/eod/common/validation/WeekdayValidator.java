package com.eod.eod.common.validation;

import com.eod.eod.common.util.KoreanHolidayChecker;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class WeekdayValidator implements ConstraintValidator<Weekday, LocalDate> {

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // @NotNull이 처리
        }
        if (date.isBefore(LocalDate.now())) {
            return true; // 과거 날짜는 서비스 레이어에서 중복 신청 검증 후 처리
        }
        return !KoreanHolidayChecker.isHoliday(date);
    }
}
