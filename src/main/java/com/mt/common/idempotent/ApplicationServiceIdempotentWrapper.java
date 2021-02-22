package com.mt.common.idempotent;

import com.mt.common.domain.model.domainId.DomainId;
import com.mt.common.domain_event.DomainEventPublisher;
import com.mt.common.idempotent.command.AppCreateChangeRecordCommand;
import com.mt.common.idempotent.exception.ChangeNotFoundException;
import com.mt.common.idempotent.model.ChangeRecord;
import com.mt.common.idempotent.representation.AppChangeRecordCardRep;
import com.mt.common.sql.SumPagedRep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import static com.mt.common.idempotent.model.ChangeRecord.CHANGE_ID;
import static com.mt.common.idempotent.model.ChangeRecord.ENTITY_TYPE;

@Service
@Slf4j
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
            //hanging tx
            DomainEventPublisher.instance().publish(new HangingTxDetected(domainId, changeId));
            return "revoked";
        } else {
            saveChangeRecord(command, changeId, OperationType.POST, "id:" + domainId.getDomainId(), null, null, clazz);
            return wrapper.get().getDomainId();
        }
    }

    public <T> Set<String> idempotentDeleteByQuery(String query, String changeId, Function<AppCreateChangeRecordCommand, Set<DomainId>> wrapper, Class<T> clazz) {
        String entityType = getEntityName(clazz);
        if (changeAlreadyExist(changeId, clazz) && changeAlreadyRevoked(changeId, clazz)) {
            SumPagedRep<AppChangeRecordCardRep> appChangeRecordCardRepSumPagedRep = appChangeRecordApplicationService.readByQuery(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + entityType, null, "sc:1");
            return appChangeRecordCardRepSumPagedRep.getData().get(0).getDeletedIds();
        } else if (changeAlreadyExist(changeId, clazz) && !changeAlreadyRevoked(changeId, clazz)) {
            SumPagedRep<AppChangeRecordCardRep> appChangeRecordCardRepSumPagedRep = appChangeRecordApplicationService.readByQuery(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + entityType, null, "sc:1");
            return appChangeRecordCardRepSumPagedRep.getData().get(0).getDeletedIds();
        } else if (!changeAlreadyExist(changeId, clazz) && changeAlreadyRevoked(changeId, clazz)) {
            return Collections.emptySet();
        } else {
            AppCreateChangeRecordCommand changeRecordCommand = saveChangeRecord(null, changeId, OperationType.DELETE_BY_QUERY, query, null, null, clazz);
            return wrapper.apply(changeRecordCommand).stream().map(DomainId::getDomainId).collect(Collectors.toSet());
        }
    }

    public <T> void idempotent(@Nullable DomainId domainId, Object command, String changeId, Consumer<AppCreateChangeRecordCommand> wrapper, Class<T> clazz) {
        if (!changeAlreadyExist(changeId, clazz) && !changeAlreadyRevoked(changeId, clazz)) {
            AppCreateChangeRecordCommand changeRecordCommand = saveChangeRecord(command, changeId, OperationType.PUT, "id:" + (domainId != null ? domainId.getDomainId() : ""), null, null, clazz);
            wrapper.accept(changeRecordCommand);
        }
    }

    public <T> void idempotentRollback(String changeId, Consumer<AppChangeRecordCardRep> wrapper, Class<T> clazz) {
        if (changeAlreadyExist(changeId, clazz) && !changeAlreadyRevoked(changeId, clazz)) {
            String entityType = getEntityName(clazz);
            SumPagedRep<AppChangeRecordCardRep> appChangeRecordCardRepSumPagedRep1 = appChangeRecordApplicationService.readByQuery(ChangeRecord.CHANGE_ID + ":" + changeId + "," + ChangeRecord.ENTITY_TYPE + ":" + entityType, null, "sc:1");
            List<AppChangeRecordCardRep> data = appChangeRecordCardRepSumPagedRep1.getData();
            if (data == null || data.size() == 0) {
                throw new ChangeNotFoundException();
            }
            log.debug("start of rollback change /w id {}", changeId);
            AppChangeRecordCardRep appChangeRecordCardRep = data.get(0);
            wrapper.accept(appChangeRecordCardRep);
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

    protected <T> AppCreateChangeRecordCommand saveChangeRecord(Object requestBody, String changeId, OperationType operationType, String query, Set<String> deletedIds, Object toBeReplaced, Class<T> clazz) {
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
