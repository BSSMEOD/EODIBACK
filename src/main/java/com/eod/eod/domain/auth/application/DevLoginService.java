package com.eod.eod.domain.auth.application;

import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"dev", "test"})
@RequiredArgsConstructor
@Transactional
public class DevLoginService {

    private static final String PROVIDER = "dev";

    private final UserRepository userRepository;
    private final AuthService authService;

    public AuthService.TokenPair login(User.Role role) {
        User user = userRepository.findByOauthProviderAndOauthId(PROVIDER, role.name().toLowerCase())
                .orElseGet(() -> userRepository.save(createUser(role)));
        return authService.issueTokensForOAuth2Login(user);
    }

    public User getDevUser(User.Role role) {
        return userRepository.findByOauthProviderAndOauthId(PROVIDER, role.name().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("dev 사용자를 찾을 수 없습니다."));
    }

    private User createUser(User.Role role) {
        boolean isStudent = role == User.Role.USER;
        return User.builder()
                .email("dev-" + role.name().toLowerCase() + "@eodi.local")
                .name("Dev " + role.name())
                .oauthProvider(PROVIDER)
                .oauthId(role.name().toLowerCase())
                .role(role)
                .grade(isStudent ? 1 : null)
                .classNo(isStudent ? 2 : null)
                .studentNo(isStudent ? 3 : null)
                .isGraduate(false)
                .build();
    }
}
