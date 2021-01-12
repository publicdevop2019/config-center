package com.mt.common.domain_event;

import com.mt.common.domain.model.CommonDomainRegistry;
import com.mt.common.domain.model.domainId.DomainId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor
public class DomainEvent implements Serializable {

    private Long id;

    private Long timestamp;

    private DomainId domainId;

    private Set<DomainId> domainIds;
    private boolean internal = true;
    private String topic;

    public DomainEvent(DomainId domainId) {
        setId(CommonDomainRegistry.uniqueIdGeneratorService().id());
        setTimestamp(new Date().getTime());
        setDomainId(domainId);
    }

    public DomainEvent(Set<DomainId> domainIds) {
        setId(CommonDomainRegistry.uniqueIdGeneratorService().id());
        setTimestamp(new Date().getTime());
        setDomainIds(domainIds);
    }
}
