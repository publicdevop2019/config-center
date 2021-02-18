package com.mt.common.sql.builder;

import com.mt.common.audit.Auditable;
import com.mt.common.audit.AuditorAwareImpl;
import com.mt.common.query.QueryCriteria;
import com.mt.common.sql.clause.SelectFieldIdWhereClause;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.Optional;

import static com.mt.common.CommonConstant.COMMON_ENTITY_ID;
import static com.mt.common.audit.Auditable.*;

@Slf4j
public abstract class SoftDeleteQueryBuilder<T extends Auditable> extends PredicateConfig<T> implements DeleteQueryBuilder<T>{
    @Autowired
    protected EntityManager em;

    protected SoftDeleteQueryBuilder() {
        supportedWhere.put(COMMON_ENTITY_ID, new SelectFieldIdWhereClause<>());
    }
    @Override
    public Integer delete(String search, Class<T> clazz) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<T> criteriaUpdate = cb.createCriteriaUpdate(clazz);
        Root<T> root = criteriaUpdate.from(clazz);
        Predicate and = getPredicate(new QueryCriteria(search), cb, root, null);
        criteriaUpdate.where(and);
        criteriaUpdate.set(ENTITY_DELETED, true);
        Optional<String> currentAuditor = AuditorAwareImpl.getAuditor();
        criteriaUpdate.set(ENTITY_DELETED_BY, currentAuditor.orElse(""));
        criteriaUpdate.set(ENTITY_DELETED_AT, new Date());
        return em.createQuery(criteriaUpdate).executeUpdate();
    }


}
