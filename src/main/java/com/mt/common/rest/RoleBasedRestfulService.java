package com.mt.common.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.mt.common.CommonConstant;
import com.mt.common.audit.Auditable;
import com.mt.common.audit.AuditorAwareImpl;
import com.mt.common.domain.model.uniqueId.IdGenerator;
import com.mt.common.idempotent.AppChangeRecordApplicationService;
import com.mt.common.idempotent.OperationType;
import com.mt.common.idempotent.command.AppCreateChangeRecordCommand;
import com.mt.common.idempotent.exception.ChangeNotFoundException;
import com.mt.common.idempotent.exception.RollbackNotSupportedException;
import com.mt.common.idempotent.model.ChangeRecord;
import com.mt.common.idempotent.representation.AppChangeRecordCardRep;
import com.mt.common.rest.exception.AggregateNotExistException;
import com.mt.common.rest.exception.AggregateOutdatedException;
import com.mt.common.serializer.CustomObjectSerializer;
import com.mt.common.sql.PatchCommand;
import com.mt.common.sql.RestfulQueryRegistry;
import com.mt.common.sql.SumPagedRep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class RoleBasedRestfulService<T extends Auditable & Aggregate, X, Y, Z extends TypedClass<Z>> implements AfterWriteCompleteHook<T> {
    @Autowired
    protected CrudRepository<T, Long> repo;
    @Autowired
    protected IdGenerator idGenerator;
    @Autowired
    protected RestfulQueryRegistry<T> queryRegistry;
    @Autowired
    protected StringRedisTemplate redisTemplate;
    @Autowired
    protected CustomObjectSerializer customObjectSerializer;

    protected Class<T> entityClass;

    protected Function<T, Z> entityPatchSupplier;

    protected RestfulQueryRegistry.RoleEnum role;
    @Autowired
    protected ObjectMapper om;
    @Autowired
    protected TransactionTemplate transactionTemplate;
    protected boolean rollbackSupported = true;
    @Autowired
    protected AppChangeRecordApplicationService appChangeRecordApplicationService;

    protected Long generateId(Object object) {
        return idGenerator.id();
    }

    public CreatedAggregateRep create(Object command, String changeId) {
        if (changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
            return new CreatedAggregateRep();
        } else if (changeAlreadyExist(changeId) && !changeAlreadyRevoked(changeId)) {
            String entityType = getEntityName();
            SumPagedRep<AppChangeRecordCardRep> appChangeRecordCardRepSumPagedRep = appChangeRecordApplicationService.readByQuery(ChangeRecord.CHANGE_ID + ":" + changeId + "," + ChangeRecord.ENTITY_TYPE + ":" + entityType, null, "sc:1");
            CreatedAggregateRep createdEntityRep = new CreatedAggregateRep();
            long l = Long.parseLong(appChangeRecordCardRepSumPagedRep.getData().get(0).getQuery().replace("id:", ""));
            createdEntityRep.setId(l);
            return createdEntityRep;
        } else if (!changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
            saveChangeRecord(command, changeId, OperationType.POST, "id:", null, null);
            return new CreatedAggregateRep();
        } else {
            long id = generateId(command);
            T execute = transactionTemplate.execute(transactionStatus -> {
                saveChangeRecord(command, changeId, OperationType.POST, "id:" + id, null, null);
                T created = createEntity(id, command);
                return repo.save(created);
            });
            cleanUpCache(Collections.singleton(id));
            afterWriteComplete();
            afterCreateComplete(execute);
            return getCreatedEntityRepresentation(execute);
        }
    }

    public void replaceById(Long id, Object command, String changeId) {
        if (changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
        } else if (changeAlreadyExist(changeId) && !changeAlreadyRevoked(changeId)) {
        } else if (!changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
            saveChangeRecord(command, changeId, OperationType.PUT, "id:" + id.toString(), null, null);
        } else {
            SumPagedRep<T> tSumPagedRep = getEntityById(id);
            T t = tSumPagedRep.getData().get(0);
            checkVersion(t, command);
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    saveChangeRecord(command, changeId, OperationType.PUT, "id:" + id.toString(), null, t);
                    T after = replaceEntity(t, command);
                    repo.save(after);
                }
            });
            cleanUpCache(Collections.singleton(id));
            afterWriteComplete();
            afterReplaceByIdComplete(id);
        }
    }

    public void patchById(Long id, JsonPatch patch, Map<String, Object> params) {
        String changeId = (String) params.get(CommonConstant.HTTP_HEADER_CHANGE_ID);
        if (changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
        } else if (changeAlreadyExist(changeId) && !changeAlreadyRevoked(changeId)) {
        } else if (!changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
            saveChangeRecord(patch, (String) params.get(CommonConstant.HTTP_HEADER_CHANGE_ID), OperationType.PATCH_BY_ID, "id:" + id.toString(), null, null);
        } else {
            SumPagedRep<T> entityById = getEntityById(id);
            T original = entityById.getData().get(0);
            Z before = entityPatchSupplier.apply(original);
            Z after = customObjectSerializer.applyJsonPatch(patch, before, before.getClazz());
            prePatch(original, params, before);
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    saveChangeRecord(patch, (String) params.get(CommonConstant.HTTP_HEADER_CHANGE_ID), OperationType.PATCH_BY_ID, "id:" + id.toString(), null, original);
                    BeanUtils.copyProperties(after, original);//make change after changeRecord is created
                    repo.save(original);
                }
            });
            postPatch(original, params, before);
            cleanUpCache(Collections.singleton(id));
            afterWriteComplete();
            afterPatchByIdComplete(id);
        }
    }

    public Integer patchBatch(List<PatchCommand> commands, String changeId) {
        if (changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
            return 0;
        } else if (changeAlreadyExist(changeId) && !changeAlreadyRevoked(changeId)) {
            return 0;
        } else if (!changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
            saveChangeRecord(commands, changeId, OperationType.PATCH_BATCH, null, null, null);
            return 0;
        } else {
            List<PatchCommand> deepCopy = customObjectSerializer.deepCopy(commands);
            Integer execute = transactionTemplate.execute(transactionStatus -> {
                saveChangeRecord(commands, changeId, OperationType.PATCH_BATCH, null, null, null);
                return queryRegistry.update(role, deepCopy, entityClass);
            });
            cleanUpAllCache();
            afterWriteComplete();
            afterPatchBatchComplete();
            return execute;
        }
    }

    public Integer deleteById(Long id, String changeId) {
        return deleteByQuery("id:" + id.toString(), changeId);
    }

    public Integer deleteByQuery(String query, String changeId) {
        if (!queryRegistry.hasDeleteQuery(role)) {
            throw new UnsupportedOperationException();
        }
        if (changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
            return 0;
        } else if (changeAlreadyExist(changeId) && !changeAlreadyRevoked(changeId)) {
            return 0;
        } else if (!changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
            saveChangeRecord(null, changeId, OperationType.DELETE_BY_QUERY, query, Collections.EMPTY_SET, null);
            return 0;
        } else {
            List<T> data = getAggregates(query);
            if (data.isEmpty())
                return 0;
            data.forEach(this::preDelete);
            Set<Long> collect = data.stream().map(Aggregate::getId).collect(Collectors.toSet());
            String join = "id:" + String.join(".", collect.stream().map(Object::toString).collect(Collectors.toSet()));
            Integer execute = transactionTemplate.execute(transactionStatus -> {
                saveChangeRecord(null, changeId, OperationType.DELETE_BY_QUERY, query, collect, null);
                return queryRegistry.deleteByQuery(role, join, entityClass);//delete only checked entity
            });
            data.forEach(this::postDelete);
            cleanUpCache(collect);
            afterWriteComplete();
            afterDeleteComplete(collect);
            return execute;
        }

    }


    private List<T> getAggregates(String query) {
        int pageNum = 0;
        SumPagedRep<T> tSumPagedRep = queryRegistry.readByQuery(role, query, "num:" + pageNum, null, entityClass);
        if (tSumPagedRep.getData().size() == 0)
            return new ArrayList<>();
        double l = (double) tSumPagedRep.getTotalItemCount() / tSumPagedRep.getData().size();//for accuracy
        double ceil = Math.ceil(l);
        int i = BigDecimal.valueOf(ceil).intValue();
        List<T> data = new ArrayList<>(tSumPagedRep.getData());
        for (int a = 1; a < i; a++) {
            data.addAll(queryRegistry.readByQuery(role, query, "num:" + a, null, entityClass).getData());
        }
        return data;
    }

    public SumPagedRep<X> readByQuery(String query, String page, String config) {
        SumPagedRep<T> tSumPagedRep = queryRegistry.readByQuery(role, query, page, config, entityClass);
        List<X> col = tSumPagedRep.getData().stream().map(this::getEntitySumRepresentation).collect(Collectors.toList());
        return new SumPagedRep<>(col, tSumPagedRep.getTotalItemCount());
    }


    public Y readById(Long id) {
        SumPagedRep<T> tSumPagedRep = getEntityById(id);
        return getEntityRepresentation(tSumPagedRep.getData().get(0));
    }

    protected boolean changeAlreadyRevoked(String changeId) {
        String entityType = getEntityName();
        SumPagedRep<AppChangeRecordCardRep> appChangeRecordCardRepSumPagedRep = appChangeRecordApplicationService.readByQuery(ChangeRecord.CHANGE_ID + ":" + changeId + CommonConstant.CHANGE_REVOKED + "," + ChangeRecord.ENTITY_TYPE + ":" + entityType, null, "sc:1");
        return (appChangeRecordCardRepSumPagedRep.getData() != null && appChangeRecordCardRepSumPagedRep.getData().size() > 0);
    }

    protected boolean changeAlreadyExist(String changeId) {
        String entityType = getEntityName();
        SumPagedRep<AppChangeRecordCardRep> appChangeRecordCardRepSumPagedRep = appChangeRecordApplicationService.readByQuery(ChangeRecord.CHANGE_ID + ":" + changeId + "," + ChangeRecord.ENTITY_TYPE + ":" + entityType, null, "sc:1");
        return (appChangeRecordCardRepSumPagedRep.getData() != null && appChangeRecordCardRepSumPagedRep.getData().size() > 0);
    }

    protected String getEntityName() {
        String[] split = entityClass.getName().split("\\.");
        return split[split.length - 1];
    }

    public void rollback(String changeId) {
        if (!rollbackSupported) {
            log.debug(getEntityName() + " rollback not supported, ignoring rollback operation");
            return;
        }
        if (changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
        } else if (changeAlreadyExist(changeId) && !changeAlreadyRevoked(changeId)) {
            String entityType = getEntityName();
            log.info("start of rollback change /w id {}", changeId);
            SumPagedRep<AppChangeRecordCardRep> appChangeRecordCardRepSumPagedRep1 = appChangeRecordApplicationService.readByQuery(ChangeRecord.CHANGE_ID + ":" + changeId + "," + ChangeRecord.ENTITY_TYPE + ":" + entityType, null, "sc:1");
            List<AppChangeRecordCardRep> data = appChangeRecordCardRepSumPagedRep1.getData();
            if (data == null || data.size() == 0) {
                throw new ChangeNotFoundException();
            }
            OperationType type = data.get(0).getOperationType();
            if (List.of(OperationType.DELETE_BY_QUERY, OperationType.POST).contains(type)) {
                if (OperationType.POST.equals(type)) {
                    Set<Long> execute = transactionTemplate.execute(e -> {
                        saveChangeRecord(null, changeId + CommonConstant.CHANGE_REVOKED, OperationType.CANCEL_CREATE, data.get(0).getQuery(), null, null);
                        return restoreCreate(data.get(0).getQuery().replace("id:", ""));
                    });
                    afterWriteComplete();
                    afterDeleteComplete(execute);
                    cleanUpCache(execute);
                } else {
                    String collect = data.get(0).getDeletedIds().stream().map(Object::toString).collect(Collectors.joining("."));
                    Set<Long> execute = transactionTemplate.execute(e -> {
                        saveChangeRecord(null, changeId + CommonConstant.CHANGE_REVOKED, OperationType.RESTORE_DELETE, "id:" + collect, null, null);
                        return restoreDelete(collect);
                    });
                    afterWriteComplete();
                    afterCreateComplete(null);
                    cleanUpCache(execute);
                }
            } else if (OperationType.PATCH_BATCH.equals(type)) {
                List<PatchCommand> rollbackCmd = buildRollbackCommand((List<PatchCommand>) data.get(0).getRequestBody());
                patchBatch(rollbackCmd, changeId + CommonConstant.CHANGE_REVOKED);
            } else if (List.of(OperationType.PATCH_BY_ID, OperationType.PUT).contains(type)) {
                T previous = (T) data.get(0).getReplacedVersion();
                T stored = getEntityById(previous.getId()).getData().get(0);
                Integer version = stored.getVersion();
                Integer version1 = previous.getVersion();
                if (version - 1 == version1) {
                    log.info("restore to previous entity version");
                } else {
                    log.warn("stored previous version is out dated, your data may get lost");
                }
                BeanUtils.copyProperties(previous, stored);
                Set<Long> execute = transactionTemplate.execute(e -> {
                    saveChangeRecord(null, changeId + CommonConstant.CHANGE_REVOKED, OperationType.RESTORE_LAST_VERSION, data.get(0).getQuery(), null, null);
                    repo.save(stored);
                    return Collections.singleton(stored.getId());
                });
                afterWriteComplete();
                afterReplaceByIdComplete(stored.getId());
                cleanUpCache(execute);
            } else {
                throw new RollbackNotSupportedException();
            }
            log.info("end of rollback change /w id {}", changeId);
        } else if (!changeAlreadyExist(changeId) && changeAlreadyRevoked(changeId)) {
        } else {
            saveChangeRecord(null, changeId + CommonConstant.CHANGE_REVOKED, OperationType.EMPTY_OPT, null, null, null);
        }
    }

    protected List<PatchCommand> buildRollbackCommand(List<PatchCommand> patchCommands) {
        List<PatchCommand> deepCopy = customObjectSerializer.deepCopy(patchCommands);
        deepCopy.forEach(e -> {
            if (e.getOp().equalsIgnoreCase(CommonConstant.PATCH_OP_TYPE_SUM)) {
                e.setOp(CommonConstant.PATCH_OP_TYPE_DIFF);
            } else if (e.getOp().equalsIgnoreCase(CommonConstant.PATCH_OP_TYPE_DIFF)) {
                e.setOp(CommonConstant.PATCH_OP_TYPE_SUM);
            } else {
                throw new RollbackNotSupportedException();
            }
        });
        return deepCopy;
    }

    protected Set<Long> restoreDelete(String ids) {
        Set<Long> collect = Arrays.stream(ids.split("\\.")).map(Long::parseLong).collect(Collectors.toSet());
        for (Long l : collect) {
            Optional<T> byId = repo.findById(l);//use repo instead of common readyBy
            if (byId.isEmpty())
                throw new AggregateNotExistException();
            T t = byId.get();
            t.setDeleted(false);
            t.setRestoredAt(new Date());
            Optional<String> currentAuditor = AuditorAwareImpl.getAuditor();
            t.setRestoredBy(currentAuditor.orElse(""));
            repo.save(byId.get());
        }
        return collect;
    }

    protected Set<Long> restoreCreate(String ids) {
        Set<Long> collect = Arrays.stream(ids.split("\\.")).map(Long::parseLong).collect(Collectors.toSet());
        for (Long l : collect) {
            Optional<T> byId = repo.findById(l);//use repo instead of common readyBy
            if (byId.isEmpty())
                throw new AggregateNotExistException();
            T t = byId.get();
            t.setDeleted(true);
            t.setDeletedAt(new Date());
            Optional<String> currentAuditor = AuditorAwareImpl.getAuditor();
            t.setDeletedBy(currentAuditor.orElse(""));
            repo.save(byId.get());
        }
        return collect;
    }

    protected SumPagedRep<T> getEntityById(Long id) {
        SumPagedRep<T> tSumPagedRep = queryRegistry.readById(role, id.toString(), entityClass);
        if (tSumPagedRep.getData().size() == 0)
            throw new AggregateNotExistException();
        return tSumPagedRep;
    }

    protected void saveChangeRecord(Object requestBody, String changeId, OperationType operationType, String query, Set<Long> deletedIds, Object toBeReplaced) {
        AppCreateChangeRecordCommand changeRecord = new AppCreateChangeRecordCommand();
        changeRecord.setChangeId(changeId);
        changeRecord.setEntityType(getEntityName());
        changeRecord.setServiceBeanName(this.getClass().getName());
        changeRecord.setOperationType(operationType);
        changeRecord.setQuery(query);
        changeRecord.setReplacedVersion(toBeReplaced);
        changeRecord.setDeletedIds(deletedIds);
        changeRecord.setRequestBody(requestBody);
        appChangeRecordApplicationService.create(changeRecord);
    }

    protected List<X> getAllByQuery(String query) {
        SumPagedRep<X> sumPagedRep = readByQuery(query, null, null);
        List<X> data = sumPagedRep.getData();
        if (data.size() == 0)
            return data;
        long l = sumPagedRep.getTotalItemCount() / data.size();
        double ceil = Math.ceil(l);
        int count = BigDecimal.valueOf(ceil).intValue();
        for (int i = 1; i < count; i++) {
            SumPagedRep<X> next = readByQuery(query, "num:" + i, "sc:1");
            data.addAll(next.getData());
        }
        return data;
    }

    private CreatedAggregateRep getCreatedEntityRepresentation(T created) {
        return new CreatedAggregateRep(created);
    }

    public void cleanUpCache(Set<Long> ids) {
        if (hasCachedAggregates()) {
            String entityName = getEntityName();
            Set<String> keys = redisTemplate.keys(entityName + CommonConstant.CACHE_QUERY_PREFIX + ":*");
            if (!CollectionUtils.isEmpty(keys)) {
                redisTemplate.delete(keys);
            }
            ids.forEach(id -> {
                Set<String> keys1 = redisTemplate.keys(entityName + CommonConstant.CACHE_ID_PREFIX + ":*");
                if (!CollectionUtils.isEmpty(keys1)) {
                    Set<String> collect = keys1.stream().filter(e -> {
                        String[] split1 = e.split(":");
                        String[] split2 = split1[1].split("\\[");
                        String s = split2[split2.length - 1];
                        String replace = s.replace("]", "");
                        String[] split3 = replace.split("-");
                        long min = Long.parseLong(split3[0]);
                        long max = Long.parseLong(split3[1]);
                        return id <= max && id >= min;
                    }).collect(Collectors.toSet());
                    if (!CollectionUtils.isEmpty(collect)) {
                        redisTemplate.delete(collect);
                    }
                }
            });
        }
    }

    private boolean hasCachedAggregates() {
        return queryRegistry.cacheable.keySet().stream().anyMatch(e -> queryRegistry.cacheable.get(e));

    }

    public void cleanUpAllCache() {
        if (hasCachedAggregates()) {
            String entityName = getEntityName();
            Set<String> keys = redisTemplate.keys(entityName + CommonConstant.CACHE_QUERY_PREFIX + ":*");
            if (!CollectionUtils.isEmpty(keys)) {
                redisTemplate.delete(keys);
            }
            Set<String> keys1 = redisTemplate.keys(entityName + CommonConstant.CACHE_ID_PREFIX + ":*");
            if (!CollectionUtils.isEmpty(keys1)) {
                redisTemplate.delete(keys1);
            }
        }
    }

    private void checkVersion(T t, Object command) {
        if (command instanceof AggregateUpdateCommand) {
            if (!t.getVersion().equals(((AggregateUpdateCommand) command).getVersion())) {
                throw new AggregateOutdatedException();
            }
        }
    }

    protected T replaceEntity(T t, Object command) {
        throw new UnsupportedOperationException();
    }

    protected X getEntitySumRepresentation(T t) {
        return null;
    }

    protected Y getEntityRepresentation(T t) {
        return null;
    }

    protected T createEntity(long id, Object command) {
        throw new UnsupportedOperationException();
    }

    protected void preDelete(T t) {
    }

    protected void postDelete(T t) {
    }

    protected void prePatch(T t, Map<String, Object> params, Z middleLayer) {
    }

    protected void postPatch(T t, Map<String, Object> params, Z middleLayer) {
    }
}
