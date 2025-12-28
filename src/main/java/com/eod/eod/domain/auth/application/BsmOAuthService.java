package com.eod.eod.domain.auth.application;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * BSM OAuth 인증 서비스
 *
 * BSM(부산소프트웨어마이스터고) OAuth 서버와 통신하여 사용자 인증을 처리합니다.
 * OAuth 2.0 표준을 기반으로 하되, BSM 서버의 커스텀 필드도 지원합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BsmOAuthService {

    @Qualifier("bsmOauthRestClient")
    private final RestClient bsmOauthRestClient;

    @Value("${bsm.auth.client-id}")
    private String clientId;

    @Value("${bsm.auth.client-secret}")
    private String clientSecret;

    @Value("${bsm.oauth.base-url:https://auth.bssm.app}")
    private String baseUrl;

    @Value("${bsm.oauth.redirect-uri:https://www.jojaemin.com/oauth/bsm}")
    private String redirectUri;

    /**
     * BSM OAuth 인증 시작 URL 생성
     *
     * @param state CSRF 방지를 위한 state 파라미터
     * @return BSM OAuth 서버로 리다이렉트할 URL
     */
    public String buildAuthorizeUrl(String state) {
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
        return String.format("%s/oauth?clientId=%s&redirectURI=%s&state=%s", baseUrl, clientId, encodedRedirectUri, encodedState);
    }

    /**
     * 인증 코드를 액세스 토큰으로 교환하고 선택적으로 사용자 정보 조회
     *
     * @param code BSM OAuth 서버로부터 받은 인증 코드
     * @param includeResource 사용자 정보 포함 여부
     * @return BSM 토큰과 사용자 정보
     */
    public ExchangeResult exchangeCode(String code, boolean includeResource) {
        String token = exchangeCodeForToken(code);
        JsonNode resource = includeResource ? fetchUserResource(token).orElse(null) : null;
        return new ExchangeResult(token, resource);
    }

    private String exchangeCodeForToken(String code) {
        log.info("BSM OAuth 토큰 교환 시작 - code: {}...", code.substring(0, Math.min(10, code.length())));
        JsonNode node = null;
        RestClientResponseException lastError = null;

        // 시도 1: 표준 OAuth 2.0 JSON body (권장)
        log.debug("시도 1/6: 표준 OAuth 2.0 JSON body 요청");
        try {
            node = requestTokenWithStandardOAuthFields(code);
            log.info("✅ 성공: 표준 OAuth 2.0 JSON 방식으로 토큰 획득");
        } catch (RestClientResponseException e) {
            lastError = e;
            log.warn("❌ 실패 (시도 1/6): 표준 OAuth 2.0 JSON - {} {}", e.getStatusCode(), e.getMessage());
        }

        // 시도 2: 표준 OAuth 2.0 Form body (가장 일반적)
        if (node == null) {
            log.debug("시도 2/6: 표준 OAuth 2.0 Form body 요청");
            try {
                node = requestTokenWithStandardOAuthFieldsForm(code);
                log.info("✅ 성공: 표준 OAuth 2.0 Form 방식으로 토큰 획득");
            } catch (RestClientResponseException e) {
                lastError = e;
                log.warn("❌ 실패 (시도 2/6): 표준 OAuth 2.0 Form - {} {}", e.getStatusCode(), e.getMessage());
            }
        }

        // 시도 3: BSM 커스텀 JSON (authCode)
        if (node == null) {
            log.debug("시도 3/6: BSM 커스텀 JSON (authCode) 요청");
            try {
                node = requestTokenWithJsonBody(code, true);
                log.info("✅ 성공: BSM 커스텀 JSON (authCode) 방식으로 토큰 획득");
            } catch (RestClientResponseException e) {
                lastError = e;
                log.warn("❌ 실패 (시도 3/6): BSM 커스텀 JSON (authCode) - {} {}", e.getStatusCode(), e.getMessage());
            }
        }

        // 시도 4: BSM 커스텀 JSON (authcode 소문자)
        if (node == null) {
            log.debug("시도 4/6: BSM 커스텀 JSON (authcode) 요청");
            try {
                node = requestTokenWithJsonBody(code, false);
                log.info("✅ 성공: BSM 커스텀 JSON (authcode) 방식으로 토큰 획득");
            } catch (RestClientResponseException e) {
                lastError = e;
                log.warn("❌ 실패 (시도 4/6): BSM 커스텀 JSON (authcode) - {} {}", e.getStatusCode(), e.getMessage());
            }
        }

        // 시도 5: BSM 커스텀 Form (authCode)
        if (node == null) {
            log.debug("시도 5/6: BSM 커스텀 Form (authCode) 요청");
            try {
                node = requestTokenWithFormBody(code, true);
                log.info("✅ 성공: BSM 커스텀 Form (authCode) 방식으로 토큰 획득");
            } catch (RestClientResponseException e) {
                lastError = e;
                log.warn("❌ 실패 (시도 5/6): BSM 커스텀 Form (authCode) - {} {}", e.getStatusCode(), e.getMessage());
            }
        }

        // 시도 6: BSM 커스텀 Form (authcode 소문자)
        if (node == null) {
            log.debug("시도 6/6: BSM 커스텀 Form (authcode) 요청");
            try {
                node = requestTokenWithFormBody(code, false);
                log.info("✅ 성공: BSM 커스텀 Form (authcode) 방식으로 토큰 획득");
            } catch (RestClientResponseException e) {
                lastError = e;
                log.warn("❌ 실패 (시도 6/6): BSM 커스텀 Form (authcode) - {} {}", e.getStatusCode(), e.getMessage());
            }
        }

        // 모든 시도 실패 시 예외 발생
        if (node == null && lastError != null) {
            log.error("❌ 모든 토큰 교환 시도 실패 - 마지막 오류: {} {}, 응답 본문: {}", 
                    lastError.getStatusCode(), 
                    lastError.getMessage(),
                    lastError.getResponseBodyAsString());
            throw new IllegalStateException("모든 토큰 요청 시도가 실패했습니다. 마지막 오류: " + lastError.getMessage(), lastError);
        }

        // 응답에서 토큰 추출
        log.debug("토큰 추출 시작 - 응답: {}", node);
        String token = extractToken(node);
        if (token == null || token.isBlank()) {
            log.error("❌ 토큰 추출 실패 - 응답에 토큰이 없음: {}", node);
            throw new IllegalStateException("BSM 토큰 응답에 토큰이 포함되지 않았습니다: " + node);
        }
        
        log.info("✅ 토큰 교환 완료 - 토큰: {}...", token.substring(0, Math.min(10, token.length())));
        return token;
    }

    /**
     * BSM 커스텀 필드로 JSON body 토큰 요청
     *
     * @param code 인증 코드
     * @param useNewFieldName true: authCode, false: authcode
     * @return 토큰 응답
     */
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

    /**
     * BSM 커스텀 필드로 Form body 토큰 요청
     *
     * @param code 인증 코드
     * @param useNewFieldName true: authCode, false: authcode
     * @return 토큰 응답
     */
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

    private JsonNode requestTokenWithStandardOAuthFields(String code) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("grant_type", "authorization_code");
        payload.put("code", code);
        payload.put("client_id", clientId);
        payload.put("client_secret", clientSecret);
        payload.put("redirect_uri", redirectUri);

        log.debug("표준 OAuth 2.0 JSON 요청 - URI: /api/oauth/token, payload: {grant_type=authorization_code, code=***, client_id={}, redirect_uri={}}", 
                clientId, redirectUri);

        return bsmOauthRestClient.post()
                .uri("/api/oauth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(JsonNode.class);
    }

    private JsonNode requestTokenWithStandardOAuthFieldsForm(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);

        log.debug("표준 OAuth 2.0 Form 요청 - URI: /api/oauth/token, Content-Type: application/x-www-form-urlencoded");

        return bsmOauthRestClient.post()
                .uri("/api/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(JsonNode.class);
    }

    private Optional<JsonNode> fetchUserResource(String token) {
        log.debug("BSM 사용자 정보 조회 시작");
        
        // 시도 1: Bearer 토큰 방식
        log.debug("사용자 정보 조회 - 시도 1: Bearer 토큰 헤더 방식");
        try {
            JsonNode resource = bsmOauthRestClient.get()
                    .uri("/api/oauth/resource")
                    .header("Authorization", "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
            log.info("✅ 사용자 정보 조회 성공 (Bearer 토큰 방식)");
            return Optional.ofNullable(resource);
        } catch (RestClientResponseException e) {
            log.warn("❌ 사용자 정보 조회 실패 (Bearer 토큰) - {} {}", e.getStatusCode(), e.getMessage());
        }

        // 시도 2-4: 쿼리 파라미터 방식 (여러 필드명 시도)
        for (String queryKey : new String[]{"token", "accessToken", "access_token"}) {
            log.debug("사용자 정보 조회 - 쿼리 파라미터 방식: {}=***", queryKey);
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
                log.info("✅ 사용자 정보 조회 성공 (쿼리 파라미터: {})", queryKey);
                return Optional.ofNullable(resource);
            } catch (RestClientResponseException e) {
                log.warn("❌ 사용자 정보 조회 실패 (쿼리: {}) - {} {}", queryKey, e.getStatusCode(), e.getMessage());
            }
        }

        log.warn("⚠️ 모든 사용자 정보 조회 방식 실패");
        return Optional.empty();
    }

    private String extractToken(JsonNode node) {
        if (node == null) {
            log.warn("토큰 추출 실패 - 응답이 null");
            return null;
        }

        // 최상위 레벨에서 토큰 찾기
        String token = firstNonBlankText(node, "token", "accessToken", "access_token");
        if (token != null) {
            log.debug("토큰 추출 성공 - 최상위 레벨에서 발견");
            return token;
        }

        // data 객체 안에서 토큰 찾기
        JsonNode data = node.get("data");
        if (data != null && data.isObject()) {
            token = firstNonBlankText(data, "token", "accessToken", "access_token");
            if (token != null) {
                log.debug("토큰 추출 성공 - data 객체에서 발견");
                return token;
            }
        }

        log.warn("토큰 추출 실패 - 응답에서 토큰 필드를 찾을 수 없음");
        return null;
    }

    /**
     * 주어진 키들 중 첫 번째로 발견되는 비어있지 않은 텍스트 값 반환
     *
     * @param node JSON 노드
     * @param keys 검색할 필드 키들
     * @return 첫 번째로 발견된 텍스트 값 (없으면 null)
     */
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

    /**
     * 토큰 교환 결과
     *
     * @param bsmToken BSM 액세스 토큰
     * @param resource BSM 사용자 정보 (선택적)
     */
    public record ExchangeResult(String bsmToken, JsonNode resource) {}
}
