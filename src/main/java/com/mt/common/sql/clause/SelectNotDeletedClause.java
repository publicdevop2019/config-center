package com.mt.common.sql.clause;

import com.mt.common.Auditable;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static com.mt.common.Auditable.ENTITY_DELETED;

public class SelectNotDeletedClause<T extends Auditable> {
    public SelectNotDeletedClause() {
    }

    public Predicate getWhereClause(CriteriaBuilder cb, Root<T> root, AbstractQuery<?> abstractQuery) {
        return cb.or(cb.isNull(root.get(ENTITY_DELETED)), cb.isFalse(root.get(ENTITY_DELETED)));
    }
}