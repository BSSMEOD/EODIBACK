package com.eod.eod.domain.auth.application;

import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BsmLoginService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class BsmLoginServiceTest {

    @Mock
    private BsmOAuthService bsmOAuthService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private BsmLoginService bsmLoginService;

    private ObjectMapper objectMapper;

    private static final String TEST_CODE = "test_authorization_code";
    private static final String TEST_BSM_TOKEN = "test_bsm_token";
    private static final String TEST_ACCESS_TOKEN = "test_access_token";
    private static final String TEST_REFRESH_TOKEN = "test_refresh_token";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("신규 사용자 로그인 - 사용자 생성 및 토큰 발급")
    void testLoginWithNewUser() {
        // given
        String providerId = "123456";
        String userCode = "12345";
        String nickname = "홍길동";
        String email = "hong@bssm.hs.kr";

        JsonNode mockResource = createMockBsmUserResource(providerId, userCode, nickname, email);
        BsmOAuthService.ExchangeResult exchangeResult =
                new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, mockResource);

        when(bsmOAuthService.exchangeCode(TEST_CODE, true)).thenReturn(exchangeResult);
        when(userRepository.findByOauthProviderAndOauthId("bsm", providerId))
                .thenReturn(Optional.empty());

        // Mock AuthService.issueTokensForOAuth2Login
        AuthService.TokenPair tokenPair = new AuthService.TokenPair(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);
        when(authService.issueTokensForOAuth2Login(any(User.class)))
                .thenReturn(tokenPair);

        // when
        BsmLoginService.LoginResult result = bsmLoginService.login(TEST_CODE);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(TEST_REFRESH_TOKEN);

        // 사용자 저장 검증
        verify(userRepository, times(1)).save(argThat(user ->
                user.getOauthProvider().equals("bsm") &&
                        user.getOauthId().equals(providerId) &&
                        user.getEmail().equals(email) &&
                        user.getName().equals(nickname) &&
                        user.getStudentCode() != null &&
                        user.getRole() == User.Role.USER
        ));
    }

    @Test
    @DisplayName("기존 사용자 로그인 - 사용자 생성 없이 토큰 발급")
    void testLoginWithExistingUser() {
        // given
        String providerId = "123456";
        String userCode = "12345";
        String nickname = "홍길동";
        String email = "hong@bssm.hs.kr";

        User existingUser = User.builder()
                .email(email)
                .name(nickname)
                .oauthProvider("bsm")
                .oauthId(providerId)
                .role(User.Role.USER)
                .studentCode(120)
                .build();

        JsonNode mockResource = createMockBsmUserResource(providerId, userCode, nickname, email);
        BsmOAuthService.ExchangeResult exchangeResult =
                new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, mockResource);

        when(bsmOAuthService.exchangeCode(TEST_CODE, true)).thenReturn(exchangeResult);
        when(userRepository.findByOauthProviderAndOauthId("bsm", providerId))
                .thenReturn(Optional.of(existingUser));

        // Mock AuthService.issueTokensForOAuth2Login
        AuthService.TokenPair tokenPair = new AuthService.TokenPair(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);
        when(authService.issueTokensForOAuth2Login(any(User.class)))
                .thenReturn(tokenPair);

        // when
        BsmLoginService.LoginResult result = bsmLoginService.login(TEST_CODE);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(TEST_REFRESH_TOKEN);

        // 사용자 저장이 호출되지 않았는지 검증
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 실패 - BSM resource가 null")
    void testLoginFailsWhenResourceIsNull() {
        // given
        BsmOAuthService.ExchangeResult exchangeResult =
                new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, null);

        when(bsmOAuthService.exchangeCode(TEST_CODE, true)).thenReturn(exchangeResult);

        // when & then
        assertThatThrownBy(() -> bsmLoginService.login(TEST_CODE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BSM resource를 가져오지 못했습니다");
    }

    @Test
    @DisplayName("로그인 실패 - BSM resource에 id 없음")
    void testLoginFailsWhenResourceMissingId() {
        // given
        JsonNode mockResource = objectMapper.createObjectNode()
                .put("userCode", "12345")
                .put("nickname", "홍길동")
                .put("email", "hong@bssm.hs.kr");

        BsmOAuthService.ExchangeResult exchangeResult =
                new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, mockResource);

        when(bsmOAuthService.exchangeCode(TEST_CODE, true)).thenReturn(exchangeResult);

        // when & then
        assertThatThrownBy(() -> bsmLoginService.login(TEST_CODE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BSM resource에 id/userCode가 없습니다");
    }

    @Test
    @DisplayName("로그인 실패 - BSM resource에 userCode 없음")
    void testLoginFailsWhenResourceMissingUserCode() {
        // given
        JsonNode mockResource = objectMapper.createObjectNode()
                .put("id", "123456")
                .put("nickname", "홍길동")
                .put("email", "hong@bssm.hs.kr");

        BsmOAuthService.ExchangeResult exchangeResult =
                new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, mockResource);

        when(bsmOAuthService.exchangeCode(TEST_CODE, true)).thenReturn(exchangeResult);

        // when & then
        assertThatThrownBy(() -> bsmLoginService.login(TEST_CODE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BSM resource에 id/userCode가 없습니다");
    }

    @Test
    @DisplayName("로그인 실패 - BSM resource에 email 없음")
    void testLoginFailsWhenResourceMissingEmail() {
        // given
        JsonNode mockResource = objectMapper.createObjectNode()
                .put("id", "123456")
                .put("userCode", "12345")
                .put("nickname", "홍길동");

        BsmOAuthService.ExchangeResult exchangeResult =
                new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, mockResource);

        when(bsmOAuthService.exchangeCode(TEST_CODE, true)).thenReturn(exchangeResult);

        // when & then
        assertThatThrownBy(() -> bsmLoginService.login(TEST_CODE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BSM resource에 email이 없습니다");
    }

    @Test
    @DisplayName("로그인 실패 - BSM OAuth 코드 교환 실패")
    void testLoginFailsWhenCodeExchangeFails() {
        // given
        when(bsmOAuthService.exchangeCode(TEST_CODE, true))
                .thenThrow(new RuntimeException("Code exchange failed"));

        // when & then
        assertThatThrownBy(() -> bsmLoginService.login(TEST_CODE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Code exchange failed");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 생성 시 기본값 확인")
    void testNewUserDefaultValues() {
        // given
        String providerId = "123456";
        String userCode = "12345";
        String nickname = "홍길동";
        String email = "hong@bssm.hs.kr";

        JsonNode mockResource = createMockBsmUserResource(providerId, userCode, nickname, email);
        BsmOAuthService.ExchangeResult exchangeResult =
                new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, mockResource);

        when(bsmOAuthService.exchangeCode(TEST_CODE, true)).thenReturn(exchangeResult);
        when(userRepository.findByOauthProviderAndOauthId("bsm", providerId))
                .thenReturn(Optional.empty());

        // Mock AuthService.issueTokensForOAuth2Login
        AuthService.TokenPair tokenPair = new AuthService.TokenPair(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);
        when(authService.issueTokensForOAuth2Login(any(User.class)))
                .thenReturn(tokenPair);

        // when
        bsmLoginService.login(TEST_CODE);

        // then
        verify(userRepository, times(1)).save(argThat(user ->
                user.getRole() == User.Role.USER && // 기본 역할은 USER
                        user.getOauthProvider().equals("bsm") &&
                        user.getOauthId().equals(providerId)
        ));
    }

    /**
     * Mock BSM 사용자 정보 생성 헬퍼 메서드
     */
    private JsonNode createMockBsmUserResource(String id, String userCode, String nickname, String email) {
        var userNode = objectMapper.createObjectNode()
                .put("id", id)
                .put("nickname", nickname)
                .put("email", email);

        // student 객체 추가 (grade=1, classNo=2 → studentCode=120)
        var studentNode = objectMapper.createObjectNode()
                .put("grade", 1)
                .put("classNo", 2);
        userNode.set("student", studentNode);

        return userNode;
    }
}