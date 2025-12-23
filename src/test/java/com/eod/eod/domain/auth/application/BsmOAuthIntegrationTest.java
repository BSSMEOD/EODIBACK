package com.eod.eod.domain.auth.application;

import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BSM OAuth 통합 테스트
 *
 * 테스트 범위:
 * 1. 인증 시작 (/auth/oauth/bsm/authorize)
 * 2. 콜백 처리 (/oauth/bsm)
 * 3. 에러 케이스 (state 불일치, 인증 실패 등)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BsmOAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private BsmOAuthService bsmOAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_STATE = "test_state_value_12345";
    private static final String TEST_CODE = "test_authorization_code";
    private static final String TEST_BSM_TOKEN = "test_bsm_access_token";

    @BeforeEach
    void setUp() {
        // 기존 테스트 사용자 정리
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("인증 시작 - BSM 로그인 페이지로 리다이렉트")
    void testAuthorizeRedirectsToBsm() throws Exception {
        // given
        String authorizeUrl = "https://auth.bssm.kro.kr/oauth?clientId=test&redirectURI=http://localhost/callback&state=" + TEST_STATE;
        when(bsmOAuthService.buildAuthorizeUrl(anyString())).thenReturn(authorizeUrl);

        // when & then
        mockMvc.perform(get("/auth/oauth/bsm/authorize"))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("auth.bssm.kro.kr/oauth")))
                .andExpect(header().string("Location", containsString("clientId=")))
                .andExpect(header().string("Location", containsString("redirectURI=")))
                .andExpect(header().string("Location", containsString("state=")))
                .andExpect(cookie().exists("bsm_oauth_state"))
                .andExpect(cookie().maxAge("bsm_oauth_state", 300)); // 5분
    }

    @Test
    @DisplayName("콜백 성공 - 신규 사용자 생성 및 토큰 발급")
    void testCallbackSuccessWithNewUser() throws Exception {
        // given
        JsonNode mockUserResource = createMockBsmUserResource(
                "123456",
                "12345",
                "홍길동",
                "hong@bssm.hs.kr"
        );

        BsmOAuthService.ExchangeResult exchangeResult =
                new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, mockUserResource);

        when(bsmOAuthService.exchangeCode(eq(TEST_CODE), eq(true)))
                .thenReturn(exchangeResult);

        // when & then
        mockMvc.perform(get("/oauth/bsm")
                        .param("code", TEST_CODE)
                        .param("state", TEST_STATE)
                        .cookie(new Cookie("bsm_oauth_state", TEST_STATE)))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("auth/callback")))
                .andExpect(header().string("Location", containsString("#token=")))
                .andExpect(header().string("Location", containsString("&provider=bsm")))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true));

        // 사용자가 DB에 저장되었는지 확인
        User savedUser = userRepository.findByOauthProviderAndOauthId("bsm", "123456")
                .orElseThrow(() -> new AssertionError("User should be saved"));

        assert savedUser.getEmail().equals("hong@bssm.hs.kr");
        assert savedUser.getName().equals("홍길동");
        assert savedUser.getRole() == User.Role.USER;
        assert savedUser.getGrade().equals(1);
        assert savedUser.getClassNo().equals(2);
        assert savedUser.getStudentNo().equals(3);
        assert savedUser.getIsGraduate().equals(false);
    }

    @Test
    @DisplayName("콜백 성공 - 기존 사용자 로그인")
    void testCallbackSuccessWithExistingUser() throws Exception {
        // given - 기존 사용자 생성
        User existingUser = User.builder()
                .email("existing@bssm.hs.kr")
                .name("기존사용자")
                .oauthProvider("bsm")
                .oauthId("999999")
                .role(User.Role.USER)
                .grade(1)
                .classNo(2)
                .studentNo(3)
                .isGraduate(false)
                .build();
        userRepository.save(existingUser);

        JsonNode mockUserResource = createMockBsmUserResource(
                "999999",
                "99999",
                "기존사용자",
                "existing@bssm.hs.kr"
        );

        BsmOAuthService.ExchangeResult exchangeResult =
                new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, mockUserResource);

        when(bsmOAuthService.exchangeCode(eq(TEST_CODE), eq(true)))
                .thenReturn(exchangeResult);

        // when & then
        mockMvc.perform(get("/oauth/bsm")
                        .param("code", TEST_CODE)
                        .param("state", TEST_STATE)
                        .cookie(new Cookie("bsm_oauth_state", TEST_STATE)))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("auth/callback")))
                .andExpect(header().string("Location", containsString("#token=")))
                .andExpect(cookie().exists("refreshToken"));

        // 사용자가 중복 생성되지 않았는지 확인
        long userCount = userRepository.findAll().stream()
                .filter(u -> u.getOauthId().equals("999999"))
                .count();
        assert userCount == 1 : "User should not be duplicated";
    }

    @Test
    @DisplayName("콜백 실패 - state 불일치")
    void testCallbackFailsWithStateMismatch() throws Exception {
        // given
        String wrongState = "wrong_state_value";
        String correctState = "correct_state_value";

        // when & then
        mockMvc.perform(get("/oauth/bsm")
                        .param("code", TEST_CODE)
                        .param("state", wrongState)
                        .cookie(new Cookie("bsm_oauth_state", correctState)))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("error=state_mismatch")))
                .andExpect(cookie().maxAge("bsm_oauth_state", 0)); // 쿠키 삭제됨
    }

    @Test
    @DisplayName("콜백 실패 - state 쿠키 없음")
    void testCallbackFailsWithoutStateCookie() throws Exception {
        // when & then
        mockMvc.perform(get("/oauth/bsm")
                        .param("code", TEST_CODE)
                        .param("state", TEST_STATE))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("error=state_mismatch")));
    }

    @Test
    @DisplayName("콜백 실패 - state 파라미터 없음")
    void testCallbackFailsWithoutStateParam() throws Exception {
        // when & then
        mockMvc.perform(get("/oauth/bsm")
                        .param("code", TEST_CODE)
                        .cookie(new Cookie("bsm_oauth_state", TEST_STATE)))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("error=state_mismatch")));
    }

    @Test
    @DisplayName("콜백 실패 - BSM 토큰 교환 실패")
    void testCallbackFailsWhenTokenExchangeFails() throws Exception {
        // given
        when(bsmOAuthService.exchangeCode(eq(TEST_CODE), eq(true)))
                .thenThrow(new RuntimeException("Token exchange failed"));

        // when & then
        mockMvc.perform(get("/oauth/bsm")
                        .param("code", TEST_CODE)
                        .param("state", TEST_STATE)
                        .cookie(new Cookie("bsm_oauth_state", TEST_STATE)))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("error=bsm_oauth_failed")))
                .andExpect(cookie().maxAge("bsm_oauth_state", 0)); // 쿠키 삭제됨
    }

    @Test
    @DisplayName("콜백 실패 - BSM 사용자 정보 없음")
    void testCallbackFailsWhenUserResourceIsNull() throws Exception {
        // given
        BsmOAuthService.ExchangeResult exchangeResult =
                new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, null);

        when(bsmOAuthService.exchangeCode(eq(TEST_CODE), eq(true)))
                .thenReturn(exchangeResult);

        // when & then
        mockMvc.perform(get("/oauth/bsm")
                        .param("code", TEST_CODE)
                        .param("state", TEST_STATE)
                        .cookie(new Cookie("bsm_oauth_state", TEST_STATE)))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("error=bsm_oauth_failed")));
    }

    @Test
    @DisplayName("콜백 실패 - BSM 사용자 정보에 필수 필드 누락 (id)")
    void testCallbackFailsWhenUserResourceMissingId() throws Exception {
        // given
        JsonNode mockUserResource = objectMapper.createObjectNode()
                .put("userCode", "12345")
                .put("nickname", "테스트")
                .put("email", "test@bssm.hs.kr");

        BsmOAuthService.ExchangeResult exchangeResult =
                new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, mockUserResource);

        when(bsmOAuthService.exchangeCode(eq(TEST_CODE), eq(true)))
                .thenReturn(exchangeResult);

        // when & then
        mockMvc.perform(get("/oauth/bsm")
                        .param("code", TEST_CODE)
                        .param("state", TEST_STATE)
                        .cookie(new Cookie("bsm_oauth_state", TEST_STATE)))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("error=bsm_oauth_failed")));
    }

    @Test
    @DisplayName("콜백 실패 - BSM 사용자 정보에 필수 필드 누락 (email)")
    void testCallbackFailsWhenUserResourceMissingEmail() throws Exception {
        // given
        JsonNode mockUserResource = objectMapper.createObjectNode()
                .put("id", "123456")
                .put("userCode", "12345")
                .put("nickname", "테스트");

        BsmOAuthService.ExchangeResult exchangeResult =
                new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, mockUserResource);

        when(bsmOAuthService.exchangeCode(eq(TEST_CODE), eq(true)))
                .thenReturn(exchangeResult);

        // when & then
        mockMvc.perform(get("/oauth/bsm")
                        .param("code", TEST_CODE)
                        .param("state", TEST_STATE)
                        .cookie(new Cookie("bsm_oauth_state", TEST_STATE)))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("error=bsm_oauth_failed")));
    }

    /**
     * Mock BSM 사용자 정보 생성 헬퍼 메서드
     */
    private JsonNode createMockBsmUserResource(String id, String userCode, String nickname, String email) {
        var userNode = objectMapper.createObjectNode()
                .put("id", id)
                .put("nickname", nickname)
                .put("email", email);

        // student 객체 추가
        var studentNode = objectMapper.createObjectNode()
                .put("grade", 1)
                .put("classNo", 2)
                .put("studentNo", 3)
                .put("isGraduate", false);
        userNode.set("student", studentNode);

        return userNode;
    }
}
