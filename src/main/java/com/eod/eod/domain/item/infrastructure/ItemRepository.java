package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {
    Optional<Item> findByIdAndDeletedAtIsNull(Long id);

    // 폐기 예정일이 지난 물품 조회 (자동 폐기용)
    List<Item> findByStatusAndDiscardedAtBeforeAndDeletedAtIsNull(Item.ItemStatus status, LocalDateTime dateTime);

    // 장기 방치된 분실물 조회 (자동 폐기 예정 전환용)
    List<Item> findByStatusAndCreatedAtBeforeAndDeletedAtIsNull(Item.ItemStatus status, LocalDateTime dateTime);

    // 특정 상태의 물품 개수 조회
    long countByStatusAndDeletedAtIsNull(Item.ItemStatus status);

    // 상점 지급 리스트: 주인에게 지급된 물품 중 습득 신고자가 있는 목록
    @Query("SELECT i FROM Item i " +
            "JOIN FETCH i.student s " +
            "WHERE i.status = :status " +
            "AND s.role = :role " +
            "AND i.deletedAt IS NULL " +
            "ORDER BY i.approvedAt DESC")
    List<Item> findRewardRequestList(@Param("status") Item.ItemStatus status, @Param("role") User.Role role);
}
