package com.eod.eod.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

// 외부 인증·이미지 서버에 로그인/업로드 요청을 위임하는 유틸리티
@Component
public class ExternalServerUtil {

    private final RestClient restClient;
    private final String loginEndpoint;
    private final String imageUploadEndpoint;
    private final String defaultUid;
    private final String defaultPassword;

    public ExternalServerUtil(RestClient.Builder restClientBuilder,
                              @Value("${external.server.base-url}") String baseUrl,
                              @Value("${external.server.login-endpoint}") String loginEndpoint,
                              @Value("${external.server.image-upload-endpoint}") String imageUploadEndpoint,
                              @Value("${external.server.default-uid}") String defaultUid,
                              @Value("${external.server.default-password}") String defaultPassword) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.loginEndpoint = loginEndpoint;
        this.imageUploadEndpoint = imageUploadEndpoint;
        this.defaultUid = defaultUid;
        this.defaultPassword = defaultPassword;
    }

    // 기본 자격으로 로그인 요청을 수행
    public LoginResult login() {
        return login(defaultUid, defaultPassword);
    }

    // 지정된 uid/password로 로그인하고 토큰 정보를 반환
    public LoginResult login(String uid, String password) {
        LoginRequest loginRequest = new LoginRequest(uid, password);
        ResponseEntity<LoginResponse> response = restClient.post()
                .uri(loginEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginRequest)
                .retrieve()
                .toEntity(LoginResponse.class);

        LoginResponse body = Objects.requireNonNull(response.getBody(), "로그인 응답이 비어 있습니다.");
        String refreshToken = extractRefreshToken(response.getHeaders());

        return new LoginResult(body.accessToken(), refreshToken, body.expiresIn());
    }

    // accessToken을 사용해 이미지 파일을 업로드하고 URL 결과를 반환
    public ImageUploadResult uploadImage(Resource fileResource, String accessToken, String contentType) {
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg")); // 이미지 MIME 타입으로 설정
        fileHeaders.setContentDispositionFormData("file", fileResource.getFilename());
        HttpEntity<Resource> fileEntity = new HttpEntity<>(fileResource, fileHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        ResponseEntity<ImageUploadResponse> response = restClient.post()
                .uri(imageUploadEndpoint)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .body(body)
                .retrieve()
                .toEntity(ImageUploadResponse.class);

        ImageUploadResponse responseBody = Objects.requireNonNull(response.getBody(), "이미지 업로드 응답이 비어 있습니다.");
        return new ImageUploadResult(responseBody.url());
    }

    // 로그인한 뒤 업로드까지 한 번에 수행
    public ImageUploadResult loginAndUpload(Resource fileResource, String contentType) {
        LoginResult loginResult = login();
        return uploadImage(fileResource, loginResult.accessToken(), contentType);
    }

    private String extractRefreshToken(HttpHeaders headers) {
        List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);
        if (cookies == null) {
            return null;
        }
        for (String cookie : cookies) {
            if (!StringUtils.hasText(cookie)) {
                continue;
            }
            String[] parts = cookie.split(";");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.startsWith("refreshToken=")) {
                    return trimmed.substring("refreshToken=".length());
                }
            }
        }
        return null;
    }

    private record LoginRequest(String uid, String password) { }

    private record LoginResponse(String accessToken, long expiresIn) { }

    public record LoginResult(String accessToken, String refreshToken, long expiresIn) { }

    private record ImageUploadResponse(String url) { }

    public record ImageUploadResult(String url) { }
}
