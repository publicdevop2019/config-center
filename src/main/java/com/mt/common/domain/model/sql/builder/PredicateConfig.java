package com.mt.common.domain.model.sql.builder;

import com.mt.common.domain.model.audit.Auditable;
import com.mt.common.domain.model.restful.query.QueryCriteria;
import com.mt.common.domain.model.sql.clause.NotDeletedClause;
import com.mt.common.domain.model.sql.clause.WhereClause;

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
        Predicate notSoftDeleted = new NotDeletedClause<T>().getWhereClause(cb, root, query);
        results.add(notSoftDeleted);
        return cb.and(results.toArray(new Predicate[0]));
    }

    public static class UnknownWhereClauseException extends RuntimeException {
    }
}
