package com.mt.common.sql.builder;

import com.mt.common.audit.Auditable;
import com.mt.common.query.QueryCriteria;
import com.mt.common.sql.clause.SelectNotDeletedClause;
import com.mt.common.sql.clause.WhereClause;
import com.mt.common.sql.exception.UnknownWhereClauseException;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredicateConfig<T extends Auditable> {
    protected Map<String, WhereClause<T>> supportedWhere = new HashMap<>();

    protected Predicate getPredicate(QueryCriteria queryConfig, CriteriaBuilder cb, Root<T> root, AbstractQuery<?> query) {
        List<Predicate> results = new ArrayList<>();
        if (queryConfig != null) {
            queryConfig.getParsed().forEach((k, v) -> {
                if (supportedWhere.get(k) == null)
                    throw new UnknownWhereClauseException();
                if (supportedWhere.get(k) != null && !v.isBlank()) {
                    WhereClause<T> tWhereClause = supportedWhere.get(k);
                    Predicate whereClause = tWhereClause.getWhereClause(v, cb, root, query);
                    results.add(whereClause);
                }
            });
        }
        Predicate notSoftDeleted = new SelectNotDeletedClause<T>().getWhereClause(cb, root, query);
        results.add(notSoftDeleted);
        return cb.and(results.toArray(new Predicate[0]));
    }

}
