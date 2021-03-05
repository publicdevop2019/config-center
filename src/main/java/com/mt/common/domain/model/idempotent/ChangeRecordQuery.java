package com.mt.common.domain.model.idempotent;

import com.mt.common.domain.model.restful.query.PageConfig;
import com.mt.common.domain.model.restful.query.QueryCriteria;

public class ChangeRecordQuery implements QueryCriteria {
    public ChangeRecordQuery(String queryParam) {

    }

    @Override
    public QueryCriteria pageOf(int a) {
        return null;
    }

    @Override
    public boolean count() {
        return false;
    }

    @Override
    public PageConfig getPageConfig() {
        return null;
    }
}
