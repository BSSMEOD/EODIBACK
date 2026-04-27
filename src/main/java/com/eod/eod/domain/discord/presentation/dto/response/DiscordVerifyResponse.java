package com.eod.eod.domain.discord.presentation.dto.response;

public record DiscordVerifyResponse(
        boolean success,
        String status,
        String code,
        Boolean requiresStudentId,
        String message
) {
    public static DiscordVerifyResponse success(String message) {
        return new DiscordVerifyResponse(true, "verified", null, false, message);
    }

    public static DiscordVerifyResponse alreadyVerified(String message) {
        return new DiscordVerifyResponse(true, "already_verified", null, false, message);
    }

    public static DiscordVerifyResponse fail(String status, String code, boolean requiresStudentId, String message) {
        return new DiscordVerifyResponse(false, status, code, requiresStudentId, message);
    }

    public static DiscordVerifyResponse internalServerError() {
        return new DiscordVerifyResponse(false, "error", "INTERNAL_SERVER_ERROR", false, "서버 오류가 발생했습니다.");
    }
}
