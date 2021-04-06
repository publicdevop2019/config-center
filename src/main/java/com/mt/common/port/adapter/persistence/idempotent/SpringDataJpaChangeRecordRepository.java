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

    default void addIfCounterChangeNotExist(ChangeRecord changeRecord, String counterChangeId) {
        OperationType.DBConverter dbConverter = new OperationType.DBConverter();
        String enumInString = dbConverter.convertToDatabaseColumn(changeRecord.getOperationType());
        StringSetConverter stringSetConverter = new StringSetConverter();
        String idsInString = stringSetConverter.convertToDatabaseColumn(changeRecord.getUpdatedIds());
        Integer integer = saveIfCounterChangeNotFound(
                changeRecord.getId(),
                changeRecord.getChangeId(),
                changeRecord.getEntityType(),
                counterChangeId,
                changeRecord.getCreatedAt(),
                changeRecord.getCreatedBy(),
                enumInString,
                changeRecord.getQuery(),
                changeRecord.getRequestBody(),
                idsInString
        );
        if (!integer.equals(1)) {
            throw new UpdateQueryBuilder.PatchCommandExpectNotMatchException();
        }
    }


    @Modifying
    @Query(
            value = "insert into change_record (id, change_id,created_at,created_by,entity_type,operation_type,query,request_body,updated_ids) select" +
                    " :id, :changeId,:createdAt,:createdBy,:entityType,:opt, :query, :reqBody, :updatedIds" +
                    " from dual where not exists (select id from change_record where change_id = :counterChangeId and entity_type = :entityType)"
            , nativeQuery = true
    )
    Integer saveIfCounterChangeNotFound(
            @Param("id") long id,
            @Param("changeId") String age,
            @Param("entityType") String entityType,
            @Param("counterChangeId") String counterChangeId,
            @Param("createdAt") Date createAt,
            @Param("createdBy") String createdBy,
            @Param("opt") String opt,
            @Param("query") String query,
            @Param("reqBody") String reqBody,
            @Param("updatedIds") String updatedIds
    );

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
            return QueryUtility.nativePagedQuery(query, context);
        }
    }
}
