package com.eod.eod.domain.auth.application;

import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BsmLoginService {

    private static final String PROVIDER = "bsm";

    private final BsmOAuthService bsmOAuthService;
    private final UserRepository userRepository;
    private final AuthService authService;

    public LoginResult login(String code) {
        BsmOAuthService.ExchangeResult exchangeResult = bsmOAuthService.exchangeCode(code, true);
        JsonNode resource = exchangeResult.resource();
        if (resource == null || resource.isNull()) {
            throw new IllegalStateException("BSM resource를 가져오지 못했습니다. (토큰 교환은 성공했을 수 있습니다)");
        }

        BsmUserInfo userInfo = BsmUserInfo.from(resource);
        User user = findOrCreateUser(userInfo);

        AuthService.TokenPair tokenPair = authService.issueTokensForOAuth2Login(user);
        return new LoginResult(tokenPair.getAccessToken(), tokenPair.getRefreshToken(), user);
    }

    private User findOrCreateUser(BsmUserInfo userInfo) {
        Optional<User> byProviderAndId = userRepository.findByOauthProviderAndOauthId(PROVIDER, userInfo.oauthId());
        if (byProviderAndId.isPresent()) {
            return byProviderAndId.get();
        }

        // 이메일은 unique 이므로, 다른 provider 계정과 자동으로 연결하지 않습니다.
        if (userRepository.findByEmail(userInfo.email()).isPresent()) {
            throw new IllegalStateException("이미 다른 방식으로 가입된 이메일입니다. 계정 연결이 필요합니다.");
        }

        User newUser = User.builder()
                .studentCode(userInfo.studentCode())
                .oauthProvider(PROVIDER)
                .oauthId(userInfo.oauthId())
                .name(userInfo.name())
                .email(userInfo.email())
                .role(userInfo.role())
                .build();

        return userRepository.save(newUser);
    }

    public record LoginResult(String accessToken, String refreshToken, User user) {}

    private record BsmUserInfo(String oauthId, String email, String name, User.Role role, Integer studentCode) {
        static BsmUserInfo from(JsonNode resource) {
            String oauthId = firstText(resource, "id", "userCode", "user_code");
            if (oauthId == null || oauthId.isBlank()) {
                throw new IllegalStateException("BSM resource에 id/userCode가 없습니다: " + resource);
            }

            String email = firstText(resource, "email");
            if (email == null || email.isBlank()) {
                throw new IllegalStateException("BSM resource에 email이 없습니다: " + resource);
            }

            String name = firstText(resource, "nickname", "name");
            if (name == null || name.isBlank()) {
                name = email;
            }

            String roleText = firstText(resource, "role");
            User.Role appRole = (roleText != null && roleText.toUpperCase().contains("TEACHER"))
                    ? User.Role.TEACHER
                    : User.Role.USER;

            Integer studentCode = null;
            JsonNode studentNode = resource.get("student");
            if (studentNode != null && studentNode.isObject()) {
                Integer grade = firstInt(studentNode, "grade");
                Integer classNumber = firstInt(studentNode, "classNo", "classNumber", "class_num", "class");
                if (grade != null && classNumber != null) {
                    // 기존 코드에서 grade/class 필터링에 사용되는 형태(학년*100 + 반*10)
                    studentCode = grade * 100 + classNumber * 10;
                }
            }

            return new BsmUserInfo(oauthId, email, name, appRole, studentCode);
        }

        private static String firstText(JsonNode node, String... keys) {
            for (String key : keys) {
                JsonNode value = node.get(key);
                if (value == null || value.isNull()) continue;
                if (value.isTextual()) return value.asText();
                if (value.isNumber() || value.isBoolean()) return value.asText();
            }
            return null;
        }

        private static Integer firstInt(JsonNode node, String... keys) {
            for (String key : keys) {
                JsonNode value = node.get(key);
                if (value == null || value.isNull()) continue;
                if (value.isInt()) return value.asInt();
                if (value.isNumber()) return value.numberValue().intValue();
                if (value.isTextual()) {
                    try {
                        return Integer.parseInt(value.asText());
                    } catch (NumberFormatException ignored) {
                        // continue
                    }
                }
            }
            return null;
        }
    }
}
