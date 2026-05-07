package com.eod.eod.domain.discord.application;

import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PickupReminderSchedulerTest {

    @Mock
    private ItemClaimRepository itemClaimRepository;

    @Mock
    private DiscordBotClient discordBotClient;

    @InjectMocks
    private PickupReminderScheduler pickupReminderScheduler;

    @Test
    void 오늘_visitDate인_승인건에_13시10분_리마인더를_보낸다() {
        LocalDate today = LocalDate.now();
        User user = User.builder()
                .oauthProvider("bsm")
                .oauthId("oauth-id")
                .name("홍길동")
                .email("hong@test.com")
                .role(User.Role.USER)
                .build();
        user.updateDiscordId("123456789");

        Item item = Item.builder()
                .name("지갑")
                .build();

        ItemClaim claim = ItemClaim.builder()
                .item(item)
                .claimant(user)
                .visitDate(today)
                .build();
        claim.approve();

        when(itemClaimRepository.findByStatusAndVisitDate(ItemClaim.ClaimStatus.APPROVED, today))
                .thenReturn(List.of(claim));

        pickupReminderScheduler.sendPickupReminders();

        ArgumentCaptor<LocalDateTime> pickupTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(discordBotClient).notifyPickupReminder(eq("123456789"), eq("지갑"), pickupTimeCaptor.capture());
        assertThat(pickupTimeCaptor.getValue()).isEqualTo(today.atTime(13, 10));
    }

    @Test
    void 디스코드가_없으면_리마인더를_보내지_않는다() {
        LocalDate today = LocalDate.now();
        User user = User.builder()
                .oauthProvider("bsm")
                .oauthId("oauth-id")
                .name("홍길동")
                .email("hong2@test.com")
                .role(User.Role.USER)
                .build();

        Item item = Item.builder()
                .name("지갑")
                .build();

        ItemClaim claim = ItemClaim.builder()
                .item(item)
                .claimant(user)
                .visitDate(today)
                .build();
        claim.approve();

        when(itemClaimRepository.findByStatusAndVisitDate(ItemClaim.ClaimStatus.APPROVED, today))
                .thenReturn(List.of(claim));

        pickupReminderScheduler.sendPickupReminders();

        verify(discordBotClient, never()).notifyPickupReminder(anyString(), anyString(), any());
    }
}
