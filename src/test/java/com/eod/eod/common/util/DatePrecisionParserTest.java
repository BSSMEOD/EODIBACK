package com.eod.eod.common.util;

import com.eod.eod.common.util.DatePrecisionParser.ParsedDate;
import com.eod.eod.domain.item.model.Item.DatePrecision;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatePrecisionParserTest {

    @Test
    void yyyy_형식_정상_파싱() {
        // given
        String input = "2025";

        // when
        ParsedDate result = DatePrecisionParser.parse(input);

        // then
        assertThat(result.getDateTime()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        assertThat(result.getPrecision()).isEqualTo(DatePrecision.YEAR);
    }

    @Test
    void yyyy_MM_형식_정상_파싱() {
        // given
        String input = "2025-01";

        // when
        ParsedDate result = DatePrecisionParser.parse(input);

        // then
        assertThat(result.getDateTime()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        assertThat(result.getPrecision()).isEqualTo(DatePrecision.MONTH);
    }

    @Test
    void yyyy_MM_dd_형식_정상_파싱() {
        // given
        String input = "2025-01-07";

        // when
        ParsedDate result = DatePrecisionParser.parse(input);

        // then
        assertThat(result.getDateTime()).isEqualTo(LocalDateTime.of(2025, 1, 7, 0, 0, 0));
        assertThat(result.getPrecision()).isEqualTo(DatePrecision.DAY);
    }

    @Test
    void 윤년_2월_29일_정상_파싱() {
        // given
        String input = "2024-02-29";

        // when
        ParsedDate result = DatePrecisionParser.parse(input);

        // then
        assertThat(result.getDateTime()).isEqualTo(LocalDateTime.of(2024, 2, 29, 0, 0, 0));
        assertThat(result.getPrecision()).isEqualTo(DatePrecision.DAY);
    }

    @Test
    void 앞뒤_공백_제거_후_파싱() {
        // given
        String input = "  2025-01-07  ";

        // when
        ParsedDate result = DatePrecisionParser.parse(input);

        // then
        assertThat(result.getDateTime()).isEqualTo(LocalDateTime.of(2025, 1, 7, 0, 0, 0));
        assertThat(result.getPrecision()).isEqualTo(DatePrecision.DAY);
    }

    @Test
    void 십이월_파싱_검증() {
        // given
        String input = "2025-12";

        // when
        ParsedDate result = DatePrecisionParser.parse(input);

        // then
        assertThat(result.getDateTime()).isEqualTo(LocalDateTime.of(2025, 12, 1, 0, 0, 0));
        assertThat(result.getPrecision()).isEqualTo(DatePrecision.MONTH);
    }

    @Test
    void null_입력_시_예외() {
        // when & then
        assertThatThrownBy(() -> DatePrecisionParser.parse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("습득 날짜는 필수입니다.");
    }

    @Test
    void 빈_문자열_입력_시_예외() {
        // when & then
        assertThatThrownBy(() -> DatePrecisionParser.parse(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("습득 날짜는 필수입니다.");
    }

    @Test
    void 공백만_있는_문자열_입력_시_예외() {
        // when & then
        assertThatThrownBy(() -> DatePrecisionParser.parse("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("습득 날짜는 필수입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2025-1",        // 한 자리 월
            "2025-1-07",     // 한 자리 월
            "202501",        // 구분자 없음
            "20250107",      // 구분자 없음
            "2025/01/07",    // 슬래시 구분자
            "2025.01.07",    // 점 구분자
            "25-01-07",      // 두 자리 연도
            "abcd-01-07",    // 문자 포함
            "2025-ab-07",    // 문자 포함
            "2025-01-ab"     // 문자 포함
    })
    void 잘못된_형식_입력_시_예외(String input) {
        // when & then
        assertThatThrownBy(() -> DatePrecisionParser.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("올바른 날짜 형식이 아닙니다. (yyyy, yyyy-MM, yyyy-MM-dd)");
    }

    @Test
    void 유효하지_않은_월_13월_입력_시_예외() {
        // given
        String input = "2025-13";

        // when & then
        assertThatThrownBy(() -> DatePrecisionParser.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바른 년월 형식이 아닙니다.");
    }

    @Test
    void 유효하지_않은_월_00월_입력_시_예외() {
        // given
        String input = "2025-00";

        // when & then
        assertThatThrownBy(() -> DatePrecisionParser.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바른 년월 형식이 아닙니다.");
    }

    @Test
    void 유효하지_않은_날짜_2월_30일_입력_시_예외() {
        // given
        String input = "2025-02-30";

        // when & then
        assertThatThrownBy(() -> DatePrecisionParser.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바른 날짜 형식이 아닙니다.");
    }

    @Test
    void 유효하지_않은_날짜_4월_31일_입력_시_예외() {
        // given
        String input = "2025-04-31";

        // when & then
        assertThatThrownBy(() -> DatePrecisionParser.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바른 날짜 형식이 아닙니다.");
    }

    @Test
    void 평년_2월_29일_입력_시_예외() {
        // given
        String input = "2025-02-29";

        // when & then
        assertThatThrownBy(() -> DatePrecisionParser.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바른 날짜 형식이 아닙니다.");
    }

    @Test
    void 유효하지_않은_날짜_00일_입력_시_예외() {
        // given
        String input = "2025-01-00";

        // when & then
        assertThatThrownBy(() -> DatePrecisionParser.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바른 날짜 형식이 아닙니다.");
    }

    @Test
    void 유효하지_않은_날짜_32일_입력_시_예외() {
        // given
        String input = "2025-01-32";

        // when & then
        assertThatThrownBy(() -> DatePrecisionParser.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바른 날짜 형식이 아닙니다.");
    }

    @Test
    void 과거_날짜_1900년_정상_파싱() {
        // given
        String input = "1900";

        // when
        ParsedDate result = DatePrecisionParser.parse(input);

        // then
        assertThat(result.getDateTime()).isEqualTo(LocalDateTime.of(1900, 1, 1, 0, 0, 0));
        assertThat(result.getPrecision()).isEqualTo(DatePrecision.YEAR);
    }

    @Test
    void 미래_날짜_2099년_정상_파싱() {
        // given
        String input = "2099-12-31";

        // when
        ParsedDate result = DatePrecisionParser.parse(input);

        // then
        assertThat(result.getDateTime()).isEqualTo(LocalDateTime.of(2099, 12, 31, 0, 0, 0));
        assertThat(result.getPrecision()).isEqualTo(DatePrecision.DAY);
    }
}
