package com.eod.eod.domain.item.application;

import com.eod.eod.common.util.ExternalServerUtil;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.place.infrastructure.PlaceRepository;
import com.eod.eod.domain.user.model.User;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemRegistrationService {

    private final ItemFacade itemFacade;
    private final PlaceRepository placeRepository;
    private final ExternalServerUtil externalServerUtil;
    private final UserRepository userRepository;

    public Long registerItem(
            String name,
            Integer reporterStudentCode,
            String reporterName,
            String foundAt,
            Long placeId,
            String placeDetail,
            String imageUrl,
            Item.ItemCategory category,
            User currentUser
    ) {
        LocalDate parsedFoundAt = parseDate(foundAt);
        ensurePlaceExists(placeId);

        LocalDateTime foundAtDateTime = toFoundDateTime(parsedFoundAt);

        int grade = reporterStudentCode / 1000;
        int classNo = (reporterStudentCode / 100) % 10;
        int studentNo = reporterStudentCode % 100;

        User student = userRepository.findByGradeAndClassNoAndStudentNo(grade, classNo, studentNo)
                .orElseThrow(() -> new IllegalArgumentException("신고자 학생 코드에 해당하는 학생을 찾을 수 없습니다."));

        Item item = Item.registerLostItem(
                currentUser,
                student,
                placeId,
                placeDetail,
                name,
                reporterName,
                imageUrl,
                category,
                foundAtDateTime
        );

        Item savedItem = itemFacade.save(item);
        return savedItem.getId();
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

    private String resolveImageUrl(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return "";
        }

        Item.validateImage(imageFile.getSize(), imageFile.getContentType());

        try {
            Resource resource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };
            return externalServerUtil.loginAndUpload(resource, imageFile.getContentType()).url();
        } catch (IOException e) {
            throw new IllegalStateException("이미지 파일을 읽을 수 없습니다.");
        } catch (Exception e) {
            // 외부 서버 통신 실패 (네트워크 오류, 타임아웃, 인증 실패 등)
            throw new IllegalStateException("이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    public void updateItem(
            Long itemId,
            String name,
            Integer reporterStudentCode,
            String reporterName,
            String foundAt,
            Long placeId,
            String placeDetail,
            String imageUrl,
            Item.ItemCategory category,
            User currentUser
    ) {
        Item item = itemFacade.getItemById(itemId);

        LocalDate parsedFoundAt = parseDate(foundAt);
        ensurePlaceExists(placeId);

        LocalDateTime foundAtDateTime = toFoundDateTime(parsedFoundAt);

        int grade = reporterStudentCode / 1000;
        int classNo = (reporterStudentCode / 100) % 10;
        int studentNo = reporterStudentCode % 100;

        User student = userRepository.findByGradeAndClassNoAndStudentNo(grade, classNo, studentNo)
                .orElseThrow(() -> new IllegalArgumentException("신고자 학생 코드에 해당하는 학생을 찾을 수 없습니다."));

        item.updateItem(
                currentUser,
                student,
                placeId,
                placeDetail,
                name,
                reporterName,
                imageUrl,
                category,
                foundAtDateTime
        );
    }

    private LocalDateTime toFoundDateTime(LocalDate date) {
        return date.atStartOfDay();
    }
}
