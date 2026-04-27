package com.eod.eod.domain.discord.application;

import com.eod.eod.domain.discord.exception.DiscordVerifyException;
import com.eod.eod.domain.discord.presentation.dto.request.DiscordVerifyRequest;
import com.eod.eod.domain.discord.presentation.dto.response.DiscordVerifyResponse;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DiscordVerifyService {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^(\\d+)기_([^_]+)$");

    private final UserRepository userRepository;

    @Transactional
    public DiscordVerifyResponse verify(DiscordVerifyRequest request) {
        ParsedNickname parsedNickname = parseNickname(request.nickname());
        List<User> students = userRepository.findByName(parsedNickname.name());
        students = filterStudentsByCohort(students, parsedNickname.expectedGrade());

        if (students.isEmpty()) {
            throw DiscordVerifyException.studentNotFound();
        }

        User student = resolveStudent(students, request.studentId());
        return connectDiscordAccount(student, request.discordUserId());
    }

    private ParsedNickname parseNickname(String nickname) {
        Matcher matcher = NICKNAME_PATTERN.matcher(nickname.trim());
        if (!matcher.matches()) {
            throw DiscordVerifyException.invalidNicknameFormat();
        }

        Integer cohort = Integer.parseInt(matcher.group(1));
        Integer expectedGrade = mapCohortToGrade(cohort);
        return new ParsedNickname(matcher.group(2).trim(), expectedGrade);
    }

    private List<User> filterStudentsByCohort(List<User> students, Integer expectedGrade) {
        if (expectedGrade == null) {
            return students;
        }

        return students.stream()
                .filter(student -> Objects.equals(student.getGrade(), expectedGrade))
                .collect(Collectors.toList());
    }

    private Integer mapCohortToGrade(int cohort) {
        return switch (cohort) {
            case 4 -> 3;
            case 5 -> 2;
            case 6 -> 1;
            default -> null;
        };
    }

    private User resolveStudent(List<User> students, String requestStudentId) {
        if (students.size() == 1) {
            User student = students.get(0);
            if (hasText(requestStudentId)) {
                validateStudentId(student, requestStudentId);
            }
            return student;
        }

        if (!hasText(requestStudentId)) {
            throw DiscordVerifyException.duplicateStudent();
        }

        for (User student : students) {
            Integer studentCode = student.getStudentCode();
            if (studentCode == null) {
                continue;
            }

            String actualStudentId = String.format("%04d", studentCode);
            if (Objects.equals(actualStudentId, requestStudentId)) {
                return student;
            }
        }

        throw DiscordVerifyException.studentIdMismatch();
    }

    private void validateStudentId(User student, String requestStudentId) {
        Integer studentCode = student.getStudentCode();
        if (studentCode == null) {
            throw DiscordVerifyException.studentIdMismatch();
        }

        String actualStudentId = String.format("%04d", studentCode);
        if (!actualStudentId.equals(requestStudentId)) {
            throw DiscordVerifyException.studentIdMismatch();
        }
    }

    private DiscordVerifyResponse connectDiscordAccount(User student, String discordUserId) {
        if (hasText(student.getDiscordId())) {
            if (Objects.equals(student.getDiscordId(), discordUserId)) {
                return DiscordVerifyResponse.alreadyVerified("이미 인증된 계정입니다.");
            }
            throw DiscordVerifyException.discordAlreadyLinked();
        }

        userRepository.findByDiscordId(discordUserId)
                .filter(existingUser -> !Objects.equals(existingUser.getId(), student.getId()))
                .ifPresent(existingUser -> {
                    throw DiscordVerifyException.discordUserAlreadyAssigned();
                });

        student.updateDiscordId(discordUserId);
        return DiscordVerifyResponse.success("인증이 완료되었습니다.");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ParsedNickname(String name, Integer expectedGrade) {
    }
}
