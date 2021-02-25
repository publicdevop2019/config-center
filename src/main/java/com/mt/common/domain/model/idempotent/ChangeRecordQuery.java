package com.mt.common.domain.model.idempotent;

import com.mt.common.query.QueryCriteria;

public class ChangeRecordQuery extends QueryCriteria {
    public ChangeRecordQuery(String queryParam) {
        super(queryParam);
    }
}
