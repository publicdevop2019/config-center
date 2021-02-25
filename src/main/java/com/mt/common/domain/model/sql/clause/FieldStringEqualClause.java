package com.mt.common.domain.model.sql.clause;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class FieldStringEqualClause<T> extends FieldStringLikeClause<T> {
    public FieldStringEqualClause(String fieldName) {
        super(fieldName);
    }

    @Override
    protected Predicate getExpression(String input, CriteriaBuilder cb, Root<T> root) {
        return cb.equal(root.get(entityFieldName).as(String.class), input);
    }

}
