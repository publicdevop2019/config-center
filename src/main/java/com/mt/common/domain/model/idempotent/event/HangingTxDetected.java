package com.mt.common.domain.model.idempotent.event;

import com.mt.common.domain.model.domainId.DomainId;
import com.mt.common.domain_event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class HangingTxDetected extends DomainEvent {
    public static final String MONITOR_TOPIC = "monitor";
    @Getter
    private String changeId;

    public HangingTxDetected(DomainId domainId, String changeId) {
        super(domainId);
        this.changeId = changeId;
        setInternal(false);
        setTopic(MONITOR_TOPIC);
    }
}
