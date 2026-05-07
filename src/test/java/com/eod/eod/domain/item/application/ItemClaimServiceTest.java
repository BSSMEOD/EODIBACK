package com.eod.eod.domain.item.application;

import com.eod.eod.common.event.EodBusinessEvent;
import com.eod.eod.domain.discord.application.DiscordBotClient;
import com.eod.eod.domain.item.infrastructure.GiveRecordRepository;
import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.model.GiveRecord;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemClaimServiceTest {

    @Mock
    private ItemFacade itemFacade;

    @Mock
    private ItemClaimRepository itemClaimRepository;

    @Mock
    private GiveRecordRepository giveRecordRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DiscordBotClient discordBotClient;

    @InjectMocks
    private ItemClaimService itemClaimService;

    @Test
    void approveClaim은_스태프에게_픽업_예정_DM을_보낸다() {
        User admin = createUser(1L, "관리자", User.Role.ADMIN, null, null, null);
        User claimant = createUser(2L, "홍길동", User.Role.USER, 1, 2, 6);
        Item item = createItem(100L, "지갑", admin);
        ItemClaim claim = createClaim(10L, item, claimant, LocalDate.now().plusDays(1));

        when(itemClaimRepository.findById(10L)).thenReturn(Optional.of(claim));
        when(itemClaimRepository.findByItemIdAndStatus(100L, ItemClaim.ClaimStatus.PENDING))
                .thenReturn(List.of(claim));

        itemClaimService.approveClaim(10L, admin);

        verify(discordBotClient).notifyStaffPickupScheduled(
                eq(1206),
                eq("홍길동"),
                eq(claim.getVisitDate()),
                eq("지갑")
        );
    }

    @Test
    void approveClaim은_승인된_주장자에게도_DM을_보낸다() {
        User admin = createUser(1L, "관리자", User.Role.ADMIN, null, null, null);
        User claimant = createUser(2L, "홍길동", User.Role.USER, 1, 2, 6);
        claimant.updateDiscordId("999888777");
        Item item = createItem(100L, "지갑", admin);
        ItemClaim claim = createClaim(10L, item, claimant, LocalDate.now().plusDays(1));

        when(itemClaimRepository.findById(10L)).thenReturn(Optional.of(claim));
        when(itemClaimRepository.findByItemIdAndStatus(100L, ItemClaim.ClaimStatus.PENDING))
                .thenReturn(List.of(claim));

        itemClaimService.approveClaim(10L, admin);

        verify(discordBotClient).notifyClaimApproved("999888777", "지갑", claim.getVisitDate());
    }

    @Test
    void approveClaim은_같은_물품에_대한_다른_PENDING_주장은_거절_DM을_보낸다() {
        User admin = createUser(1L, "관리자", User.Role.ADMIN, null, null, null);
        User approved = createUser(2L, "홍길동", User.Role.USER, 1, 2, 6);
        approved.updateDiscordId("999");
        User other = createUser(3L, "김철수", User.Role.USER, 1, 2, 7);
        other.updateDiscordId("888");

        Item item = createItem(100L, "지갑", admin);
        ItemClaim approvedClaim = createClaim(10L, item, approved, LocalDate.now().plusDays(1));
        ItemClaim otherClaim = createClaim(11L, item, other, LocalDate.now().plusDays(1));

        when(itemClaimRepository.findById(10L)).thenReturn(Optional.of(approvedClaim));
        when(itemClaimRepository.findByItemIdAndStatus(100L, ItemClaim.ClaimStatus.PENDING))
                .thenReturn(List.of(approvedClaim, otherClaim));

        itemClaimService.approveClaim(10L, admin);

        verify(discordBotClient).notifyClaimRejected("888", "지갑");
        verify(discordBotClient, never()).notifyClaimRejected(eq("999"), any());
    }

    @Test
    void approveClaim은_학번이_없는_졸업생도_스태프에게_알림을_보낸다() {
        User admin = createUser(1L, "관리자", User.Role.ADMIN, null, null, null);
        User graduate = createUser(2L, "졸업생", User.Role.USER, null, null, null);
        Item item = createItem(100L, "우산", admin);
        ItemClaim claim = createClaim(10L, item, graduate, LocalDate.now().plusDays(1));

        when(itemClaimRepository.findById(10L)).thenReturn(Optional.of(claim));
        when(itemClaimRepository.findByItemIdAndStatus(100L, ItemClaim.ClaimStatus.PENDING))
                .thenReturn(List.of(claim));

        itemClaimService.approveClaim(10L, admin);

        verify(discordBotClient).notifyStaffPickupScheduled(
                eq((Integer) null),
                eq("졸업생"),
                eq(claim.getVisitDate()),
                eq("우산")
        );
    }

    @Test
    void approveClaim은_GiveRecord를_저장하고_business_이벤트를_발행한다() {
        User admin = createUser(1L, "관리자", User.Role.ADMIN, null, null, null);
        User claimant = createUser(2L, "홍길동", User.Role.USER, 1, 2, 6);
        Item item = createItem(100L, "지갑", admin);
        ItemClaim claim = createClaim(10L, item, claimant, LocalDate.now().plusDays(1));

        when(itemClaimRepository.findById(10L)).thenReturn(Optional.of(claim));
        when(itemClaimRepository.findByItemIdAndStatus(100L, ItemClaim.ClaimStatus.PENDING))
                .thenReturn(List.of(claim));

        itemClaimService.approveClaim(10L, admin);

        verify(giveRecordRepository).save(any(GiveRecord.class));
        verify(eventPublisher).publishEvent(any(EodBusinessEvent.class));
    }

    private User createUser(Long id, String name, User.Role role,
                            Integer grade, Integer classNo, Integer studentNo) {
        User user = User.builder()
                .name(name)
                .grade(grade)
                .classNo(classNo)
                .studentNo(studentNo)
                .oauthProvider("bsm")
                .oauthId("oauth-id-" + id)
                .email("user" + id + "@example.com")
                .role(role)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Item createItem(Long id, String name, User admin) {
        Item item = Item.builder()
                .admin(admin)
                .foundPlaceId(1L)
                .foundPlaceDetail("교무실")
                .name(name)
                .image("img.jpg")
                .status(Item.ItemStatus.LOST)
                .category(Item.ItemCategory.ETC)
                .foundAt(java.time.LocalDateTime.now())
                .foundAtPrecision(Item.DatePrecision.DAY)
                .build();
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    private ItemClaim createClaim(Long id, Item item, User claimant, LocalDate visitDate) {
        ItemClaim claim = ItemClaim.builder()
                .item(item)
                .claimant(claimant)
                .visitDate(visitDate)
                .build();
        ReflectionTestUtils.setField(claim, "id", id);
        return claim;
    }
}
