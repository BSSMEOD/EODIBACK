package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.model.GiveRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiveRecordRepository extends JpaRepository<GiveRecord, Long> {
}