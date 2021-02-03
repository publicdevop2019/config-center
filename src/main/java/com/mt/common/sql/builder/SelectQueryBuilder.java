package com.mt.common.sql.builder;

import com.mt.common.audit.Auditable;
import com.mt.common.sql.clause.SelectFieldIdWhereClause;
import com.mt.common.sql.clause.SelectNotDeletedClause;
import com.mt.common.sql.exception.MaxPageSizeExceedException;
import com.mt.common.sql.exception.UnsupportedQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mt.common.CommonConstant.COMMON_ENTITY_ID;

public abstract class SelectQueryBuilder<T extends Auditable> extends PredicateConfig<T> {
    protected Integer DEFAULT_PAGE_SIZE = 10;
    protected Integer MAX_PAGE_SIZE = 20;
    protected Integer DEFAULT_PAGE_NUM = 0;
    protected String DEFAULT_SORT_BY = COMMON_ENTITY_ID;
    protected Map<String, String> mappedSortBy = new HashMap<>();
    protected Sort.Direction DEFAULT_SORT_ORDER = Sort.Direction.ASC;
    @Autowired
    protected EntityManager em;

    protected SelectQueryBuilder() {
        mappedSortBy.put(COMMON_ENTITY_ID, COMMON_ENTITY_ID);
        supportedWhereField.put(COMMON_ENTITY_ID, new SelectFieldIdWhereClause<>());
    }

    public List<T> select(String search, String page, Class<T> clazz) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(clazz);
        Root<T> root = query.from(clazz);
        query.select(root);
        PageRequest pageRequest = getPageRequest(page);
        Predicate and = getPredicateEx(search, cb, root, query);

        if (and != null)
            query.where(and);
        if (sortConverter != null) {
            List<Order> collect = sortConverter.getOrderClause(page, cb, root, query);
            query.orderBy(collect);
        } else {
            Set<Order> collect = pageRequest.getSort().get().map(e -> {
                if (e.getDirection().isAscending()) {
                    return cb.asc(root.get(e.getProperty()));
                } else {
                    return cb.desc(root.get(e.getProperty()));
                }
            }).collect(Collectors.toSet());
            query.orderBy(collect.toArray(Order[]::new));
        }

        TypedQuery<T> query1 = em.createQuery(query)
                .setFirstResult(BigDecimal.valueOf(pageRequest.getOffset()).intValue())
                .setMaxResults(pageRequest.getPageSize());
        ((Query) query1).setHint("org.hibernate.cacheable", true);
        return query1.getResultList();
    }

    private Predicate getPredicateEx(String search, CriteriaBuilder cb, Root<T> root, AbstractQuery<?> query) {
        Predicate predicateEx = super.getPredicate(search, cb, root, query);
        //force to select only not deleted entity
        Predicate notSoftDeleted = new SelectNotDeletedClause<T>().getWhereClause(cb, root, query);
        return cb.and(predicateEx, notSoftDeleted);
    }

    public Long selectCount(String search, Class<T> clazz) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(clazz);
        query.select(cb.count(root));
        Predicate and = getPredicateEx(search, cb, root, query);
        if (and != null)
            query.where(and);
        TypedQuery<Long> query1 = em.createQuery(query);
        ((Query) query1).setHint("org.hibernate.cacheable", true);
        return query1.getSingleResult();
    }

    private PageRequest getPageRequest(String page) {
        if (page == null) {
            Sort sort = new Sort(DEFAULT_SORT_ORDER, mappedSortBy.get(DEFAULT_SORT_BY));
            return PageRequest.of(DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE, sort);
        }
        String[] params = page.split(",");
        Integer pageNumber = DEFAULT_PAGE_NUM;
        Integer pageSize = DEFAULT_PAGE_SIZE;
        String sortBy = mappedSortBy.get(DEFAULT_SORT_BY);
        Sort.Direction sortOrder = DEFAULT_SORT_ORDER;
        for (String param : params) {
            String[] values = param.split(":");
            if (values[0].equals("num") && values[1] != null) {
                pageNumber = Integer.parseInt(values[1]);
            }
            if (values[0].equals("size") && values[1] != null) {
                pageSize = Integer.parseInt(values[1]);
            }
            if (sortConverter == null) {
                if (values[0].equals("by") && values[1] != null) {
                    sortBy = mappedSortBy.get(values[1]);
                    if (sortBy == null)
                        throw new UnsupportedQueryException();
                }
            }
            if (values[0].equals("order") && values[1] != null) {
                sortOrder = Sort.Direction.fromString(values[1]);
            }
        }
        if (pageSize > MAX_PAGE_SIZE)
            throw new MaxPageSizeExceedException();
        Sort sort = new Sort(sortOrder, sortBy);
        return PageRequest.of(pageNumber, pageSize, sort);
    }

}
