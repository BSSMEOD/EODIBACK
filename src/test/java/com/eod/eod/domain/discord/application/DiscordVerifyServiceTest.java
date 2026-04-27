package com.eod.eod.domain.discord.application;

import com.eod.eod.domain.discord.presentation.dto.request.DiscordVerifyRequest;
import com.eod.eod.domain.discord.presentation.dto.response.DiscordVerifyResponse;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscordVerifyServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DiscordVerifyService discordVerifyService;

    @Test
    void verifyUpdatesDiscordIdWithFirstMatchedStudent() {
        User firstStudent = createUser("김현호", 3, 1, 1);
        DiscordVerifyRequest request = new DiscordVerifyRequest("3101", "4기_김현호", "123456789012345678");

        when(userRepository.findByName("김현호")).thenReturn(List.of(firstStudent));
        when(userRepository.findByDiscordId("123456789012345678")).thenReturn(Optional.empty());

        DiscordVerifyResponse response = discordVerifyService.verify(request);

        assertEquals("123456789012345678", firstStudent.getDiscordId());
        assertEquals("verified", response.status());
    }

    @Test
    void verifyThrowsWhenNicknameFormatIsInvalid() {
        DiscordVerifyRequest request = new DiscordVerifyRequest(null, "4기김현호", "123456789012345678");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> discordVerifyService.verify(request));

        assertEquals("닉네임 형식이 올바르지 않습니다. `4기_김현호` 형식으로 입력해주세요.", exception.getMessage());
    }

    @Test
    void verifyThrowsWhenStudentNotFound() {
        DiscordVerifyRequest request = new DiscordVerifyRequest(null, "4기_김현호", "123456789012345678");

        when(userRepository.findByName("김현호")).thenReturn(List.of());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> discordVerifyService.verify(request));

        assertEquals("일치하는 학생 정보를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void verifyThrowsDuplicateWhenMultipleStudentsExistWithoutStudentId() {
        User firstStudent = createUser("김현호", 3, 1, 1);
        User secondStudent = createUser("김현호", 3, 1, 2);
        DiscordVerifyRequest request = new DiscordVerifyRequest(null, "4기_김현호", "123456789012345678");

        when(userRepository.findByName("김현호")).thenReturn(List.of(firstStudent, secondStudent));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> discordVerifyService.verify(request));

        assertEquals("동명이인이 있어 학번 입력이 필요합니다.", exception.getMessage());
    }

    @Test
    void verifyResolvesStudentByCohortGradeWithoutStudentId() {
        User fourthCohortStudent = createUser("김현호", 3, 1, 1);
        User fifthCohortStudent = createUser("김현호", 2, 1, 1);
        DiscordVerifyRequest request = new DiscordVerifyRequest(null, "4기_김현호", "123456789012345678");

        when(userRepository.findByName("김현호")).thenReturn(List.of(fourthCohortStudent, fifthCohortStudent));
        when(userRepository.findByDiscordId("123456789012345678")).thenReturn(Optional.empty());

        DiscordVerifyResponse response = discordVerifyService.verify(request);

        assertEquals("123456789012345678", fourthCohortStudent.getDiscordId());
        assertEquals(null, fifthCohortStudent.getDiscordId());
        assertEquals("verified", response.status());
    }

    @Test
    void verifyThrowsWhenNoStudentMatchesKnownCohortGrade() {
        User fifthCohortStudent = createUser("김현호", 2, 1, 1);
        DiscordVerifyRequest request = new DiscordVerifyRequest(null, "4기_김현호", "123456789012345678");

        when(userRepository.findByName("김현호")).thenReturn(List.of(fifthCohortStudent));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> discordVerifyService.verify(request));

        assertEquals("일치하는 학생 정보를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void verifyUpdatesDiscordIdWhenStudentIdMatchesAmongDuplicates() {
        User firstStudent = createUser("김현호", 3, 1, 1);
        User secondStudent = createUser("김현호", 3, 1, 2);
        DiscordVerifyRequest request = new DiscordVerifyRequest("3102", "4기_김현호", "123456789012345678");

        when(userRepository.findByName("김현호")).thenReturn(List.of(firstStudent, secondStudent));
        when(userRepository.findByDiscordId("123456789012345678")).thenReturn(Optional.empty());

        DiscordVerifyResponse response = discordVerifyService.verify(request);

        assertEquals(null, firstStudent.getDiscordId());
        assertEquals("123456789012345678", secondStudent.getDiscordId());
        assertEquals("verified", response.status());
    }

    @Test
    void verifyThrowsWhenStudentIdDoesNotMatchAnyDuplicate() {
        User firstStudent = createUser("김현호", 3, 1, 1);
        User secondStudent = createUser("김현호", 3, 1, 2);
        DiscordVerifyRequest request = new DiscordVerifyRequest("3103", "4기_김현호", "123456789012345678");

        when(userRepository.findByName("김현호")).thenReturn(List.of(firstStudent, secondStudent));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> discordVerifyService.verify(request));

        assertEquals("입력한 학번과 닉네임 정보가 일치하지 않습니다.", exception.getMessage());
    }

    @Test
    void verifyReturnsAlreadyVerifiedWhenSameDiscordIdIsLinked() {
        User student = createUser("김현호", 3, 1, 1);
        student.updateDiscordId("123456789012345678");
        DiscordVerifyRequest request = new DiscordVerifyRequest(null, "4기_김현호", "123456789012345678");

        when(userRepository.findByName("김현호")).thenReturn(List.of(student));

        DiscordVerifyResponse response = discordVerifyService.verify(request);

        assertEquals("already_verified", response.status());
        assertEquals("이미 인증된 계정입니다.", response.message());
    }

    @Test
    void verifyThrowsWhenStudentIsLinkedToAnotherDiscordId() {
        User student = createUser("김현호", 3, 1, 1);
        student.updateDiscordId("999999999999999999");
        DiscordVerifyRequest request = new DiscordVerifyRequest(null, "4기_김현호", "123456789012345678");

        when(userRepository.findByName("김현호")).thenReturn(List.of(student));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> discordVerifyService.verify(request));

        assertEquals("이미 다른 디스코드 계정에 연결된 학생입니다.", exception.getMessage());
    }

    @Test
    void verifyThrowsWhenDiscordUserIdIsAlreadyAssignedToAnotherStudent() {
        User targetStudent = createUser("김현호", 3, 1, 1);
        User linkedStudent = createUser("이민지", 3, 1, 5);
        ReflectionTestUtils.setField(targetStudent, "id", 1L);
        ReflectionTestUtils.setField(linkedStudent, "id", 2L);
        linkedStudent.updateDiscordId("123456789012345678");
        DiscordVerifyRequest request = new DiscordVerifyRequest(null, "4기_김현호", "123456789012345678");

        when(userRepository.findByName("김현호")).thenReturn(List.of(targetStudent));
        when(userRepository.findByDiscordId("123456789012345678")).thenReturn(Optional.of(linkedStudent));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> discordVerifyService.verify(request));

        assertEquals("이미 다른 학생에 연결된 디스코드 계정입니다.", exception.getMessage());
    }

    private User createUser(String name, int grade, int classNo, int studentNo) {
        return User.builder()
                .name(name)
                .grade(grade)
                .classNo(classNo)
                .studentNo(studentNo)
                .oauthProvider("bsm")
                .oauthId("oauth-id-" + studentNo)
                .email("student" + studentNo + "@example.com")
                .role(User.Role.USER)
                .build();
    }
}
