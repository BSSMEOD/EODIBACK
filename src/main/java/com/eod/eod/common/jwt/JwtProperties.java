package com.eod.eod.common.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration accessTokenExpiration;

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration refreshTokenExpiration;
}
