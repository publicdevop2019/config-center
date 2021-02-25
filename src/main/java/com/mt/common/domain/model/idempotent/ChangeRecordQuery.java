package com.mt.common.domain.model.idempotent;

import com.mt.common.domain.model.restful.query.QueryCriteria;

public class ChangeRecordQuery extends QueryCriteria {
    public ChangeRecordQuery(String queryParam) {
        super(queryParam);
    }
}
