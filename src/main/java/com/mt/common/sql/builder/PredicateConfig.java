package com.mt.common.sql.builder;

import com.mt.common.audit.Auditable;
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

    protected Predicate getPredicate(String search, CriteriaBuilder cb, Root<T> root, AbstractQuery<?> query) {
        List<Predicate> results = new ArrayList<>();
        if (search != null) {
            String[] queryParams = search.split(",");
            for (String param : queryParams) {
                String[] split = param.split(":");
                if (supportedWhere.get(split[0]) == null)
                    throw new UnknownWhereClauseException();
                if (supportedWhere.get(split[0]) != null && !split[1].isBlank()) {
                    WhereClause<T> tWhereClause = supportedWhere.get(split[0]);
                    Predicate whereClause = tWhereClause.getWhereClause(split[1], cb, root, query);
                    results.add(whereClause);
                }
            }
        }
        Predicate notSoftDeleted = new SelectNotDeletedClause<T>().getWhereClause(cb, root, query);
        results.add(notSoftDeleted);
        return cb.and(results.toArray(new Predicate[0]));
    }

}
