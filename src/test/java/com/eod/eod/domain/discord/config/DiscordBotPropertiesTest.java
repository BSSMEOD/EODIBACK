package com.eod.eod.domain.discord.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordBotPropertiesTest {

    @Test
    void getActiveStaffNotificationIds는_콤마로_파싱된_ID들을_반환한다() {
        DiscordBotProperties properties = new DiscordBotProperties();
        properties.setStaffNotificationIds(Arrays.asList("111111111111111111", "222222222222222222"));

        List<String> ids = properties.getActiveStaffNotificationIds();

        assertThat(ids).containsExactly("111111111111111111", "222222222222222222");
    }

    @Test
    void getActiveStaffNotificationIds는_공백과_빈문자열을_제외한다() {
        DiscordBotProperties properties = new DiscordBotProperties();
        properties.setStaffNotificationIds(Arrays.asList("111", "", "  ", null, "222"));

        List<String> ids = properties.getActiveStaffNotificationIds();

        assertThat(ids).containsExactly("111", "222");
    }

    @Test
    void getActiveStaffNotificationIds는_각_ID의_앞뒤_공백을_제거한다() {
        DiscordBotProperties properties = new DiscordBotProperties();
        properties.setStaffNotificationIds(Arrays.asList("  111  ", "\t222\n"));

        List<String> ids = properties.getActiveStaffNotificationIds();

        assertThat(ids).containsExactly("111", "222");
    }

    @Test
    void staffNotificationIds가_null이면_빈_리스트를_반환한다() {
        DiscordBotProperties properties = new DiscordBotProperties();
        properties.setStaffNotificationIds(null);

        assertThat(properties.getActiveStaffNotificationIds()).isEmpty();
    }

    @Test
    void staffNotificationIds_기본값은_빈_리스트다() {
        DiscordBotProperties properties = new DiscordBotProperties();

        assertThat(properties.getActiveStaffNotificationIds()).isEmpty();
    }
}
