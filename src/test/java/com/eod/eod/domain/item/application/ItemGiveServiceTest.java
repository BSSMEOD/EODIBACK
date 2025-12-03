package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.GiveRecordRepository;
import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.GiveRecord;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemGiveServiceTest {

    @Mock
    private ItemFacade itemFacade;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GiveRecordRepository giveRecordRepository;

    @InjectMocks
    private ItemGiveService itemGiveService;

    @Test
    void 물품_지급_성공() {
        // given
        Long itemId = 1L;
        Long receiverId = 2L;

        User adminUser = User.builder()
                .name("Admin User")
                .email("admin@test.com")
                .role(User.Role.ADMIN)
                .build();

        User receiverUser = User.builder()
                .name("Receiver User")
                .email("receiver@test.com")
                .role(User.Role.USER)
                .build();

        Item item = Item.builder()
                .student(receiverUser)
                .admin(adminUser)
                .foundPlaceId(1L)
                .foundPlaceDetail("Test place")
                .name("Test item")
                .image("test.jpg")
                .status(Item.ItemStatus.LOST)
                .foundAt(java.time.LocalDateTime.now())
                .build();

        when(itemFacade.getItemById(itemId)).thenReturn(item);
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiverUser));

        // when
        itemGiveService.giveItemToStudent(itemId, receiverId, adminUser);

        // then
        verify(itemFacade).getItemById(itemId);
        verify(userRepository).findById(receiverId);
        verify(giveRecordRepository).save(any(GiveRecord.class));
    }

    @Test
    void 물품_지급_실패_ADMIN_권한_없음() {
        // given
        Long itemId = 1L;
        Long receiverId = 2L;

        User adminUser = User.builder()
                .name("Admin User")
                .email("admin@test.com")
                .role(User.Role.ADMIN)
                .build();

        User nonAdminUser = User.builder()
                .name("Non Admin User")
                .email("user@test.com")
                .role(User.Role.USER)
                .build();

        User receiverUser = User.builder()
                .name("Receiver User")
                .email("receiver@test.com")
                .role(User.Role.USER)
                .build();

        Item item = Item.builder()
                .student(receiverUser)
                .admin(adminUser)
                .foundPlaceId(1L)
                .foundPlaceDetail("Test place")
                .name("Test item")
                .image("test.jpg")
                .status(Item.ItemStatus.LOST)
                .foundAt(java.time.LocalDateTime.now())
                .build();

        when(itemFacade.getItemById(itemId)).thenReturn(item);
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiverUser));

        // when & then
        assertThatThrownBy(() -> itemGiveService.giveItemToStudent(itemId, receiverId, nonAdminUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("ADMIN 권한이 없습니다.");

        verify(itemFacade).getItemById(itemId);
        verify(userRepository).findById(receiverId);
        verify(giveRecordRepository, never()).save(any());
    }

    @Test
    void 물품_지급_실패_물품_없음() {
        // given
        Long itemId = 999L;
        Long receiverId = 2L;

        User adminUser = User.builder()
                .name("Admin User")
                .email("admin@test.com")
                .role(User.Role.ADMIN)
                .build();

        when(itemFacade.getItemById(itemId)).thenThrow(new IllegalArgumentException("해당 물품을 찾을 수 없습니다."));

        // when & then
        assertThatThrownBy(() -> itemGiveService.giveItemToStudent(itemId, receiverId, adminUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 물품을 찾을 수 없습니다.");

        verify(itemFacade).getItemById(itemId);
        verify(userRepository, never()).findById(any());
        verify(giveRecordRepository, never()).save(any());
    }

    @Test
    void 물품_지급_실패_학생_없음() {
        // given
        Long itemId = 1L;
        Long receiverId = 999L;

        User adminUser = User.builder()
                .name("Admin User")
                .email("admin@test.com")
                .role(User.Role.ADMIN)
                .build();

        Item item = Item.builder()
                .student(User.builder().name("Student").email("student@test.com").role(User.Role.USER).build())
                .admin(adminUser)
                .foundPlaceId(1L)
                .foundPlaceDetail("Test place")
                .name("Test item")
                .image("test.jpg")
                .status(Item.ItemStatus.LOST)
                .foundAt(java.time.LocalDateTime.now())
                .build();

        when(itemFacade.getItemById(itemId)).thenReturn(item);
        when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> itemGiveService.giveItemToStudent(itemId, receiverId, adminUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 학생을 찾을 수 없습니다.");

        verify(itemFacade).getItemById(itemId);
        verify(userRepository).findById(receiverId);
        verify(giveRecordRepository, never()).save(any());
    }

    @Test
    void 물품_지급_실패_이미_지급된_물품() {
        // given
        Long itemId = 1L;
        Long receiverId = 2L;

        User adminUser = User.builder()
                .name("Admin User")
                .email("admin@test.com")
                .role(User.Role.ADMIN)
                .build();

        User receiverUser = User.builder()
                .name("Receiver User")
                .email("receiver@test.com")
                .role(User.Role.USER)
                .build();

        Item item = Item.builder()
                .student(receiverUser)
                .admin(adminUser)
                .foundPlaceId(1L)
                .foundPlaceDetail("Test place")
                .name("Test item")
                .image("test.jpg")
                .status(Item.ItemStatus.GIVEN)
                .foundAt(java.time.LocalDateTime.now())
                .build();

        when(itemFacade.getItemById(itemId)).thenReturn(item);
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiverUser));

        // when & then
        assertThatThrownBy(() -> itemGiveService.giveItemToStudent(itemId, receiverId, adminUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 물품은 이미 지급 처리되었습니다.");

        verify(itemFacade).getItemById(itemId);
        verify(userRepository).findById(receiverId);
        verify(giveRecordRepository, never()).save(any());
    }
}