package com.mt.common.domain.model.sql.builder;

import com.mt.common.domain.model.audit.Auditable;
import com.mt.common.domain.model.restful.query.PageConfig;
import com.mt.common.domain.model.restful.query.QueryCriteria;
import com.mt.common.domain.model.sql.clause.OrderClause;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SelectQueryBuilder<T extends Auditable> extends PredicateConfig<T> {
    @Autowired
    protected EntityManager em;
    protected OrderClause<T> sortConverter;
    protected Map<String, String> supportedSort = new HashMap<>();

    public List<T> select(@NotNull QueryCriteria search, @NotNull PageConfig page, @NotNull Class<T> clazz) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(clazz);
        Root<T> root = query.from(clazz);
        query.select(root);
        Predicate and = getPredicate(search, cb, root, query);
        if (and != null)
            query.where(and);
        if (sortConverter != null) {
            List<Order> collect = sortConverter.getOrderClause(page.value(), cb, root, query);
            query.orderBy(collect);
        } else {
            Order order;
            if (page.isSortOrderAsc()) {
                order = cb.asc(root.get(page.getSortBy()));
            } else {
                order = cb.desc(root.get(page.getSortBy()));
            }
            query.orderBy(order);
        }

        TypedQuery<T> query1 = em.createQuery(query)
                .setFirstResult(BigDecimal.valueOf(page.getOffset()).intValue())
                .setMaxResults(page.getPageSize());
        ((Query) query1).setHint("org.hibernate.cacheable", true);
        return query1.getResultList();
    }


    public Long count(QueryCriteria search, Class<T> clazz) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(clazz);
        query.select(cb.count(root));
        Predicate and = getPredicate(search, cb, root, query);
        if (and != null)
            query.where(and);
        TypedQuery<Long> query1 = em.createQuery(query);
        ((Query) query1).setHint("org.hibernate.cacheable", true);
        return query1.getSingleResult();
    }
}
