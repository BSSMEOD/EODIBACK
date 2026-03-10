package com.eod.eod.common.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KoreanHolidayChecker {

    // 매년 고정 공휴일 (월/일)
    private static final Set<MonthDay> FIXED_HOLIDAYS = Set.of(
            MonthDay.of(1, 1),   // 신정
            MonthDay.of(3, 1),   // 삼일절
            MonthDay.of(5, 5),   // 어린이날
            MonthDay.of(6, 6),   // 현충일
            MonthDay.of(8, 15),  // 광복절
            MonthDay.of(10, 3),  // 개천절
            MonthDay.of(10, 9),  // 한글날
            MonthDay.of(12, 25)  // 크리스마스
    );

    // 음력 기반 공휴일 + 대체공휴일 (연도별 하드코딩)
    private static final Map<Integer, List<LocalDate>> YEARLY_HOLIDAYS = Map.of(
            2025, List.of(
                    LocalDate.of(2025, 1, 28),  // 설날 전날
                    LocalDate.of(2025, 1, 29),  // 설날
                    LocalDate.of(2025, 1, 30),  // 설날 다음날
                    LocalDate.of(2025, 3, 3),   // 삼일절 대체공휴일 (3/1 토)
                    LocalDate.of(2025, 5, 6),   // 부처님오신날 대체공휴일 (5/5 어린이날 겹침)
                    LocalDate.of(2025, 10, 6),  // 추석
                    LocalDate.of(2025, 10, 7),  // 추석 다음날
                    LocalDate.of(2025, 10, 8)   // 추석 대체공휴일 (10/5 일)
            ),
            2026, List.of(
                    LocalDate.of(2026, 2, 17),  // 설날 전날
                    LocalDate.of(2026, 2, 18),  // 설날
                    LocalDate.of(2026, 2, 19),  // 설날 다음날
                    LocalDate.of(2026, 3, 2),   // 삼일절 대체공휴일 (3/1 일)
                    LocalDate.of(2026, 5, 25),  // 부처님오신날 대체공휴일 (5/24 일)
                    LocalDate.of(2026, 6, 8),   // 현충일 대체공휴일 (6/6 토)
                    LocalDate.of(2026, 8, 17),  // 광복절 대체공휴일 (8/15 토)
                    LocalDate.of(2026, 9, 24),  // 추석 전날
                    LocalDate.of(2026, 9, 25),  // 추석
                    LocalDate.of(2026, 9, 28),  // 추석 대체공휴일 (9/26 토)
                    LocalDate.of(2026, 10, 5)   // 개천절 대체공휴일 (10/3 토)
            )
    );

    public static boolean isHoliday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return true;
        }
        if (FIXED_HOLIDAYS.contains(MonthDay.from(date))) {
            return true;
        }
        List<LocalDate> yearlyHolidays = YEARLY_HOLIDAYS.get(date.getYear());
        return yearlyHolidays != null && yearlyHolidays.contains(date);
    }
}