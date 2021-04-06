package com.mt.common.domain.model.idempotent;

import com.mt.common.domain.model.restful.SumPagedRep;

public interface ChangeRecordRepository {
    SumPagedRep<ChangeRecord> changeRecordsOfQuery(ChangeRecordQuery changeRecordQuery);

    void add(ChangeRecord changeRecord);

    void addIfChangeNotFound(ChangeRecord changeRecord1);
}
