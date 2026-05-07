package com.eod.eod.domain.discord.application;

import com.eod.eod.domain.discord.config.DiscordBotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
public class DiscordBotClient {

    private static final String DISCORD_API_BASE = "https://discord.com/api/v10";

    private final WebClient webClient;
    private final DiscordBotProperties properties;

    public DiscordBotClient(DiscordBotProperties properties, WebClient.Builder builder) {
        this.properties = properties;
        this.webClient = builder
                .baseUrl(DISCORD_API_BASE)
                .defaultHeader("Authorization", "Bot " + properties.getToken())
                .build();
    }

    /**
     * 인증 완료 후 Discord 역할 부여 + DM 발송 (비동기 fire-and-forget)
     */
    public void notifyVerified(String discordUserId, String studentName) {
        if (!properties.isConfigured()) {
            log.warn("Discord bot not configured (token/guild/role 중 하나 비어있음), skipping notification for {}", discordUserId);
            return;
        }

        grantRole(discordUserId)
                .doOnSuccess(v -> log.info("Discord role granted to {}", discordUserId))
                .doOnError(e -> log.warn("Failed to grant Discord role to {}: {}", discordUserId, e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();

        sendDm(discordUserId, buildDmMessage(studentName))
                .doOnSuccess(v -> log.info("Discord DM sent to {}", discordUserId))
                .doOnError(e -> log.warn("Failed to send Discord DM to {}: {}", discordUserId, e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }

    private Mono<Void> grantRole(String discordUserId) {
        return webClient.put()
                .uri("/guilds/{guildId}/members/{userId}/roles/{roleId}",
                        properties.getGuildId(), discordUserId, properties.getVerifiedRoleId())
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    private Mono<Void> sendDm(String discordUserId, String message) {
        return createDmChannel(discordUserId)
                .flatMap(channelId -> sendMessage(channelId, message));
    }

    private Mono<String> createDmChannel(String discordUserId) {
        return webClient.post()
                .uri("/users/@me/channels")
                .bodyValue(Map.of("recipient_id", discordUserId))
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> (String) body.get("id"));
    }

    private Mono<Void> sendMessage(String channelId, String message) {
        return webClient.post()
                .uri("/channels/{channelId}/messages", channelId)
                .bodyValue(Map.of("content", message))
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    private String buildDmMessage(String studentName) {
        return "✅ **인증이 완료되었습니다!**\n" +
                "안녕하세요, **" + studentName + "**님!\n" +
                "어디 디스코드 인증이 성공적으로 완료되었습니다. 이제 서버의 모든 채널에 접근할 수 있습니다.";
    }

    /**
     * 소유권 주장 승인 알림 (DM 만)
     */
    public void notifyClaimApproved(String discordUserId, String itemName, LocalDate visitDate) {
        if (!properties.isTokenConfigured()) {
            log.warn("Discord bot token 미설정, claim approval DM 스킵 (discordId={})", discordUserId);
            return;
        }
        String visitSchedule = visitDate != null
                ? visitDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + " 13:10"
                : "방문일 13:10";
        String message = "✅ **소유권 주장 승인**\n" +
                "**" + safe(itemName) + "**에 대한 소유권 주장이 승인되었습니다.\n" +
                "방문 예정 시각은 **" + visitSchedule + "** 입니다.";
        sendDmFireAndForget(discordUserId, message, "claim approval");
    }

    /**
     * 소유권 주장 거절 알림 (DM 만)
     */
    public void notifyClaimRejected(String discordUserId, String itemName) {
        if (!properties.isTokenConfigured()) {
            log.warn("Discord bot token 미설정, claim rejection DM 스킵 (discordId={})", discordUserId);
            return;
        }
        String message = "❌ **소유권 주장 거절**\n" +
                "**" + safe(itemName) + "**에 대한 소유권 주장이 거절되었습니다.";
        sendDmFireAndForget(discordUserId, message, "claim rejection");
    }

    /**
     * 스태프(2명)에게 승인된 픽업 일정 DM 발송
     */
    public void notifyStaffPickupScheduled(Integer studentCode, String studentName, LocalDate visitDate, String itemName) {
        if (!properties.isTokenConfigured()) {
            log.warn("Discord bot token 미설정, staff pickup notification 스킵");
            return;
        }
        java.util.List<String> staffIds = properties.getActiveStaffNotificationIds();
        if (staffIds.isEmpty()) {
            log.warn("Discord staff notification IDs 미설정, staff pickup notification 스킵");
            return;
        }
        String visitDateStr = visitDate != null
                ? visitDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "방문일";
        String studentCodeStr = studentCode != null ? studentCode.toString() : "학번 미등록";
        String safeName = studentName == null || studentName.isBlank() ? "이름 미등록" : studentName;
        String message = "📦 **픽업 예정 알림**\n" +
                "**" + studentCodeStr + " " + safeName + "** 학생이 **" +
                visitDateStr + " 13:10**에 **" + safe(itemName) + "**을(를) 찾으러 옵니다.";
        for (String staffId : staffIds) {
            sendDmFireAndForget(staffId, message, "staff pickup notification");
        }
    }

    /**
     * 픽업 당일 아침 리마인더 (DM 만)
     */
    public void notifyPickupReminder(String discordUserId, String itemName, LocalDateTime pickupTime) {
        if (!properties.isTokenConfigured()) {
            log.warn("Discord bot token 미설정, pickup reminder DM 스킵 (discordId={})", discordUserId);
            return;
        }
        String timeStr = pickupTime != null
                ? pickupTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                : "예정된 시간";
        String message = "🔔 **오늘 픽업 예정**\n" +
                "오늘 **" + timeStr + "**에 **" + safe(itemName) + "**를 찾으러 와주세요.";
        sendDmFireAndForget(discordUserId, message, "pickup reminder");
    }

    private void sendDmFireAndForget(String discordUserId, String message, String purpose) {
        sendDm(discordUserId, message)
                .doOnSuccess(v -> log.info("Discord DM sent to {} ({})", discordUserId, purpose))
                .doOnError(e -> log.warn("Failed to send Discord DM to {} ({}): {}", discordUserId, purpose, e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "분실물" : value;
    }
}
