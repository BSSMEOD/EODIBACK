package com.eod.eod.domain.item.presentation.dto.response;

import com.eod.eod.domain.item.model.ItemClaim;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ClaimRequestsResponse {

    private int page;
    private int size;
    private long total;
    private List<ClaimRequestDto> requests;

    public static ClaimRequestsResponse from(Page<ItemClaim> claimPage, int pageNumber) {
        List<ClaimRequestDto> requests = claimPage.getContent().stream()
                .map(ClaimRequestDto::from)
                .collect(Collectors.toList());

        return ClaimRequestsResponse.builder()
                .page(pageNumber)
                .size(claimPage.getSize())
                .total(claimPage.getTotalElements())
                .requests(requests)
                .build();
    }
}