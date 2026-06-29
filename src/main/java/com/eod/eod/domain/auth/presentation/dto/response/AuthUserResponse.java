package com.eod.eod.domain.auth.presentation.dto.response;

import com.eod.eod.domain.user.model.User;

public record AuthUserResponse(
        Long id,
        String email,
        String name,
        User.Role role,
        Integer studentCode
) {
    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getStudentCode()
        );
    }
}
