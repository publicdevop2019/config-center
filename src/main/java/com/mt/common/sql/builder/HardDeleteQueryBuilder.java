package com.mt.common.sql.builder;

import com.mt.common.sql.clause.SelectFieldIdWhereClause;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static com.mt.common.AppConstant.COMMON_ENTITY_ID;

public abstract class HardDeleteQueryBuilder<T> extends PredicateConfig<T> implements DeleteQueryBuilder<T> {
    @Autowired
    protected EntityManager em;
    protected HardDeleteQueryBuilder() {
        supportedWhereField.put(COMMON_ENTITY_ID, new SelectFieldIdWhereClause<>());
    }
    @Override
    public Integer delete(String search, Class<T> clazz) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<T> criteriaDeleteSku = cb.createCriteriaDelete(clazz);
        Root<T> root = criteriaDeleteSku.from(clazz);
        Predicate predicate = getPredicate(search, cb, root, null);
        criteriaDeleteSku.where(predicate);
        return em.createQuery(criteriaDeleteSku).executeUpdate();
    }
}
