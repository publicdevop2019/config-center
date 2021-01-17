package com.mt.common.idempotent;

import com.mt.common.domain.model.domainId.DomainId;
import com.mt.common.domain_event.DomainEvent;
import lombok.Getter;

public class HangingTxDetected extends DomainEvent {
    @Getter
    private final String changeId;

    public HangingTxDetected(DomainId domainId, String changeId) {
        super(domainId);
        this.changeId = changeId;
    }
}
