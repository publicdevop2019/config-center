package com.mt.common.domain_event;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends CrudRepository<StoredEvent, Long> {
    List<StoredEvent> findByIdGreaterThan(long id);

    default List<StoredEvent> allStoredEventsSince(long aStoredEventId) {
        return findByIdGreaterThan(aStoredEventId);
    }

    default void append(DomainEvent aDomainEvent) {
        save(new StoredEvent(aDomainEvent));
    }

}
