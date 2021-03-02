package com.mt.common.domain.model.idempotent.event;

import com.mt.common.domain.model.domainId.DomainId;
import com.mt.common.domain.model.domain_event.DomainEvent;
import com.mt.common.domain.model.domain_event.StoredEvent;
import com.mt.common.domain.model.restful.PatchCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class SkuChangeFailed extends DomainEvent {
    public static final String MALL_MONITOR_TOPIC = "mall_monitor";
    @Getter
    private List<PatchCommand> changes;

    public SkuChangeFailed(DomainId domainId, List<PatchCommand> skuId) {
        super(domainId);
        this.changes = skuId;
        setInternal(false);
        setTopic(MALL_MONITOR_TOPIC);
    }
}
