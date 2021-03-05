package com.mt.common.domain.model.restful.query;

public interface QueryCriteria {
    QueryCriteria pageOf(int a);
    boolean count();
    PageConfig getPageConfig();
}
