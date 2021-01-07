package com.mt.common.idempotent;

import com.mt.common.idempotent.model.ChangeRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ChangeRepository extends CrudRepository<ChangeRecord, Long> {
    Optional<ChangeRecord> findByChangeIdAndEntityType(String changeId, String entityType);
}
