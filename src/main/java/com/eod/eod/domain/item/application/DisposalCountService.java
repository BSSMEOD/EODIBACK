package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisposalCountService {

    private final ItemRepository itemRepository;

    public long getDisposalCount() {
        return itemRepository.countByStatus(Item.ItemStatus.TO_BE_DISCARDED);
    }
}
