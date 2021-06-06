package com.mt.common.port.adapter.persistence.idempotent;

import com.mt.common.domain.model.idempotent.ChangeRecord;
import com.mt.common.domain.model.idempotent.ChangeRecordQuery;
import com.mt.common.domain.model.idempotent.ChangeRecordRepository;
import com.mt.common.domain.model.idempotent.ChangeRecord_;
import com.mt.common.domain.model.restful.SumPagedRep;
import com.mt.common.domain.model.restful.query.QueryUtility;
import com.mt.common.port.adapter.persistence.CommonQueryBuilderRegistry;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.Order;
import java.util.Optional;

/**
 * for saga, where forward events (e.g. 127c17e0-c4d6-11eb-ab24-712aed736a9c)
 * and reverse events(e.g. 127c17e0-c4d6-11eb-ab24-712aed736a9c_cancelled) exist
 */
@Repository
@Primary
public interface SpringDataJpaChangeRecordRepository extends ChangeRecordRepository, JpaRepository<ChangeRecord, Long> {

    default SumPagedRep<ChangeRecord> changeRecordsOfQuery(ChangeRecordQuery changeRecordQuery) {
        return CommonQueryBuilderRegistry.getChangeRecordQueryBuilder().execute(changeRecordQuery);
    }

    //concurrent safe save, this work when changeId and changeId_cancel could happen at same time
    //event driven architecture
    //or changeId and changeId_cancel http call happens at same time
    //changeId will fail and changeId_cancel will success
    @Modifying
    @Query(
            value = "insert into change_record (id, change_id,entity_type,return_value) select" +
                    " :id, :changeId, :entityType, :returnValue" +
                    " from dual where not exists (select id from change_record where change_id = :counterChangeId and entity_type = :entityType)"
            , nativeQuery = true
    )
    Integer addIfNotCancelled(
            @Param("id") long id,
            @Param("changeId") String changeId,
            @Param("entityType") String entityType,
            @Param("counterChangeId") String counterChangeId,
            @Param("returnValue") String returnValue
    );

    @Modifying
    @Query(
            value = "insert into change_record (id, change_id,entity_type,return_value) select" +
                    " :id, :changeId, :entityType, :returnValue" +
                    " from dual where not exists (select id from change_record where change_id = :counterChangeId and entity_type = :entityType)"
            , nativeQuery = true
    )
    Integer emptyCancel(
            @Param("id") long id,
            @Param("changeId") String changeId,
            @Param("entityType") String entityType,
            @Param("counterChangeId") String counterChangeId,
            @Param("returnValue") String returnValue
    );

    /**
     * when add forward change, reverse change should not exist
     * @param changeRecord
     */
    default void addForwardChangeRecord(ChangeRecord changeRecord) {
        Integer integer = addIfNotCancelled(changeRecord.getId(), changeRecord.getChangeId(), changeRecord.getEntityType(), changeRecord.getChangeId() + "_cancelled", changeRecord.getReturnValue());
        if (!integer.equals(1)) {
            throw new IllegalArgumentException("unable to insert change, expect 1 but got " + integer);
        }
    }

    default void addReverseChangeRecord(ChangeRecord changeRecord) {
        save(changeRecord);
    }

    /**
     * when add empty reverse change, forward change should not exist
     */
    default void addEmptyReverseChangeRecord(ChangeRecord changeRecord) {
        Integer integer = emptyCancel(changeRecord.getId(), changeRecord.getChangeId(), changeRecord.getEntityType(), changeRecord.getChangeId() + "_cancelled", changeRecord.getReturnValue());
        if (!integer.equals(1)) {
            throw new IllegalArgumentException("unable to insert change, expect 1 but got " + integer);
        }
    }

    //when find cancelled event
    default void addEmptyForwardChangeRecord(ChangeRecord changeRecord) {
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
