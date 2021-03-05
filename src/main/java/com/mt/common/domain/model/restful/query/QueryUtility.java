package com.mt.common.domain.model.restful.query;

import com.mt.common.domain.model.audit.Auditable;
import com.mt.common.domain.model.restful.SumPagedRep;
import com.mt.common.domain.model.sql.builder.SqlSelectQueryConverter;
import com.mt.common.domain.model.sql.clause.NotDeletedClause;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component
public class QueryUtility {
    @Autowired
    private static EntityManager em;

    public static <T, S extends QueryCriteria> Set<T> getAllByQuery(BiFunction<S, PageConfig, SumPagedRep<T>> ofQuery, S query) {
        PageConfig queryPagingParam = new PageConfig();
        SumPagedRep<T> tSumPagedRep = ofQuery.apply(query, queryPagingParam);
        if (tSumPagedRep.getData().size() == 0)
            return new HashSet<>();
        double l = (double) tSumPagedRep.getTotalItemCount() / tSumPagedRep.getData().size();//for accuracy
        double ceil = Math.ceil(l);
        int i = BigDecimal.valueOf(ceil).intValue();
        Set<T> data = new HashSet<>(tSumPagedRep.getData());
        for (int a = 1; a < i; a++) {
            data.addAll(ofQuery.apply(query, queryPagingParam.pageOf(a)).getData());
        }
        return data;
    }

    public static <T> Set<T> getAllByQuery(Function<QueryCriteria, SumPagedRep<T>> ofQuery, QueryCriteria query) {
        SumPagedRep<T> tSumPagedRep = ofQuery.apply(query);
        if (tSumPagedRep.getData().size() == 0)
            return new HashSet<>();
        double l = (double) tSumPagedRep.getTotalItemCount() / tSumPagedRep.getData().size();//for accuracy
        double ceil = Math.ceil(l);
        int i = BigDecimal.valueOf(ceil).intValue();
        Set<T> data = new HashSet<>(tSumPagedRep.getData());
        for (int a = 1; a < i; a++) {
            data.addAll(ofQuery.apply(query.pageOf(a)).getData());
        }
        return data;
    }

    public static <T extends Auditable> SumPagedRep<T> pagedQuery(SqlSelectQueryConverter<T> queryBuilder, QueryCriteria query, PageConfig page, QueryConfig config, Class<T> clazz) {
        List<T> select = queryBuilder.select(query, page, clazz);
        Long aLong = null;
        if (config.count()) {
            aLong = queryBuilder.count(query, clazz);
        }
        return new SumPagedRep<>(select, aLong);
    }

    public static <T extends Auditable> SumPagedRep<T> pagedQuery(Predicate predicate, Order order, QueryCriteria queryCriteria, QueryContext<T> context) {
        Predicate notSoftDeleted = new NotDeletedClause<T>().getWhereClause(context.getCriteriaBuilder(), context.getRoot(), context.getQuery());
        Predicate extended = QueryUtility.combinePredicate(context, notSoftDeleted, predicate);
        List<T> select = QueryUtility.select(extended, order, queryCriteria.getPageConfig(), context);
        Long aLong = null;
        if (queryCriteria.count()) {
            aLong = QueryUtility.count(predicate, context);
        }
        return new SumPagedRep<>(select, aLong);
    }

    private static <T extends Auditable> Long count(Predicate predicate, QueryContext<T> context) {
        CriteriaQuery<Long> query = context.getCriteriaBuilder().createQuery(Long.class);
        Root<T> root = query.from(context.clazz);
        query.select(context.getCriteriaBuilder().count(root));
        query.where(predicate);
        TypedQuery<Long> query1 = em.createQuery(query);
        ((Query) query1).setHint("org.hibernate.cacheable", true);
        return query1.getSingleResult();
    }


    private static <T extends Auditable> List<T> select(Predicate predicate, Order order, PageConfig page, QueryContext<T> context) {
        CriteriaQuery<T> query = context.getQuery();
        Root<T> root = context.getRoot();
        query.select(root);
        query.where(predicate);
        query.orderBy(order);
        TypedQuery<T> query1 = em.createQuery(query)
                .setFirstResult(BigDecimal.valueOf(page.getOffset()).intValue())
                .setMaxResults(page.getPageSize());
        ((Query) query1).setHint("org.hibernate.cacheable", true);
        return query1.getResultList();
    }

    public static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> parsed = new HashMap<>();
        if (rawQuery != null) {
            String[] split = rawQuery.split(",");
            for (String str : split) {
                String[] split1 = str.split(":");
                if (split1.length != 2)
                    throw new QueryParseException();
                parsed.put(split1[0], split1[1]);
            }
        }
        return parsed;
    }

    public static <T> QueryContext<T> prepareContext(Class<T> clazz) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(clazz);
        Root<T> root = query.from(clazz);
        return new QueryContext<>(criteriaBuilder, query, root, null, clazz);
    }

    public static <T> Predicate getStringEqualPredicate(String value, String sqlFieldName, QueryContext<T> queryContext) {
        return queryContext.getCriteriaBuilder().equal(queryContext.getRoot().get(sqlFieldName).as(String.class), value);
    }

    public static <T> Predicate combinePredicate(QueryContext<T> queryContext, Predicate... stringEqualPredicate) {
        List<Predicate> results = List.of(stringEqualPredicate);
        return queryContext.getCriteriaBuilder().and(results.toArray(new Predicate[0]));
    }

    public static class QueryParseException extends RuntimeException {
    }

    @Getter
    public static class QueryContext<T> {
        private final CriteriaBuilder criteriaBuilder;
        private final Root<T> root;
        private final AbstractQuery<?> abstractQuery;
        private final CriteriaQuery<T> query;
        private final Class<T> clazz;

        public QueryContext(CriteriaBuilder cb, CriteriaQuery<T> query, Root<T> root, AbstractQuery<?> abstractQuery, Class<T> clazz) {
            this.criteriaBuilder = cb;
            this.root = root;
            this.abstractQuery = abstractQuery;
            this.query = query;
            this.clazz = clazz;
        }
    }
}
