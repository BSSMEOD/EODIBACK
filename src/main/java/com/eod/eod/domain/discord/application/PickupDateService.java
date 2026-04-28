package com.eod.eod.domain.discord.application;

import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class PickupDateService {

    private final UserRepository userRepository;

    @Transactional
    public void savePickupDate(String discordId, String pickupDateStr) {
        LocalDateTime pickupDate = parsePickupDate(pickupDateStr);

        if (!pickupDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("픽업 날짜는 현재 시각 이후여야 합니다.");
        }

        User user = userRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        user.updatePickupDate(pickupDate);
    }

    private LocalDateTime parsePickupDate(String input) {
        try {
            int lastSlash = input.lastIndexOf("/");
            if (lastSlash < 1) {
                throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다. (예: 4/23/12:00)");
            }
            String datePart = input.substring(0, lastSlash);
            String timePart = input.substring(lastSlash + 1);

            MonthDay monthDay = MonthDay.parse(datePart, DateTimeFormatter.ofPattern("M/d"));
            LocalTime time = LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HH:mm"));

            return LocalDateTime.of(
                    LocalDate.now()
                            .withMonth(monthDay.getMonthValue())
                            .withDayOfMonth(monthDay.getDayOfMonth()),
                    time
            );
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다. (예: 4/23/12:00)");
        }
    }
}
