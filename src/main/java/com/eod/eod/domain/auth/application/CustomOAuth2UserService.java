package com.eod.eod.domain.auth.application;

import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // OAuth2 제공자 정보
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google", "kakao" 등
        String providerId = oAuth2User.getAttribute("sub"); // Google의 경우 'sub' 클레임이 고유 ID

        // 사용자 정보
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.info("OAuth2 로그인 시도 - provider: {}, providerId: {}, email: {}, name: {}", provider, providerId, email, name);

        // 사용자 조회 또는 생성 (OAuth provider + providerId 기준)
        User user = userRepository.findByOauthProviderAndOauthId(provider, providerId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .oauthProvider(provider)
                            .oauthId(providerId)
                            .email(email)
                            .name(name)
                            .role(User.Role.USER)
                            .build();
                    return userRepository.save(newUser);
                });

        log.info("사용자 인증 완료 - userId: {}, role: {}", user.getId(), user.getRole());

        return oAuth2User;
    }
}
