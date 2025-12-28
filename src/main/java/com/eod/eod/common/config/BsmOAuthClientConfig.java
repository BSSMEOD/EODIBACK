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
            @Value("${bsm.oauth.base-url:https://api-auth.bssm.app}") String baseUrl
    ) {
        // BSM OAuth API Base URL: https://api-auth.bssm.app/api/oauth
        return builder.baseUrl(baseUrl + "/api/oauth").build();
    }
}
