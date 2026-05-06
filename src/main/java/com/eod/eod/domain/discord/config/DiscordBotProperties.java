package com.eod.eod.domain.discord.config;

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

    public boolean isConfigured() {
        return isTokenConfigured()
                && guildId != null && !guildId.isBlank()
                && verifiedRoleId != null && !verifiedRoleId.isBlank();
    }

    public boolean isTokenConfigured() {
        return token != null && !token.isBlank();
    }
}
