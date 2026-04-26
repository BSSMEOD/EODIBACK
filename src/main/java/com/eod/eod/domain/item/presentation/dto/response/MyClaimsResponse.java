package com.eod.eod.domain.item.presentation.dto.response;

import com.eod.eod.domain.item.model.ItemClaim;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MyClaimsResponse {

    private int page;
    private int size;
    private long total;
    private List<MyClaimResponse> claims;

    public static MyClaimsResponse from(Page<ItemClaim> claimPage, int pageNumber) {
        List<MyClaimResponse> claims = claimPage.getContent().stream()
                .map(MyClaimResponse::from)
                .collect(Collectors.toList());

        return MyClaimsResponse.builder()
                .page(pageNumber)
                .size(claimPage.getSize())
                .total(claimPage.getTotalElements())
                .claims(claims)
                .build();
    }
}
