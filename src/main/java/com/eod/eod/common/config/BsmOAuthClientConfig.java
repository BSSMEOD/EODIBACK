package com.eod.eod.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BsmOAuthClientConfig {

    @Bean("bsmOauthRestClient")
    public RestClient bsmOauthRestClient(
            RestClient.Builder builder,
            @Value("${bsm.oauth.base-url:https://auth.bssm.kro.kr}") String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }
}
