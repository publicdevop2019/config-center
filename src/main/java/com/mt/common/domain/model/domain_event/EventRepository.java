package com.mt.common.domain.model.domain_event;

import java.util.List;

public interface EventRepository {

    List<StoredEvent> allStoredEventsSince(long lastId);

    void append(DomainEvent aDomainEvent);

}
