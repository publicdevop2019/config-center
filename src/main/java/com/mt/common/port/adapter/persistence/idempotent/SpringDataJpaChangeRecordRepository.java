package com.mt.common.port.adapter.persistence.idempotent;

import com.mt.common.domain.model.idempotent.ChangeRecord;
import com.mt.common.domain.model.idempotent.ChangeRecordQuery;
import com.mt.common.domain.model.idempotent.ChangeRecordRepository;
import com.mt.common.domain.model.restful.SumPagedRep;
import com.mt.common.domain.model.restful.query.QueryUtility;
import com.mt.common.port.adapter.persistence.CommonQueryBuilderRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;

import static com.mt.common.domain.model.idempotent.ChangeRecord.CHANGE_ID;
import static com.mt.common.domain.model.idempotent.ChangeRecord.ENTITY_TYPE;

@Repository
public interface SpringDataJpaChangeRecordRepository extends ChangeRecordRepository, JpaRepository<ChangeRecord, Long> {
    default SumPagedRep<ChangeRecord> changeRecordsOfQuery(ChangeRecordQuery changeRecordQuery) {
        return CommonQueryBuilderRegistry.getChangeRecordQueryBuilder().execute(changeRecordQuery);
    }

    default void add(ChangeRecord changeRecord) {
        save(changeRecord);
    }

    @Component
    class JpaCriteriaApiChangeRecordAdaptor {
        public SumPagedRep<ChangeRecord> execute(ChangeRecordQuery changeRecordQuery) {
            QueryUtility.QueryContext<ChangeRecord> queryContext = QueryUtility.prepareContext(ChangeRecord.class);
            Predicate predicate1 = QueryUtility.getStringEqualPredicate(changeRecordQuery.getChangeId(), CHANGE_ID, queryContext);
            Predicate predicate2 = QueryUtility.getStringEqualPredicate(changeRecordQuery.getEntityType(), ENTITY_TYPE, queryContext);
            Predicate predicate = QueryUtility.combinePredicate(queryContext, predicate1, predicate2);
            Order order = null;
            if (changeRecordQuery.getChangeRecordSort().isById())
                order = QueryUtility.getOrder("changeId", queryContext, changeRecordQuery.getChangeRecordSort().isAsc());
            return QueryUtility.pagedQuery(predicate, order, changeRecordQuery, queryContext);
        }
    }
}
