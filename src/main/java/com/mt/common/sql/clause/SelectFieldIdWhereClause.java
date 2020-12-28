package com.mt.common.sql.clause;

import static com.mt.common.AppConstant.COMMON_ENTITY_ID;

public class SelectFieldIdWhereClause<T> extends SelectFieldLongEqualClause<T> {
    public SelectFieldIdWhereClause() {
        super(COMMON_ENTITY_ID);
    }
}
