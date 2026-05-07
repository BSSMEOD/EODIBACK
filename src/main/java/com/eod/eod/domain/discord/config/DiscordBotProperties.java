package com.eod.eod.domain.discord.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "discord.bot")
public class DiscordBotProperties {
    private String token;
    private String guildId;
    private String verifiedRoleId;
    private List<String> staffNotificationIds = new ArrayList<>();

    public boolean isConfigured() {
        return isTokenConfigured()
                && guildId != null && !guildId.isBlank()
                && verifiedRoleId != null && !verifiedRoleId.isBlank();
    }

    public boolean isTokenConfigured() {
        return token != null && !token.isBlank();
    }

    public List<String> getActiveStaffNotificationIds() {
        if (staffNotificationIds == null) {
            return Collections.emptyList();
        }
        return staffNotificationIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .map(String::trim)
                .toList();
    }
}
