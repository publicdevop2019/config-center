package com.mt.common.idempotent.model;

import com.mt.common.sql.RestfulQueryRegistry;
import org.springframework.stereotype.Component;

@Component
public class ChangeRecordQueryRegistry extends RestfulQueryRegistry<ChangeRecord> {

    @Override
    public Class<ChangeRecord> getEntityClass() {
        return ChangeRecord.class;
    }

}
