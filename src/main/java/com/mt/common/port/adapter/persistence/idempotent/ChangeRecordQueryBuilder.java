package com.mt.common.port.adapter.persistence.idempotent;

import com.mt.common.domain.model.idempotent.ChangeRecord;
import com.mt.common.domain.model.sql.builder.SelectQueryBuilder;
import com.mt.common.domain.model.sql.clause.FieldStringEqualClause;
import org.springframework.stereotype.Component;

import static com.mt.common.domain.model.idempotent.ChangeRecord.CHANGE_ID;
import static com.mt.common.domain.model.idempotent.ChangeRecord.ENTITY_TYPE;

@Component
public class ChangeRecordQueryBuilder extends SelectQueryBuilder<ChangeRecord> {
    ChangeRecordQueryBuilder() {
        supportedSort.put("id","id");
        supportedWhere.put(ENTITY_TYPE, new FieldStringEqualClause<>(ENTITY_TYPE));
        supportedWhere.put(CHANGE_ID, new FieldStringEqualClause<>(CHANGE_ID));
    }
}
