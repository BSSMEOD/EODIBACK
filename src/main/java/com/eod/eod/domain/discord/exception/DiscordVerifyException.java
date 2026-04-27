package com.eod.eod.domain.discord.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DiscordVerifyException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String status;
    private final String code;
    private final boolean requiresStudentId;

    private DiscordVerifyException(HttpStatus httpStatus, String status, String code, boolean requiresStudentId, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.status = status;
        this.code = code;
        this.requiresStudentId = requiresStudentId;
    }

    public static DiscordVerifyException invalidNicknameFormat() {
        return new DiscordVerifyException(
                HttpStatus.BAD_REQUEST,
                "invalid_request",
                "INVALID_NICKNAME_FORMAT",
                false,
                "닉네임 형식이 올바르지 않습니다. `4기_김현호` 형식으로 입력해주세요."
        );
    }

    public static DiscordVerifyException duplicateStudent() {
        return new DiscordVerifyException(
                HttpStatus.CONFLICT,
                "duplicate",
                "DUPLICATE_STUDENT",
                true,
                "동명이인이 있어 학번 입력이 필요합니다."
        );
    }

    public static DiscordVerifyException studentNotFound() {
        return new DiscordVerifyException(
                HttpStatus.NOT_FOUND,
                "not_found",
                "STUDENT_NOT_FOUND",
                false,
                "일치하는 학생 정보를 찾을 수 없습니다."
        );
    }

    public static DiscordVerifyException studentIdMismatch() {
        return new DiscordVerifyException(
                HttpStatus.BAD_REQUEST,
                "mismatch",
                "STUDENT_ID_MISMATCH",
                false,
                "입력한 학번과 닉네임 정보가 일치하지 않습니다."
        );
    }

    public static DiscordVerifyException discordAlreadyLinked() {
        return new DiscordVerifyException(
                HttpStatus.CONFLICT,
                "already_linked",
                "DISCORD_ALREADY_LINKED",
                false,
                "이미 다른 디스코드 계정에 연결된 학생입니다."
        );
    }

    public static DiscordVerifyException discordUserAlreadyAssigned() {
        return new DiscordVerifyException(
                HttpStatus.CONFLICT,
                "already_linked",
                "DISCORD_ALREADY_LINKED",
                false,
                "이미 다른 학생에 연결된 디스코드 계정입니다."
        );
    }
}
