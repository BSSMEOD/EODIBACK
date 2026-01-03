package com.eod.eod.domain.auth.application;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

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

    @Value("${bsm.oauth.base-url:https://api-auth.bssm.app}")
    private String baseUrl;

    @Value("${bsm.oauth.redirect-uri:https://www.jojaemin.com/oauth/bsm}")
    private String redirectUri;

    /**
     * BSM OAuth 인증 시작 URL 생성
     *
     * 인증 시작은 https://auth.bssm.app/oauth 사용
     * (API 호출과 다른 도메인)
     *
     * @param state CSRF 방지를 위한 state 파라미터
     * @return BSM OAuth 서버로 리다이렉트할 URL
     */
    public String buildAuthorizeUrl(String state) {
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
        
        // BSM OAuth scope: id는 필수, 나머지는 학생 정보
        String scope = "id+email+name+isGraduate+grade+classNo+studentNo";
        
        // 인증 시작 URL은 auth.bssm.app 사용
        return String.format("https://auth.bssm.app/oauth?clientId=%s&redirectURI=%s&state=%s&scope=%s", 
                clientId, encodedRedirectUri, encodedState, scope);
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
        JsonNode node = requestTokenWithJsonBody(code, true);

        String token = extractToken(node);
        if (token == null || token.isBlank()) {
            log.error("❌ 토큰 추출 실패 - 응답에 토큰이 없음: {}", node);
            throw new IllegalStateException("BSM 토큰 응답에 토큰이 포함되지 않았습니다: " + node);
        }

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
                .uri("/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(JsonNode.class);
    }

    private Optional<JsonNode> fetchUserResource(String token) {
        log.debug("BSM 사용자 정보 조회 시작");
        
        try {
            // BSM 공식 문서: POST /resource with JSON body
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("clientId", clientId);
            payload.put("clientSecret", clientSecret);
            payload.put("token", token);
            
            log.debug("사용자 정보 조회 - POST /resource (clientId={}, token={}...)", 
                    clientId, token.substring(0, Math.min(10, token.length())));
            
            JsonNode resource = bsmOauthRestClient.post()
                    .uri("/resource")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
                    
            log.info("✅ 사용자 정보 조회 성공");
            return Optional.ofNullable(resource);
        } catch (RestClientResponseException e) {
            log.error("❌ 사용자 정보 조회 실패 - {} {}, 응답: {}", 
                    e.getStatusCode(), e.getMessage(), e.getResponseBodyAsString());
            return Optional.empty();
        }
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
