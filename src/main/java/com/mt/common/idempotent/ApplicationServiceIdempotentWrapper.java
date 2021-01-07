package com.mt.common.idempotent;

import com.mt.common.domain.model.domainId.DomainId;
import com.mt.common.idempotent.command.AppCreateChangeRecordCommand;
import com.mt.common.idempotent.representation.AppChangeRecordCardRep;
import com.mt.common.sql.SumPagedRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.mt.common.CommonConstant.CHANGE_REVOKED;
import static com.mt.common.idempotent.model.ChangeRecord.CHANGE_ID;
import static com.mt.common.idempotent.model.ChangeRecord.ENTITY_TYPE;

@Service
public class ApplicationServiceIdempotentWrapper {
    @Autowired
    AppChangeRecordApplicationService appChangeRecordApplicationService;

    public <T> String idempotentCreate(Object command, String changeId, DomainId domainId, Supplier<DomainId> wrapper, Class<T> clazz) {
        String entityType = getEntityName(clazz);
        if (changeAlreadyExist(changeId, clazz) && changeAlreadyRevoked(changeId, clazz)) {
            SumPagedRep<AppChangeRecordCardRep> appChangeRecordCardRepSumPagedRep = appChangeRecordApplicationService.readByQuery(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + entityType, null, "sc:1");
            return appChangeRecordCardRepSumPagedRep.getData().get(0).getQuery().replace("id:", "");
        } else if (changeAlreadyExist(changeId, clazz) && !changeAlreadyRevoked(changeId, clazz)) {
            SumPagedRep<AppChangeRecordCardRep> appChangeRecordCardRepSumPagedRep = appChangeRecordApplicationService.readByQuery(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + entityType, null, "sc:1");
            return appChangeRecordCardRepSumPagedRep.getData().get(0).getQuery().replace("id:", "");
        } else if (!changeAlreadyExist(changeId, clazz) && changeAlreadyRevoked(changeId, clazz)) {
            return "revoked";
        } else {
            saveChangeRecord(command, changeId, OperationType.POST, "id:" + domainId.getDomainId(), null, null, clazz);
            return wrapper.get().getDomainId();
        }
    }

    public <T> void idempotent(Object command, String changeId, Consumer<AppCreateChangeRecordCommand> wrapper, Class<T> clazz) {
        if (!changeAlreadyExist(changeId, clazz) && !changeAlreadyRevoked(changeId, clazz)) {
            AppCreateChangeRecordCommand changeRecordCommand = saveChangeRecord(command, changeId, OperationType.PUT, "id:", null, null, clazz);
            wrapper.accept(changeRecordCommand);
        }
    }

    protected <T> boolean changeAlreadyRevoked(String changeId, Class<T> clazz) {
        String entityType = getEntityName(clazz);
        SumPagedRep<AppChangeRecordCardRep> appChangeRecordCardRepSumPagedRep = appChangeRecordApplicationService.readByQuery(CHANGE_ID + ":" + changeId + CHANGE_REVOKED + "," + ENTITY_TYPE + ":" + entityType, null, "sc:1");
        return (appChangeRecordCardRepSumPagedRep.getData() != null && appChangeRecordCardRepSumPagedRep.getData().size() > 0);
    }

    protected <T> boolean changeAlreadyExist(String changeId, Class<T> clazz) {
        String entityType = getEntityName(clazz);
        SumPagedRep<AppChangeRecordCardRep> appChangeRecordCardRepSumPagedRep = appChangeRecordApplicationService.readByQuery(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + entityType, null, "sc:1");
        return (appChangeRecordCardRepSumPagedRep.getData() != null && appChangeRecordCardRepSumPagedRep.getData().size() > 0);
    }

    protected <T> String getEntityName(Class<T> tClass) {
        String[] split = tClass.getName().split("\\.");
        return split[split.length - 1];
    }

    protected <T> AppCreateChangeRecordCommand saveChangeRecord(Object requestBody, String changeId, OperationType operationType, String query, Set<Long> deletedIds, Object toBeReplaced, Class<T> clazz) {
        AppCreateChangeRecordCommand changeRecord = new AppCreateChangeRecordCommand();
        changeRecord.setChangeId(changeId);
        changeRecord.setEntityType(getEntityName(clazz));
        changeRecord.setServiceBeanName(this.getClass().getName());
        changeRecord.setOperationType(operationType);
        changeRecord.setQuery(query);
        changeRecord.setReplacedVersion(toBeReplaced);
        changeRecord.setDeletedIds(deletedIds);
        changeRecord.setRequestBody(requestBody);
        appChangeRecordApplicationService.create(changeRecord);
        return changeRecord;
    }
}
