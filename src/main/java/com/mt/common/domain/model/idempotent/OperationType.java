package com.mt.common.domain.model.idempotent;

import com.mt.common.domain.model.sql.converter.EnumConverter;

public enum OperationType {
    POST,
    PATCH_BATCH,
    PATCH_BY_ID,
    PUT,
    RESTORE_LAST_VERSION,
    EMPTY_OPT,
    RESTORE_DELETE,
    CANCEL_CREATE,
    DELETE_BY_QUERY;

    public static class DBConverter extends EnumConverter<OperationType> {
        public DBConverter() {
            super(OperationType.class);
        }
    }
}
