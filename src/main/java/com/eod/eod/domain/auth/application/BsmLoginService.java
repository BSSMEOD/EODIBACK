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
            // 기존 사용자의 학생 정보를 BSM 최신 정보로 업데이트
            User existingUser = byProviderAndId.get();
            existingUser.updateStudentInfo(
                    userInfo.isGraduate(),
                    userInfo.grade(),
                    userInfo.classNo(),
                    userInfo.studentNo()
            );
            return userRepository.save(existingUser);
        }

        // 이메일은 unique 이므로, 다른 provider 계정과 자동으로 연결하지 않습니다.
        if (userRepository.findByEmail(userInfo.email()).isPresent()) {
            throw new IllegalStateException("이미 다른 방식으로 가입된 이메일입니다. 계정 연결이 필요합니다.");
        }

        User newUser = User.builder()
                .isGraduate(userInfo.isGraduate())
                .grade(userInfo.grade())
                .classNo(userInfo.classNo())
                .studentNo(userInfo.studentNo())
                .oauthProvider(PROVIDER)
                .oauthId(userInfo.oauthId())
                .name(userInfo.name())
                .email(userInfo.email())
                .role(userInfo.role())
                .build();

        return userRepository.save(newUser);
    }

    public record LoginResult(String accessToken, String refreshToken, User user) {}

    private record BsmUserInfo(String oauthId, String email, String name, User.Role role,
                               Boolean isGraduate, Integer grade, Integer classNo, Integer studentNo) {
        static BsmUserInfo from(JsonNode resource) {
            // BSM API 응답 구조: {"user": {...}, "scopeList": [...]}
            JsonNode userNode = resource.get("user");
            if (userNode == null || userNode.isNull()) {
                throw new IllegalStateException("BSM resource에 user 객체가 없습니다: " + resource);
            }

            // email은 필수
            String email = firstText(userNode, "email");
            if (email == null || email.isBlank()) {
                throw new IllegalStateException("BSM resource에 email이 없습니다: " + resource);
            }

            // id가 없는 경우 studentNo를 oauthId로 사용 (학생), 또는 email 사용 (교사)
            String oauthId = firstText(userNode, "id", "userCode", "user_code");
            if (oauthId == null || oauthId.isBlank()) {
                Integer studentNoForId = firstInt(userNode, "studentNo", "studentNumber", "student_no");
                if (studentNoForId != null) {
                    oauthId = "student_" + studentNoForId;
                } else {
                    oauthId = "email_" + email;
                }
            }

            // 이름 (nickname 또는 name, 없으면 email 사용)
            String name = firstText(userNode, "nickname", "name");
            if (name == null || name.isBlank()) {
                name = email;
            }

            // 역할 (TEACHER 또는 USER)
            String roleText = firstText(userNode, "role");
            User.Role appRole = (roleText != null && roleText.toUpperCase().contains("TEACHER"))
                    ? User.Role.TEACHER
                    : User.Role.USER;

            // 학생 정보는 user 객체에 직접 포함되어 있음
            Boolean isGraduate = firstBoolean(userNode, "isGraduate", "is_graduate", "graduate");
            Integer grade = firstInt(userNode, "grade");
            Integer classNo = firstInt(userNode, "classNo", "classNumber", "class_num", "class");
            Integer studentNo = firstInt(userNode, "studentNo", "studentNumber", "student_no", "number");

            return new BsmUserInfo(oauthId, email, name, appRole, isGraduate, grade, classNo, studentNo);
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

        private static Boolean firstBoolean(JsonNode node, String... keys) {
            for (String key : keys) {
                JsonNode value = node.get(key);
                if (value == null || value.isNull()) continue;
                if (value.isBoolean()) return value.asBoolean();
                if (value.isNumber()) return value.asInt() != 0;
                if (value.isTextual()) {
                    String text = value.asText().trim().toLowerCase();
                    if ("true".equals(text) || "1".equals(text) || "yes".equals(text)) return true;
                    if ("false".equals(text) || "0".equals(text) || "no".equals(text)) return false;
                }
            }
            return null;
        }
    }
}
