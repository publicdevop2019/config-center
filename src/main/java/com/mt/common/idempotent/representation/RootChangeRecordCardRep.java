package com.mt.common.idempotent.representation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.mt.common.idempotent.OperationType;
import com.mt.common.idempotent.model.ChangeRecord;
import com.mt.common.idempotent.model.CustomByteArraySerializer;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Data
public class RootChangeRecordCardRep {
    private Long id;

    private String changeId;
    private String entityType;

    private OperationType operationType;
    private String query;
    private String serviceBeanName;
    private Object requestBody;
    @Autowired
    private ObjectMapper om;
    public RootChangeRecordCardRep(ChangeRecord changeRecord) {
        this.id = changeRecord.getId();
        this.changeId = changeRecord.getChangeId();
        this.entityType = changeRecord.getEntityType();
        this.operationType = changeRecord.getOperationType();
        this.query = changeRecord.getQuery();
        this.serviceBeanName = changeRecord.getServiceBeanName();
        if (changeRecord.getOperationType().equals(OperationType.PATCH_BY_ID)) {
            try {
                this.requestBody = om.readValue(changeRecord.getRequestBody(), JsonPatch.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.requestBody = CustomByteArraySerializer.convertToEntityAttribute(changeRecord.getRequestBody());
        }
    }
}
