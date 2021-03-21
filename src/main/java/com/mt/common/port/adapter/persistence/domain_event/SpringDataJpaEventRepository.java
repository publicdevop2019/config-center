package com.mt.common.port.adapter.persistence.domain_event;

import com.mt.common.domain.model.domain_event.DomainEvent;
import com.mt.common.domain.model.domain_event.EventRepository;
import com.mt.common.domain.model.domain_event.StoredEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SpringDataJpaEventRepository extends CrudRepository<StoredEvent, Long>, EventRepository {
    List<StoredEvent> findByIdGreaterThan(long id);

    default List<StoredEvent> allStoredEventsSince(long lastId) {
        return findByIdGreaterThan(lastId);
    }

    default void append(DomainEvent aDomainEvent) {
        save(new StoredEvent(aDomainEvent));
    }

}
