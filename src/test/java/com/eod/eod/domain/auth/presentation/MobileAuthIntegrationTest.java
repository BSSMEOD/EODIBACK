package com.eod.eod.domain.auth.presentation;

import com.eod.eod.domain.auth.application.BsmOAuthService;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MobileAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private BsmOAuthService bsmOAuthService;

    private static final String TEST_CODE = "mobile_auth_code";
    private static final String TEST_BSM_TOKEN = "mobile_bsm_access_token";
    private static final String REDIRECT_URI = "eodi-dev://auth/callback";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("모바일 BSM 콜백은 앱 딥링크로 oneTimeToken을 전달하고 exchange는 앱 토큰을 반환한다")
    void mobileBsmCallbackIssuesOneTimeTokenAndExchangeReturnsTokens() throws Exception {
        String authorizeUrl = "https://auth.bssm.app/oauth?clientId=test&redirectURI=http://localhost:8080/oauth/bsm&state=generated";
        when(bsmOAuthService.buildAuthorizeUrl(eq("generated"))).thenReturn(authorizeUrl);
        when(bsmOAuthService.exchangeCode(eq(TEST_CODE), eq(true)))
                .thenReturn(new BsmOAuthService.ExchangeResult(TEST_BSM_TOKEN, createMockBsmUserResource("123456", "홍길동", "hong@bssm.hs.kr", "STUDENT")));

        mockMvc.perform(get("/auth/mobile/bsm/authorize")
                        .param("redirectUri", REDIRECT_URI)
                        .param("state", "generated"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(authorizeUrl));

        MvcResult callbackResult = mockMvc.perform(get("/oauth/bsm")
                        .param("code", TEST_CODE)
                        .param("state", "generated"))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString(REDIRECT_URI + "?oneTimeToken=")))
                .andReturn();

        String oneTimeToken = queryParam(callbackResult.getResponse().getHeader("Location"), "oneTimeToken");
        assertThat(oneTimeToken).isNotBlank();

        mockMvc.perform(post("/auth/mobile/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"oneTimeToken":"%s"}
                                """.formatted(oneTimeToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("hong@bssm.hs.kr"))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.studentCode").value(1203));
    }

    @Test
    @DisplayName("모바일 refresh는 body refreshToken으로 access/refresh/user를 재발급한다")
    void mobileRefreshReturnsNewTokenPairAndUser() throws Exception {
        User user = saveUser(User.Role.USER);
        String refreshToken = issueRefreshTokenByDevLogin(user.getRole());

        mockMvc.perform(post("/auth/mobile/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    @DisplayName("auth me는 Bearer access token의 현재 사용자를 반환한다")
    void meReturnsCurrentUser() throws Exception {
        saveUser(User.Role.TEACHER);
        String accessToken = issueAccessTokenByDevLogin(User.Role.TEACHER);

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andExpect(jsonPath("$.email").value("dev-teacher@eodi.local"));
    }

    @Test
    @DisplayName("dev login은 test/dev 환경에서 지정 role의 앱 토큰을 발급한다")
    void devLoginReturnsRoleScopedMobileTokenResponse() throws Exception {
        mockMvc.perform(post("/auth/dev-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"ADMIN"}
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andExpect(jsonPath("$.user.email").value("dev-admin@eodi.local"));
    }

    private String issueAccessTokenByDevLogin(User.Role role) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/dev-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"%s"}
                                """.formatted(role.name())))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String issueRefreshTokenByDevLogin(User.Role role) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/dev-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"%s"}
                                """.formatted(role.name())))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("refreshToken").asText();
    }

    private User saveUser(User.Role role) {
        return userRepository.save(User.builder()
                .email("dev-" + role.name().toLowerCase() + "@eodi.local")
                .name("Dev " + role.name())
                .oauthProvider("dev")
                .oauthId(role.name().toLowerCase())
                .role(role)
                .grade(role == User.Role.USER ? 1 : null)
                .classNo(role == User.Role.USER ? 2 : null)
                .studentNo(role == User.Role.USER ? 3 : null)
                .isGraduate(false)
                .build());
    }

    private JsonNode createMockBsmUserResource(String id, String name, String email, String role) {
        var userNode = objectMapper.createObjectNode()
                .put("id", id)
                .put("name", name)
                .put("email", email)
                .put("role", role)
                .put("isGraduate", false)
                .put("grade", 1)
                .put("classNo", 2)
                .put("studentNo", 3);

        return objectMapper.createObjectNode().set("user", userNode);
    }

    private String queryParam(String location, String name) {
        String query = URI.create(location).getRawQuery();
        for (String part : query.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && pair[0].equals(name)) {
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
