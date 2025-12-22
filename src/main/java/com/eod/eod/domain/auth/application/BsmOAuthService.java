package com.eod.eod.domain.auth.application;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BsmOAuthService {

    @Qualifier("bsmOauthRestClient")
    private final RestClient bsmOauthRestClient;

    @Value("${bsm.auth.client-id}")
    private String clientId;

    @Value("${bsm.auth.client-secret}")
    private String clientSecret;

    @Value("${bsm.oauth.base-url:https://auth.bssm.kro.kr}")
    private String baseUrl;

    @Value("${bsm.oauth.redirect-uri:https://www.jojaemin.com/oauth/bsm}")
    private String redirectUri;

    public String buildAuthorizeUrl(String state) {
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
        return String.format("%s/oauth?clientId=%s&redirectURI=%s&state=%s", baseUrl, clientId, encodedRedirectUri, encodedState);
    }

    public ExchangeResult exchangeCode(String code, boolean includeResource) {
        String token = exchangeCodeForToken(code);
        JsonNode resource = includeResource ? fetchUserResource(token).orElse(null) : null;
        return new ExchangeResult(token, resource);
    }

    private String exchangeCodeForToken(String code) {
        JsonNode node;
        try {
            node = requestTokenWithJsonBody(code, true);
        } catch (RestClientResponseException firstError) {
            try {
                node = requestTokenWithJsonBody(code, false);
            } catch (RestClientResponseException secondError) {
                try {
                    node = requestTokenWithFormBody(code, true);
                } catch (RestClientResponseException thirdError) {
                    node = requestTokenWithFormBody(code, false);
                }
            }
        }

        String token = extractToken(node);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("BSM token response did not include a token: " + node);
        }
        return token;
    }

    private JsonNode requestTokenWithJsonBody(String code, boolean useNewFieldName) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("clientId", clientId);
        payload.put("clientSecret", clientSecret);
        payload.put(useNewFieldName ? "authCode" : "authcode", code);

        return bsmOauthRestClient.post()
                .uri("/api/oauth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(JsonNode.class);
    }

    private JsonNode requestTokenWithFormBody(String code, boolean useNewFieldName) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("clientId", clientId);
        form.add("clientSecret", clientSecret);
        form.add(useNewFieldName ? "authCode" : "authcode", code);

        return bsmOauthRestClient.post()
                .uri("/api/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(JsonNode.class);
    }

    private Optional<JsonNode> fetchUserResource(String token) {
        try {
            JsonNode resource = bsmOauthRestClient.get()
                    .uri("/api/oauth/resource")
                    .header("Authorization", "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
            return Optional.ofNullable(resource);
        } catch (RestClientResponseException ignored) {
            // Try alternative styles below
        }

        for (String queryKey : new String[]{"token", "accessToken", "access_token"}) {
            try {
                String uri = UriComponentsBuilder.fromPath("/api/oauth/resource")
                        .queryParam(queryKey, token)
                        .build()
                        .toUriString();
                JsonNode resource = bsmOauthRestClient.get()
                        .uri(uri)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(JsonNode.class);
                return Optional.ofNullable(resource);
            } catch (RestClientResponseException ignored) {
                // continue
            }
        }

        return Optional.empty();
    }

    private String extractToken(JsonNode node) {
        if (node == null) return null;

        String token = firstNonBlankText(node, "token", "accessToken", "access_token");
        if (token != null) return token;

        JsonNode data = node.get("data");
        if (data != null && data.isObject()) {
            return firstNonBlankText(data, "token", "accessToken", "access_token");
        }

        return null;
    }

    private String firstNonBlankText(JsonNode node, String... keys) {
        if (node == null) return null;
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && value.isTextual()) {
                String text = value.asText();
                if (text != null && !text.isBlank()) return text;
            }
        }
        return null;
    }

    public record ExchangeResult(String bsmToken, JsonNode resource) {}
}
