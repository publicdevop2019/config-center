package com.mt.common.port.adapter.persistence.idempotent;

import com.mt.common.CommonConstant;
import com.mt.common.domain.model.idempotent.ChangeRecord;
import com.mt.common.domain.model.idempotent.ChangeRecordQuery;
import com.mt.common.domain.model.idempotent.ChangeRecordRepository;
import com.mt.common.domain.model.idempotent.ChangeRecord_;
import com.mt.common.domain.model.restful.SumPagedRep;
import com.mt.common.domain.model.restful.query.QueryUtility;
import com.mt.common.domain.model.sql.builder.UpdateQueryBuilder;
import com.mt.common.port.adapter.persistence.CommonQueryBuilderRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.Order;
import java.util.Optional;

@Repository
public interface SpringDataJpaChangeRecordRepository extends ChangeRecordRepository, JpaRepository<ChangeRecord, Long> {

    default SumPagedRep<ChangeRecord> changeRecordsOfQuery(ChangeRecordQuery changeRecordQuery) {
        return CommonQueryBuilderRegistry.getChangeRecordQueryBuilder().execute(changeRecordQuery);
    }

    default void add(ChangeRecord changeRecord) {
        Integer integer = saveIfRevokeChangeNotFound(changeRecord.getId(), changeRecord.getChangeId(), changeRecord.getEntityType(), changeRecord.getServiceBeanName(), changeRecord.getChangeId() + CommonConstant.CHANGE_REVOKED);
        if(!integer.equals(1)){
            throw new UpdateQueryBuilder.PatchCommandExpectNotMatchException();
        }
    }

    @Modifying
    @Query(
            value = "insert into change_record (id, change_id,deleted,entity_type,service_bean_name) select :id, :changeId, false, :entityType, :beanName from dual where not exists (select id from change_record where change_id = :counterChangeId and entity_type = :entityType)"
            ,nativeQuery = true
    )
    Integer saveIfChangeNotFound(@Param("id") long id, @Param("changeId") String age, @Param("entityType") String entityType, @Param("beanName") String beanName, @Param("counterChangeId") String counterChangeId);

    @Modifying
    @Query(
            value = "insert into change_record (id, change_id,deleted,entity_type,service_bean_name) select :id, :changeId, false, :entityType, :beanName from dual where not exists (select id from change_record where change_id = :counterChangeId and entity_type = :entityType)"
            ,nativeQuery = true
    )

    Integer saveIfRevokeChangeNotFound(@Param("id") long id, @Param("changeId") String age, @Param("entityType") String entityType, @Param("beanName") String beanName, @Param("counterChangeId") String counterChangeId);

    default void addIfChangeNotFound(ChangeRecord changeRecord) {
        Integer integer = saveIfChangeNotFound(changeRecord.getId(), changeRecord.getChangeId(), changeRecord.getEntityType(), changeRecord.getServiceBeanName(), changeRecord.getChangeId().replace(CommonConstant.CHANGE_REVOKED, ""));
        if(!integer.equals(1)){
            throw new UpdateQueryBuilder.PatchCommandExpectNotMatchException();
        }
    }

    @Component
    class JpaCriteriaApiChangeRecordAdaptor {
        public SumPagedRep<ChangeRecord> execute(ChangeRecordQuery query) {
            QueryUtility.QueryContext<ChangeRecord> context = QueryUtility.prepareContext(ChangeRecord.class, query);
            Optional.ofNullable(query.getChangeId()).ifPresent(e -> QueryUtility.addStringEqualPredicate(e, ChangeRecord_.CHANGE_ID, context));
            Optional.ofNullable(query.getEntityType()).ifPresent(e -> QueryUtility.addStringEqualPredicate(e, ChangeRecord_.ENTITY_TYPE, context));
            Order order = null;
            if (query.getChangeRecordSort().isById())
                order = QueryUtility.getOrder(ChangeRecord_.CHANGE_ID, context, query.getChangeRecordSort().isAsc());
            context.setOrder(order);
            return QueryUtility.pagedQuery(query, context);
        }
    }
}
