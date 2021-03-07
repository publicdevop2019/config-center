package com.mt.common.domain.model.sql.clause;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class FieldBooleanEqualClause<T> extends WhereClause<T> {
    public FieldBooleanEqualClause(String fieldName) {
        entityFieldName = fieldName;
    }

    @Override
    public Predicate getWhereClause(String query, CriteriaBuilder cb, Root<T> root, AbstractQuery<?> abstractQuery) {
        if ("1".equals(query))
            return cb.isTrue(root.get(entityFieldName));
        return cb.isFalse(root.get(entityFieldName));
    }
}