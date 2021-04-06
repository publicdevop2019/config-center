package com.mt.common.application.idempotent.representation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.mt.common.domain.CommonDomainRegistry;
import com.mt.common.domain.model.idempotent.ChangeRecord;
import com.mt.common.domain.model.idempotent.OperationType;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Set;

@Data
public class ChangeRecordRepresentation {
    private Long id;

    private String changeId;
    private String entityType;

    private Set<String> deletedIds;
    private OperationType operationType;
    private String query;
    private Object replacedVersion;
    private Object requestBody;
    @Autowired
    private ObjectMapper om;

    public ChangeRecordRepresentation(ChangeRecord changeRecord) {
        this.id = changeRecord.getId();
        this.changeId = changeRecord.getChangeId();
        this.entityType = changeRecord.getEntityType();
        this.operationType = changeRecord.getOperationType();
        this.query = changeRecord.getQuery();
        this.deletedIds = changeRecord.getUpdatedIds();
        if (changeRecord.getOperationType().equals(OperationType.PATCH_BY_ID)) {
            try {
                this.requestBody = om.readValue(changeRecord.getRequestBody(), JsonPatch.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.requestBody = CommonDomainRegistry.getCustomObjectSerializer().deserialize(changeRecord.getRequestBody(), Object.class);
        }
    }
}
