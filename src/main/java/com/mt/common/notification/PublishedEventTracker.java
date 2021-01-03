package com.mt.common.notification;

import com.mt.common.domain.model.CommonDomainRegistry;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
@NoArgsConstructor
public class PublishedEventTracker {
    @Id
    private final Long id = CommonDomainRegistry.uniqueIdGeneratorService().id();
    @Version
    private int version;
    @Setter
    @Getter
    private long lastPublishedEventId;
}
