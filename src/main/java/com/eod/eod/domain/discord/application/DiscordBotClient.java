package com.eod.eod.domain.discord.application;

import com.eod.eod.domain.discord.config.DiscordBotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
            log.debug("Discord bot not configured, skipping notification for {}", discordUserId);
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
                "이오디 디스코드 인증이 성공적으로 완료되었습니다. 이제 서버의 모든 채널에 접근할 수 있습니다.";
    }
}
