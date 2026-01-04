package com.eod.eod.domain.item.application;

import com.eod.eod.common.annotation.RequireAdmin;
import com.eod.eod.domain.item.application.command.ItemRegistrationCommand;
import com.eod.eod.domain.item.application.command.ItemUpdateCommand;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.place.infrastructure.PlaceRepository;
import com.eod.eod.domain.user.model.User;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemRegistrationService {

    private final ItemFacade itemFacade;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    @RequireAdmin
    public Long registerItem(ItemRegistrationCommand command, User currentUser) {
        LocalDateTime foundAtDateTime = validateAndParseFoundAt(command.foundAt(), command.placeId());
        User student = findStudentByCodeAndName(command.reporterStudentCode(), command.reporterName());

        Item item = Item.registerLostItem(
                currentUser,
                student,
                command.placeId(),
                command.placeDetail(),
                command.name(),
                command.imageUrl(),
                command.category(),
                foundAtDateTime
        );

        Item savedItem = itemFacade.save(item);
        return savedItem.getId();
    }

    /**
     * 습득일 검증 및 파싱, 장소 존재 여부 검증
     */
    private LocalDateTime validateAndParseFoundAt(String foundAt, Long placeId) {
        ensurePlaceExists(placeId);
        LocalDate parsedFoundAt = parseDate(foundAt);
        return toFoundDateTime(parsedFoundAt);
    }

    /**
     * 학생 코드와 이름으로 학생 조회
     */
    private User findStudentByCodeAndName(Integer reporterStudentCode, String reporterName) {
        int grade = reporterStudentCode / 1000;
        int classNo = (reporterStudentCode / 100) % 10;
        int studentNo = reporterStudentCode % 100;

        return userRepository.findByGradeAndClassNoAndStudentNoAndName(grade, classNo, studentNo, reporterName)
                .orElseThrow(() -> new IllegalArgumentException("신고자 학생 코드와 이름이 일치하는 학생을 찾을 수 없습니다."));
    }

    private LocalDate parseDate(String rawDate) {
        try {
            return LocalDate.parse(rawDate);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("올바른 날짜 형식이 아닙니다. (yyyy-MM-dd)");
        }
    }

    private void ensurePlaceExists(Long placeId) {
        if (placeId == null) {
            throw new IllegalStateException("필수 항목이 누락되었습니다.");
        }
        if (!placeRepository.existsById(placeId)) {
            throw new IllegalArgumentException("등록되지 않은 장소입니다.");
        }
    }

    @RequireAdmin
    public void updateItem(Long itemId, ItemUpdateCommand command, User currentUser) {
        Item item = itemFacade.getItemById(itemId);
        LocalDateTime foundAtDateTime = validateAndParseFoundAt(command.foundAt(), command.placeId());
        User student = findStudentByCodeAndName(command.reporterStudentCode(), command.reporterName());

        item.updateItem(
                currentUser,
                student,
                command.placeId(),
                command.placeDetail(),
                command.name(),
                command.imageUrl(),
                command.category(),
                foundAtDateTime
        );
    }

    private LocalDateTime toFoundDateTime(LocalDate date) {
        return date.atStartOfDay();
    }
}
