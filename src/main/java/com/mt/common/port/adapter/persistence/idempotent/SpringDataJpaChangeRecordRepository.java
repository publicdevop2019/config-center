package com.mt.common.port.adapter.persistence.idempotent;

import com.mt.common.domain.model.idempotent.ChangeRecord;
import com.mt.common.domain.model.idempotent.ChangeRecordQuery;
import com.mt.common.domain.model.idempotent.ChangeRecordRepository;
import com.mt.common.persistence.QueryConfig;
import com.mt.common.port.adapter.persistence.CommonQueryBuilderRegistry;
import com.mt.common.query.PageConfig;
import com.mt.common.query.QueryUtility;
import com.mt.common.sql.SumPagedRep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJpaChangeRecordRepository extends ChangeRecordRepository, JpaRepository<ChangeRecord, Long> {
    default SumPagedRep<ChangeRecord> changeRecordsOfQuery(ChangeRecordQuery changeRecordQuery, PageConfig pageConfig, QueryConfig queryConfig) {
        return QueryUtility.pagedQuery(CommonQueryBuilderRegistry.getChangeRecordQueryBuilder(), changeRecordQuery, pageConfig, queryConfig, ChangeRecord.class);
    }

    default void add(ChangeRecord changeRecord) {
        save(changeRecord);
    }
}
