package com.mt.common.domain.model.idempotent;

import com.mt.common.domain.model.restful.query.QueryConfig;
import com.mt.common.domain.model.restful.query.PageConfig;
import com.mt.common.domain.model.restful.SumPagedRep;

public interface ChangeRecordRepository {
    SumPagedRep<ChangeRecord> changeRecordsOfQuery(ChangeRecordQuery changeRecordQuery, PageConfig pageConfig, QueryConfig queryConfig);

    void add(ChangeRecord changeRecord);
}
