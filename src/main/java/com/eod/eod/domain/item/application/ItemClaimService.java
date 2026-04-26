package com.eod.eod.domain.item.application;

import com.eod.eod.common.annotation.RequireAdmin;
import com.eod.eod.domain.item.exception.ItemConflictException;
import com.eod.eod.domain.item.exception.ItemForbiddenException;
import com.eod.eod.domain.item.exception.ItemResourceNotFoundException;
import com.eod.eod.domain.item.infrastructure.GiveRecordRepository;
import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.model.GiveRecord;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.item.presentation.dto.response.ItemClaimResponse;
import com.eod.eod.domain.user.model.User;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemClaimService {

    private final ItemFacade itemFacade;
    private final ItemClaimRepository itemClaimRepository;
    private final GiveRecordRepository giveRecordRepository;

    public ItemClaimResponse claimItem(Long itemId, LocalDate visitDate, User currentUser) {
        // 아이템 존재 여부 확인
        Item item = itemFacade.getItemById(itemId);

        // 소유권 주장 가능 여부 검증은 도메인에서 처리
        item.validateClaimableBy(currentUser);

        // 중복 주장 확인 (PENDING 상태인 주장이 있는 경우에만 중복으로 간주)
        if (itemClaimRepository.existsByItemIdAndClaimantIdAndStatus(itemId, currentUser.getId(), ItemClaim.ClaimStatus.PENDING)) {
            throw new ItemConflictException("이미 해당 분실물에 대해 소유권을 주장하셨습니다.");
        }

        // 소유권 주장 생성
        ItemClaim claim = ItemClaim.builder()
                .item(item)
                .claimant(currentUser)
                .visitDate(visitDate)
                .build();

        itemClaimRepository.save(claim);

        return ItemClaimResponse.success();
    }

    /**
     * 소유권 주장 승인
     * 같은 물품에 대한 다른 PENDING 상태의 주장들은 자동으로 거절됨
     */
    @RequireAdmin
    public void approveClaim(Long claimId, User currentUser) {
        // 소유권 주장 조회
        ItemClaim claim = itemClaimRepository.findById(claimId)
                .orElseThrow(() -> new ItemResourceNotFoundException("해당 소유권 주장을 찾을 수 없습니다."));

        // 승인 처리
        claim.approve();

        // 물품 승인 처리 (소유권 승인 시 물품도 함께 승인)
        Item item = claim.getItem();
        item.processApproval(Item.ApprovalStatus.APPROVED, currentUser);

        // 지급 기록 생성 (수령자: 소유권 주장자, 지급자: 관리자)
        GiveRecord giveRecord = GiveRecord.builder()
                .item(item)
                .giver(currentUser)
                .receiver(claim.getClaimant())
                .build();
        giveRecordRepository.save(giveRecord);

        // 같은 물품에 대한 다른 PENDING 상태의 주장들을 모두 거절
        Long itemId = item.getId();
        List<ItemClaim> otherPendingClaims = itemClaimRepository
                .findByItemIdAndStatus(itemId, ItemClaim.ClaimStatus.PENDING);

        for (ItemClaim otherClaim : otherPendingClaims) {
            if (!otherClaim.getId().equals(claimId)) {
                otherClaim.reject();
            }
        }
    }

    /**
     * 소유권 주장 거절
     */
    @RequireAdmin
    public void rejectClaim(Long claimId, User currentUser) {
        // 소유권 주장 조회
        ItemClaim claim = itemClaimRepository.findById(claimId)
                .orElseThrow(() -> new ItemResourceNotFoundException("해당 소유권 주장을 찾을 수 없습니다."));

        // 거절 처리
        claim.reject();
    }

    /**
     * 소유권 주장 취소 (본인만 가능, 관리자 승인 전에만 가능)
     */
    public void cancelClaim(Long claimId, User currentUser) {
        // 소유권 주장 조회
        ItemClaim claim = itemClaimRepository.findById(claimId)
                .orElseThrow(() -> new ItemResourceNotFoundException("해당 소유권 주장을 찾을 수 없습니다."));

        // 본인 확인
        if (!claim.getClaimant().getId().equals(currentUser.getId())) {
            throw new ItemForbiddenException("본인의 소유권 주장만 취소할 수 있습니다.");
        }

        // PENDING 상태인지 확인
        if (claim.getStatus() != ItemClaim.ClaimStatus.PENDING) {
            throw new ItemConflictException("대기 중인 소유권 주장만 취소할 수 있습니다.");
        }

        // 취소 처리 - 레코드 삭제
        itemClaimRepository.delete(claim);
    }
}
