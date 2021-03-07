package com.mt.common.domain.model.restful.query;

import com.mt.common.CommonConstant;
import com.mt.common.domain.model.audit.Auditable;
import com.mt.common.domain.model.restful.SumPagedRep;
import com.mt.common.domain.model.sql.builder.SqlSelectQueryConverter;
import com.mt.common.domain.model.sql.clause.NotDeletedClause;
import com.mt.common.domain.model.sql.exception.UnsupportedQueryException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
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
    private static EntityManager em;

    @Autowired
    public void setEntityManager(EntityManager em) {
        QueryUtility.em = em;
    }

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
        return pagedQuery(predicate, null, order, queryCriteria, context);
    }

    public static <T extends Auditable> SumPagedRep<T> pagedQuery(Predicate predicate, @Nullable Predicate countPredicate, Order order, QueryCriteria queryCriteria, QueryContext<T> context) {
        Predicate notSoftDeleted = new NotDeletedClause<T>().getWhereClause(context.getCriteriaBuilder(), context.getRoot(), context.getQuery());
        Predicate extended = QueryUtility.combinePredicate(context, notSoftDeleted, predicate);
        List<T> select = QueryUtility.select(extended, order, queryCriteria.getPageConfig(), context);
        Long aLong = null;
        if (queryCriteria.count()) {
            aLong = QueryUtility.count(countPredicate == null ? predicate : countPredicate, context);
        }
        return new SumPagedRep<>(select, aLong);
    }

    private static <T extends Auditable> Long count(Predicate predicate, QueryContext<T> context) {
        CriteriaQuery<Long> query = context.getCountQuery();
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
        return new QueryContext<>(criteriaBuilder, query, root, criteriaBuilder.createQuery(Long.class), clazz, new ArrayList<>());
    }

    public static <T> Predicate getStringEqualPredicate(String value, String sqlFieldName, QueryContext<T> queryContext) {
        return queryContext.getCriteriaBuilder().equal(queryContext.getRoot().get(sqlFieldName).as(String.class), value);
    }

    public static <T> Predicate combinePredicate(QueryContext<T> queryContext, Predicate... stringEqualPredicate) {
        List<Predicate> results = List.of(stringEqualPredicate);
        return queryContext.getCriteriaBuilder().and(results.toArray(new Predicate[0]));
    }

    public static <T> Predicate combinePredicate(QueryContext<T> queryContext, List<Predicate> predicates) {
        return queryContext.getCriteriaBuilder().and(predicates.toArray(new Predicate[0]));
    }

    public static <T> Order getOrder(String fieldName, QueryContext<T> queryContext, boolean isAsc) {
        Order order;
        if (isAsc) {
            order = queryContext.getCriteriaBuilder().asc(queryContext.getRoot().get(fieldName));
        } else {
            order = queryContext.getCriteriaBuilder().desc(queryContext.getRoot().get(fieldName));
        }
        return order;
    }

    public static <T> Predicate getStringInPredicate(Set<String> collect, String fieldName, QueryContext<T> queryContext) {
        return queryContext.getRoot().get(fieldName).as(String.class).in(collect);
    }

    public static <T> Predicate getDomainIdInPredicate(Set<String> collect, String catalogIdLiteral, QueryContext<T> queryContext) {
        return queryContext.getRoot().get(catalogIdLiteral).get(CommonConstant.DOMAIN_ID).as(String.class).in(collect);
    }

    public static <T> Predicate getStringLikePredicate(String value, String sqlFieldName, QueryContext<T> queryContext) {
        return queryContext.getCriteriaBuilder().like(queryContext.getRoot().get(sqlFieldName).as(String.class), value);
    }

    public static <T> Predicate getNumberRagePredicate(String query, String entityFieldName, QueryContext<T> queryContext) {
        CriteriaBuilder cb = queryContext.getCriteriaBuilder();
        Root<T> root = queryContext.getRoot();
        String[] split = query.split("\\$");
        List<Predicate> results = new ArrayList<>();
        for (String str : split) {
            if (str.contains("<=")) {
                int i = Integer.parseInt(str.replace("<=", ""));
                results.add(cb.lessThanOrEqualTo(root.get(entityFieldName), i));
            } else if (str.contains(">=")) {
                int i = Integer.parseInt(str.replace(">=", ""));
                results.add(cb.greaterThanOrEqualTo(root.get(entityFieldName), i));
            } else if (str.contains("<")) {
                int i = Integer.parseInt(str.replace("<", ""));
                results.add(cb.lessThan(root.get(entityFieldName), i));
            } else if (str.contains(">")) {
                int i = Integer.parseInt(str.replace(">", ""));
                results.add(cb.greaterThan(root.get(entityFieldName), i));
            } else {
                throw new UnsupportedQueryException();
            }
        }
        return cb.and(results.toArray(new Predicate[0]));
    }

    public static class QueryParseException extends RuntimeException {
    }

    @Getter
    public static class QueryContext<T> {
        private final CriteriaBuilder criteriaBuilder;
        private final Root<T> root;
        private final CriteriaQuery<Long> countQuery;
        private final CriteriaQuery<T> query;
        private final Class<T> clazz;
        private final List<Predicate> predicates;

        public QueryContext(CriteriaBuilder cb, CriteriaQuery<T> query, Root<T> root, CriteriaQuery<Long> countQuery, Class<T> clazz, List<Predicate> predicates) {
            this.criteriaBuilder = cb;
            this.root = root;
            this.countQuery = countQuery;
            this.query = query;
            this.clazz = clazz;
            this.predicates = predicates;
        }
    }
}
