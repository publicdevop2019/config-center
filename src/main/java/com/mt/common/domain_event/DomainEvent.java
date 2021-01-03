package com.mt.common.domain_event;

import com.mt.common.domain.model.CommonDomainRegistry;
import com.mt.common.domain.model.domainId.DomainId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(length=100)
public abstract class DomainEvent implements Serializable {

    @Id
    private Long id;

    private Long timestamp;

    @Embedded
    @AttributeOverride(name = "domainId", column = @Column(updatable = false))
    private DomainId domainId;

    @ElementCollection(fetch = FetchType.LAZY)
    @Embedded
    @CollectionTable(
            name = "domain_event_ids_map",
            joinColumns = @JoinColumn(name = "id", referencedColumnName = "id")
    )
    private Set<DomainId> domainIds;

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
