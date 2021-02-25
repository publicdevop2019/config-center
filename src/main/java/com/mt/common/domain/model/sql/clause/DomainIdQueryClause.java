package com.mt.common.domain.model.sql.clause;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static com.mt.common.CommonConstant.DOMAIN_ID;

public class DomainIdQueryClause<T> extends FieldStringEqualClause<T> {
    public DomainIdQueryClause(String fieldName) {
        super(fieldName);
    }

    @Override
    protected Predicate getExpression(String input, CriteriaBuilder cb, Root<T> root) {
        return cb.equal(root.get(entityFieldName).get(DOMAIN_ID).as(String.class), input);
    }

}
