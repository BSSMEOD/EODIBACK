package com.eod.eod.domain.discord.application;

import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PickupReminderScheduler {

    private final UserRepository userRepository;
    private final ItemClaimRepository itemClaimRepository;
    private final DiscordBotClient discordBotClient;

    /**
     * 매일 07:00에 실행. pickup_date가 오늘로 설정된 사용자에게 Discord DM 리마인더를 보내고,
     * 발송 후 pickup_date를 NULL로 정리한다.
     */
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void sendPickupReminders() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();

        List<User> users = userRepository.findByPickupDateBetween(startOfToday, endOfToday);
        log.info("픽업 리마인더 스케줄러 시작 - 대상 {}명", users.size());

        int sent = 0;
        for (User user : users) {
            try {
                String discordId = user.getDiscordId();
                if (discordId == null || discordId.isBlank()) {
                    log.warn("픽업 리마인더 스킵 - discord_id 없음 userId={}", user.getId());
                    user.updatePickupDate(null);
                    continue;
                }

                String itemName = itemClaimRepository
                        .findFirstByClaimantIdAndStatusOrderByIdDesc(user.getId(), ItemClaim.ClaimStatus.APPROVED)
                        .map(claim -> claim.getItem().getName())
                        .orElse(null);

                discordBotClient.notifyPickupReminder(discordId, itemName, user.getPickupDate());
                user.updatePickupDate(null);
                sent++;
                log.info("픽업 리마인더 발송 userId={} itemName={}", user.getId(), itemName);
            } catch (Exception e) {
                log.error("픽업 리마인더 발송 실패 userId={}", user.getId(), e);
            }
        }
        log.info("픽업 리마인더 스케줄러 완료 - 총 {}명 발송", sent);
    }
}
