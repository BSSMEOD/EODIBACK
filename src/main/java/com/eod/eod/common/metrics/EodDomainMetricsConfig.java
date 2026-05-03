package com.eod.eod.common.metrics;

import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.reward.infrastructure.RewardRecordRepository;
import com.eod.eod.domain.user.model.User;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EodDomainMetricsConfig {

    public EodDomainMetricsConfig(
            MeterRegistry meterRegistry,
            ItemRepository itemRepository,
            ItemClaimRepository itemClaimRepository,
            RewardRecordRepository rewardRecordRepository
    ) {
        for (Item.ItemStatus status : Item.ItemStatus.values()) {
            Gauge.builder("eod_items_current", itemRepository,
                            repository -> repository.countByStatusAndDeletedAtIsNull(status))
                    .description("Current item count by status")
                    .tag("status", status.name())
                    .register(meterRegistry);
        }

        for (ItemClaim.ClaimStatus status : ItemClaim.ClaimStatus.values()) {
            Gauge.builder("eod_claims_current", itemClaimRepository,
                            repository -> repository.countByStatusAndItemDeletedAtIsNull(status))
                    .description("Current item claim count by status")
                    .tag("status", status.name())
                    .register(meterRegistry);
        }

        Gauge.builder("eod_reward_eligible_items", rewardRecordRepository,
                        repository -> repository.countRewardEligibleItems(Item.ItemStatus.GIVEN, User.Role.USER))
                .description("Current count of items eligible for reward")
                .register(meterRegistry);

        Gauge.builder("eod_reward_records_current", rewardRecordRepository, RewardRecordRepository::count)
                .description("Current reward record count")
                .register(meterRegistry);
    }
}
