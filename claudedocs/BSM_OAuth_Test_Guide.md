# BSM OAuth 테스트 가이드

## 개요
이 문서는 EODI 프로젝트의 BSM OAuth 인증 흐름을 테스트하는 방법을 설명합니다.

## 전체 인증 흐름

```
사용자 → Backend → BSM 인증 서버 → Backend → Frontend
```

### 1단계: 인증 시작
- **엔드포인트**: `GET /auth/oauth/bsm/authorize`
- **동작**:
  - 32바이트 랜덤 state 생성
  - state를 쿠키에 저장 (`bsm_oauth_state`, 5분 유효)
  - BSM 인증 URL로 302 리다이렉트

### 2단계: BSM 인증
- **URL**: `https://auth.bssm.kro.kr/oauth?clientId={clientId}&redirectURI={redirectUri}&state={state}`
- **동작**:
  - 사용자가 BSM 계정으로 로그인
  - 인증 성공 시 콜백 URL로 리다이렉트

### 3단계: 콜백 처리
- **엔드포인트**: `GET /oauth/bsm?code={code}&state={state}`
- **동작**:
  1. state 검증 (CSRF 방지)
  2. authorization code → access token 교환
  3. access token으로 사용자 정보 조회
  4. 사용자 찾기/생성
  5. JWT 토큰 생성
  6. 프론트엔드로 리다이렉트

## 수동 테스트 (브라우저)

### 사전 준비
```bash
# 1. 환경 변수 설정
export BSM_CLIENT_ID="your_client_id"
export BSM_CLIENT_SECRET="your_client_secret"
export BSM_REDIRECT_URI="https://www.jojaemin.com/oauth/bsm"
export FRONTEND_BASE_URL="http://localhost:3000"
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/eod"
export SPRING_DATASOURCE_USERNAME="eod_user"
export SPRING_DATASOURCE_PASSWORD="eod_password"
export JWT_SECRET="your_jwt_secret_min_32_chars"

# 2. 애플리케이션 실행
./gradlew bootRun
```

### 테스트 절차

#### 1. 인증 시작 테스트
```bash
# 브라우저에서 접속
http://localhost:8080/auth/oauth/bsm/authorize

# 예상 결과:
# - BSM 로그인 페이지로 리다이렉트
# - 쿠키에 bsm_oauth_state 저장됨
```

#### 2. 전체 흐름 테스트
1. 브라우저에서 `http://localhost:8080/auth/oauth/bsm/authorize` 접속
2. BSM 로그인 페이지에서 인증
3. 콜백 URL로 리다이렉트 확인
4. 프론트엔드 URL로 최종 리다이렉트 확인
5. URL fragment에 `token={accessToken}&provider=bsm` 포함 확인

#### 3. 쿠키 확인
개발자 도구 → Application → Cookies에서 확인:
- `bsm_oauth_state`: 인증 시작 시 생성 (5분 후 삭제)
- `refreshToken`: 인증 완료 후 생성 (7일 유효)

## API 테스트 (Postman/cURL)

### 주의사항
BSM OAuth는 **브라우저 기반 흐름**이므로 Postman/cURL로 완전한 테스트는 어렵습니다.
state 쿠키와 리다이렉트가 필요하기 때문입니다.

### 부분 테스트 (콜백만)

```bash
# 실제 BSM에서 받은 code와 state를 사용해야 합니다
curl -X GET "http://localhost:8080/oauth/bsm?code=ACTUAL_CODE&state=ACTUAL_STATE" \
  -H "Cookie: bsm_oauth_state=ACTUAL_STATE" \
  -v
```

## 통합 테스트 (자동화)

### MockMvc를 사용한 통합 테스트

테스트 파일 위치: `src/test/java/com/eod/eod/domain/auth/application/BsmOAuthIntegrationTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
class BsmOAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BsmOAuthService bsmOAuthService;

    @Test
    void 인증_시작_테스트() throws Exception {
        // given
        String expectedState = "test_state_value";
        String authorizeUrl = "https://auth.bssm.kro.kr/oauth?clientId=test&state=" + expectedState;

        when(bsmOAuthService.buildAuthorizeUrl(anyString())).thenReturn(authorizeUrl);

        // when & then
        mockMvc.perform(get("/auth/oauth/bsm/authorize"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("auth.bssm.kro.kr")))
                .andExpect(cookie().exists("bsm_oauth_state"));
    }

    @Test
    void 콜백_성공_테스트() throws Exception {
        // given
        String code = "test_code";
        String state = "test_state";

        BsmOAuthService.ExchangeResult mockResult = new BsmOAuthService.ExchangeResult(
            "bsm_access_token",
            createMockUserResource()
        );

        when(bsmOAuthService.exchangeCode(eq(code), eq(true))).thenReturn(mockResult);

        // when & then
        mockMvc.perform(get("/oauth/bsm")
                .param("code", code)
                .param("state", state)
                .cookie(new Cookie("bsm_oauth_state", state)))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("auth/callback")))
                .andExpect(header().string("Location", containsString("token=")))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void 콜백_state_불일치_테스트() throws Exception {
        // given
        String code = "test_code";
        String wrongState = "wrong_state";
        String correctState = "correct_state";

        // when & then
        mockMvc.perform(get("/oauth/bsm")
                .param("code", code)
                .param("state", wrongState)
                .cookie(new Cookie("bsm_oauth_state", correctState)))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("error=state_mismatch")));
    }
}
```

## 단위 테스트

### BsmOAuthService 테스트

```java
@ExtendWith(MockitoExtension.class)
class BsmOAuthServiceTest {

    @Mock
    private RestClient bsmOauthRestClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private BsmOAuthService bsmOAuthService;

    @Test
    void buildAuthorizeUrl_테스트() {
        // given
        ReflectionTestUtils.setField(bsmOAuthService, "baseUrl", "https://auth.bssm.kro.kr");
        ReflectionTestUtils.setField(bsmOAuthService, "clientId", "test_client");
        ReflectionTestUtils.setField(bsmOAuthService, "redirectUri", "http://localhost/callback");

        String state = "test_state";

        // when
        String url = bsmOAuthService.buildAuthorizeUrl(state);

        // then
        assertThat(url).contains("https://auth.bssm.kro.kr/oauth");
        assertThat(url).contains("clientId=test_client");
        assertThat(url).contains("state=test_state");
        assertThat(url).contains("redirectURI=");
    }
}
```

### BsmLoginService 테스트

```java
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

    @Test
    void 신규_사용자_로그인_테스트() {
        // given
        String code = "test_code";
        JsonNode mockResource = createMockUserResource();
        BsmOAuthService.ExchangeResult exchangeResult =
            new BsmOAuthService.ExchangeResult("token", mockResource);

        when(bsmOAuthService.exchangeCode(code, true)).thenReturn(exchangeResult);
        when(userRepository.findByProviderAndProviderId(eq("bsm"), anyString()))
            .thenReturn(Optional.empty());
        when(authService.generateAccessToken(anyString(), anyString(), anyString()))
            .thenReturn("access_token");
        when(authService.generateRefreshToken(anyString(), anyString(), anyString()))
            .thenReturn("refresh_token");

        // when
        BsmLoginService.LoginResult result = bsmLoginService.login(code);

        // then
        assertThat(result.accessToken()).isEqualTo("access_token");
        assertThat(result.refreshToken()).isEqualTo("refresh_token");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void 기존_사용자_로그인_테스트() {
        // given
        String code = "test_code";
        JsonNode mockResource = createMockUserResource();
        BsmOAuthService.ExchangeResult exchangeResult =
            new BsmOAuthService.ExchangeResult("token", mockResource);

        User existingUser = User.builder()
            .email("test@bssm.hs.kr")
            .name("테스트")
            .provider("bsm")
            .providerId("123")
            .role(UserRole.USER)
            .build();

        when(bsmOAuthService.exchangeCode(code, true)).thenReturn(exchangeResult);
        when(userRepository.findByProviderAndProviderId("bsm", "123"))
            .thenReturn(Optional.of(existingUser));
        when(authService.generateAccessToken(anyString(), anyString(), anyString()))
            .thenReturn("access_token");
        when(authService.generateRefreshToken(anyString(), anyString(), anyString()))
            .thenReturn("refresh_token");

        // when
        BsmLoginService.LoginResult result = bsmLoginService.login(code);

        // then
        assertThat(result.accessToken()).isEqualTo("access_token");
        verify(userRepository, never()).save(any(User.class));
    }
}
```

## 에러 케이스 테스트

### 1. State 불일치
```
요청: GET /oauth/bsm?code=xxx&state=wrong_state
쿠키: bsm_oauth_state=correct_state
예상: 프론트엔드로 리다이렉트 (error=state_mismatch)
```

### 2. State 누락
```
요청: GET /oauth/bsm?code=xxx
예상: 프론트엔드로 리다이렉트 (error=state_mismatch)
```

### 3. Code 교환 실패
```
요청: GET /oauth/bsm?code=invalid_code&state=valid_state
예상: 프론트엔드로 리다이렉트 (error=bsm_oauth_failed)
```

### 4. 사용자 정보 조회 실패
```
BSM에서 토큰은 받았지만 사용자 정보 조회 실패
예상: 프론트엔드로 리다이렉트 (error=bsm_oauth_failed)
```

## 로그 확인

### 성공 케이스 로그
```
INFO  - Building BSM authorize URL with state: xxx
INFO  - BSM OAuth callback received. code=xxx, state=xxx
DEBUG - Exchanging BSM code for token
DEBUG - Fetching BSM user resource
INFO  - BSM login successful. userId=xxx, email=xxx
```

### 실패 케이스 로그
```
WARN  - BSM OAuth state mismatch. expected=xxx, actual=yyy
ERROR - BSM OAuth callback failed
```

## 데이터베이스 검증

### 사용자 생성 확인
```sql
SELECT * FROM user WHERE provider = 'bsm' ORDER BY created_at DESC LIMIT 1;
```

### Refresh Token 확인
```sql
SELECT * FROM refresh_token WHERE user_id = (
  SELECT id FROM user WHERE provider = 'bsm' AND provider_id = 'xxx'
);
```

## 환경별 설정

### 로컬 개발
```properties
bsm.oauth.base-url=https://auth.bssm.kro.kr
bsm.oauth.redirect-uri=http://localhost:8080/oauth/bsm
frontend.base-url=http://localhost:3000
```

### 스테이징
```properties
bsm.oauth.base-url=https://auth.bssm.kro.kr
bsm.oauth.redirect-uri=https://staging.jojaemin.com/oauth/bsm
frontend.base-url=https://staging-frontend.jojaemin.com
```

### 프로덕션
```properties
bsm.oauth.base-url=https://auth.bssm.kro.kr
bsm.oauth.redirect-uri=https://www.jojaemin.com/oauth/bsm
frontend.base-url=https://www.jojaemin.com
```

## 트러블슈팅

### 1. "state_mismatch" 에러
- 원인: state 쿠키가 만료되었거나 브라우저에서 삭제됨
- 해결: 인증을 처음부터 다시 시작

### 2. "bsm_oauth_failed" 에러
- 원인: BSM 서버 통신 실패 또는 잘못된 code
- 해결: 서버 로그 확인, BSM 클라이언트 정보 확인

### 3. Redirect URI 불일치
- 원인: BSM에 등록된 redirect URI와 설정이 다름
- 해결: application.properties의 `bsm.oauth.redirect-uri` 확인

### 4. 쿠키가 저장되지 않음
- 원인: SameSite 정책, HTTPS 요구사항
- 해결: 로컬에서는 HTTP 허용, 프로덕션에서는 HTTPS 사용

## 참고사항

1. **CSRF 보호**: state 파라미터로 CSRF 공격 방지
2. **쿠키 보안**:
   - `bsm_oauth_state`: SameSite=Lax (5분 유효)
   - `refreshToken`: SameSite=None, Secure (7일 유효)
3. **토큰 전달**: access token은 URL fragment로 전달 (보안상 이유)
4. **에러 처리**: 모든 에러는 프론트엔드로 리다이렉트하여 처리
