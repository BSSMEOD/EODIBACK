package com.eod.eod.domain.discord.application;

import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.model.ItemClaim;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PickupReminderScheduler {

    private final ItemClaimRepository itemClaimRepository;
    private final DiscordBotClient discordBotClient;

    /**
     * 매일 13:10에 실행. visit_date가 오늘인 승인된 클레임 대상자에게 Discord DM 리마인더를 보낸다.
     */
    @Scheduled(cron = "0 10 13 * * *")
    public void sendPickupReminders() {
        LocalDate today = LocalDate.now();
        LocalDateTime pickupTime = today.atTime(13, 10);
        List<ItemClaim> claims = itemClaimRepository.findByStatusAndVisitDate(ItemClaim.ClaimStatus.APPROVED, today);
        log.info("픽업 리마인더 스케줄러 시작 - 대상 {}건", claims.size());

        int sent = 0;
        for (ItemClaim claim : claims) {
            try {
                String discordId = claim.getClaimant().getDiscordId();
                if (discordId == null || discordId.isBlank()) {
                    log.warn("픽업 리마인더 스킵 - discord_id 없음 claimId={} userId={}", claim.getId(), claim.getClaimant().getId());
                    continue;
                }
                discordBotClient.notifyPickupReminder(discordId, claim.getItem().getName(), pickupTime);
                sent++;
                log.info("픽업 리마인더 발송 claimId={} userId={} itemName={}", claim.getId(), claim.getClaimant().getId(), claim.getItem().getName());
            } catch (Exception e) {
                log.error("픽업 리마인더 발송 실패 claimId={}", claim.getId(), e);
            }
        }
        log.info("픽업 리마인더 스케줄러 완료 - 총 {}건 발송", sent);
    }
}
