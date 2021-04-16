package com.mt.common.port.adapter.persistence.idempotent;

import com.mt.common.domain.model.idempotent.*;
import com.mt.common.domain.model.restful.SumPagedRep;
import com.mt.common.domain.model.restful.query.QueryUtility;
import com.mt.common.domain.model.sql.builder.UpdateQueryBuilder;
import com.mt.common.domain.model.sql.converter.StringSetConverter;
import com.mt.common.port.adapter.persistence.CommonQueryBuilderRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.Order;
import java.util.Date;
import java.util.Optional;

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
        public SumPagedRep<ChangeRecord> execute(ChangeRecordQuery query) {
            QueryUtility.QueryContext<ChangeRecord> context = QueryUtility.prepareContext(ChangeRecord.class, query);
            Optional.ofNullable(query.getChangeIds()).ifPresent(e -> QueryUtility.addStringInPredicate(e, ChangeRecord_.CHANGE_ID, context));
            Optional.ofNullable(query.getEntityType()).ifPresent(e -> QueryUtility.addStringEqualPredicate(e, ChangeRecord_.ENTITY_TYPE, context));
            Order order = null;
            if (query.getChangeRecordSort().isById())
                order = QueryUtility.getOrder(ChangeRecord_.CHANGE_ID, context, query.getChangeRecordSort().isAsc());
            context.setOrder(order);
            return QueryUtility.nativePagedQuery(query, context);
        }
    }
}
