package com.hw.shared.idempotent.model;

import com.hw.shared.sql.builder.SelectQueryBuilder;
import com.hw.shared.sql.clause.SelectFieldStringEqualClause;
import com.hw.shared.sql.clause.SelectFieldStringLikeClause;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

import static com.hw.shared.idempotent.model.ChangeRecord.ENTITY_TYPE;

@Component
public class RootChangeRecordSelectQueryBuilder extends SelectQueryBuilder<ChangeRecord> {
    RootChangeRecordSelectQueryBuilder() {
        supportedWhereField.put(ENTITY_TYPE, new SelectFieldStringEqualClause<>(ENTITY_TYPE));
        allowEmptyClause = true;
    }

    @Autowired
    private void setEntityManager(EntityManager entityManager) {
        em = entityManager;
    }
}