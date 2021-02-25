package com.mt.common.domain.model.idempotent;

import com.mt.common.persistence.QueryConfig;
import com.mt.common.query.PageConfig;
import com.mt.common.sql.SumPagedRep;

public interface ChangeRecordRepository {
    SumPagedRep<ChangeRecord> changeRecordsOfQuery(ChangeRecordQuery changeRecordQuery, PageConfig pageConfig, QueryConfig queryConfig);

    void add(ChangeRecord changeRecord);
}
