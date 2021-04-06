package com.mt.common.domain.model.idempotent;

import com.mt.common.CommonConstant;
import com.mt.common.application.CommonApplicationServiceRegistry;
import com.mt.common.application.idempotent.ChangeRecordApplicationService;
import com.mt.common.application.idempotent.CreateChangeRecordCommand;
import com.mt.common.application.idempotent.exception.ChangeNotFoundException;
import com.mt.common.domain.model.domainId.DomainId;
import com.mt.common.domain.model.domain_event.DomainEventPublisher;
import com.mt.common.domain.model.idempotent.event.HangingTxDetected;
import com.mt.common.domain.model.restful.SumPagedRep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.mt.common.CommonConstant.CHANGE_REVOKED;
import static com.mt.common.domain.model.idempotent.ChangeRecord.CHANGE_ID;
import static com.mt.common.domain.model.idempotent.ChangeRecord.ENTITY_TYPE;

@Service
@Slf4j
public class IdempotentService {

    public <T> String idempotentCreate(Object command, String changeId, DomainId domainId, Supplier<DomainId> wrapper, Class<T> clazz) {
        String entityType = getEntityName(clazz);
        if (changeAlreadyExist(changeId, clazz) && changeAlreadyRevoked(changeId, clazz)) {
            SumPagedRep<ChangeRecord> appChangeRecordCardRepSumPagedRep = idempotentApplicationService().changeRecords(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + entityType);
            return appChangeRecordCardRepSumPagedRep.getData().get(0).getQuery().replace(CommonConstant.COMMON_ENTITY_ID + CommonConstant.QUERY_DELIMITER, "");
        } else if (changeAlreadyExist(changeId, clazz) && !changeAlreadyRevoked(changeId, clazz)) {
            SumPagedRep<ChangeRecord> appChangeRecordCardRepSumPagedRep = idempotentApplicationService().changeRecords(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + entityType);
            return appChangeRecordCardRepSumPagedRep.getData().get(0).getQuery().replace("id:", "");
        } else if (!changeAlreadyExist(changeId, clazz) && changeAlreadyRevoked(changeId, clazz)) {
            //hanging tx
            DomainEventPublisher.instance().publish(new HangingTxDetected(domainId, changeId));
            return "revoked";
        } else {
            saveChangeRecord(command, changeId, OperationType.POST, "id:" + domainId.getDomainId(), null, clazz);
            return wrapper.get().getDomainId();
        }
    }

    public <T> Set<String> idempotentDeleteByQuery(String query, String changeId, Function<CreateChangeRecordCommand, Set<DomainId>> wrapper, Class<T> clazz) {
        String entityType = getEntityName(clazz);
        if (changeAlreadyExist(changeId, clazz) && changeAlreadyRevoked(changeId, clazz)) {
            SumPagedRep<ChangeRecord> appChangeRecordCardRepSumPagedRep = idempotentApplicationService().changeRecords(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + entityType);
            return appChangeRecordCardRepSumPagedRep.getData().get(0).getUpdatedIds();
        } else if (changeAlreadyExist(changeId, clazz) && !changeAlreadyRevoked(changeId, clazz)) {
            SumPagedRep<ChangeRecord> appChangeRecordCardRepSumPagedRep = idempotentApplicationService().changeRecords(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + entityType);
            return appChangeRecordCardRepSumPagedRep.getData().get(0).getUpdatedIds();
        } else if (!changeAlreadyExist(changeId, clazz) && changeAlreadyRevoked(changeId, clazz)) {
            return Collections.emptySet();
        } else {
            CreateChangeRecordCommand changeRecordCommand = saveChangeRecord(null, changeId, OperationType.DELETE_BY_QUERY, query, null, clazz);
            //@todo find a better way to get updated ids, current solution requires caller manually update
            return wrapper.apply(changeRecordCommand).stream().map(DomainId::getDomainId).collect(Collectors.toSet());
        }
    }

    public <T> void idempotent(@Nullable DomainId domainId, Object command, String changeId, Consumer<CreateChangeRecordCommand> wrapper, Class<T> clazz) {
        if (changeAlreadyExist(changeId, clazz) && changeAlreadyRevoked(changeId, clazz)) {
        } else if (changeAlreadyExist(changeId, clazz) && !changeAlreadyRevoked(changeId, clazz)) {
        } else if (!changeAlreadyExist(changeId, clazz) && changeAlreadyRevoked(changeId, clazz)) {
            saveChangeRecord(command, changeId, OperationType.PUT, "Already Revoked", null, clazz);
        } else {
            CreateChangeRecordCommand changeRecordCommand = saveChangeRecord(command, changeId, OperationType.PUT,  (domainId != null ? "id:" + domainId.getDomainId() : ""), null, clazz);
            wrapper.accept(changeRecordCommand);
        }
    }

    public <T> void idempotentRollback(String changeId, Consumer<ChangeRecord> wrapper, Class<T> clazz) {
        if (changeAlreadyExist(changeId, clazz) && changeAlreadyRevoked(changeId, clazz)) {
        } else if (changeAlreadyExist(changeId, clazz) && !changeAlreadyRevoked(changeId, clazz)) {
            String entityType = getEntityName(clazz);
            SumPagedRep<ChangeRecord> appChangeRecordCardRepSumPagedRep1 = idempotentApplicationService().changeRecords(ChangeRecord.CHANGE_ID + ":" + changeId + "," + ChangeRecord.ENTITY_TYPE + ":" + entityType);
            List<ChangeRecord> data = appChangeRecordCardRepSumPagedRep1.getData();
            if (data == null || data.size() == 0) {
                throw new ChangeNotFoundException();
            }
            log.debug("start of rollback change /w id {}", changeId);
            ChangeRecord appChangeRecordCardRep = data.get(0);
            wrapper.accept(appChangeRecordCardRep);
        } else if (!changeAlreadyExist(changeId, clazz) && changeAlreadyRevoked(changeId, clazz)) {
        } else {
            saveChangeRecordRollback(changeId + CHANGE_REVOKED, clazz);
        }
    }

    protected <T> boolean changeAlreadyRevoked(String changeId, Class<T> clazz) {
        String entityType = getEntityName(clazz);
        SumPagedRep<ChangeRecord> appChangeRecordCardRepSumPagedRep = idempotentApplicationService().changeRecords(CHANGE_ID + ":" + changeId + CHANGE_REVOKED + "," + ENTITY_TYPE + ":" + entityType);
        return (appChangeRecordCardRepSumPagedRep.getData() != null && appChangeRecordCardRepSumPagedRep.getData().size() > 0);
    }

    protected <T> boolean changeAlreadyExist(String changeId, Class<T> clazz) {
        String entityType = getEntityName(clazz);
        SumPagedRep<ChangeRecord> appChangeRecordCardRepSumPagedRep = idempotentApplicationService().changeRecords(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + entityType);
        return (appChangeRecordCardRepSumPagedRep.getData() != null && appChangeRecordCardRepSumPagedRep.getData().size() > 0);
    }

    protected <T> String getEntityName(Class<T> tClass) {
        String[] split = tClass.getName().split("\\.");
        return split[split.length - 1];
    }

    protected <T> CreateChangeRecordCommand saveChangeRecord(Object requestBody, String changeId, OperationType operationType, String query, Set<String> updatedIds, Class<T> clazz) {
        CreateChangeRecordCommand changeRecord = new CreateChangeRecordCommand();
        changeRecord.setChangeId(changeId);
        changeRecord.setEntityType(getEntityName(clazz));
        changeRecord.setOperationType(operationType);
        changeRecord.setQuery(query);
        changeRecord.setDeletedIds(updatedIds);
        changeRecord.setRequestBody(requestBody);
        idempotentApplicationService().create(changeRecord);
        return changeRecord;
    }

    protected <T> void saveChangeRecordRollback(String changeId, Class<T> clazz) {
        CreateChangeRecordCommand changeRecord = new CreateChangeRecordCommand();
        changeRecord.setChangeId(changeId);
        changeRecord.setEntityType(getEntityName(clazz));
        changeRecord.setOperationType(OperationType.EMPTY_OPT);
        changeRecord.setQuery((String) null);
        changeRecord.setDeletedIds(null);
        changeRecord.setRequestBody("Change Not Found");
        changeRecord.setRollbackChangeNotFound(true);
        idempotentApplicationService().create(changeRecord);
    }

    private ChangeRecordApplicationService idempotentApplicationService() {
        return CommonApplicationServiceRegistry.getChangeRecordApplicationService();
    }
}
