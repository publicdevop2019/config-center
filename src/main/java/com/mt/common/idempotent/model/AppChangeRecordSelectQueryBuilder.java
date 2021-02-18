package com.mt.common.idempotent.model;

import com.mt.common.sql.builder.SelectQueryBuilder;
import com.mt.common.sql.clause.SelectFieldStringEqualClause;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

import static com.mt.common.idempotent.model.ChangeRecord.CHANGE_ID;
import static com.mt.common.idempotent.model.ChangeRecord.ENTITY_TYPE;

@Component
public class AppChangeRecordSelectQueryBuilder extends SelectQueryBuilder<ChangeRecord> {
    AppChangeRecordSelectQueryBuilder() {
        supportedWhere.put(ENTITY_TYPE, new SelectFieldStringEqualClause<>(ENTITY_TYPE));
        supportedWhere.put(CHANGE_ID, new SelectFieldStringEqualClause<>(CHANGE_ID));
    }

    @Autowired
    private void setEntityManager(EntityManager entityManager) {
        em = entityManager;
    }
}
