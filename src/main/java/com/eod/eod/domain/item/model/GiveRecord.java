package com.eod.eod.domain.item.model;

import com.eod.eod.domain.user.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "give_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GiveRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giver_id", nullable = false)
    private User giver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "status", nullable = false)
    private Boolean status = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public GiveRecord(Item item, User giver, User receiver, Boolean status) {
        this.item = item;
        this.giver = giver;
        this.receiver = receiver;
        this.status = status != null ? status : false;
        this.createdAt = LocalDateTime.now();
    }
}