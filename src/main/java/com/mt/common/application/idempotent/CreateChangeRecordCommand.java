package com.mt.common.application.idempotent;

import com.mt.common.CommonConstant;
import com.mt.common.domain.model.domainId.DomainId;
import com.mt.common.domain.model.idempotent.OperationType;
import lombok.Data;

import java.util.Set;

@Data
public class CreateChangeRecordCommand {

    private String changeId;
    private String entityType;
    private String serviceBeanName;
    private OperationType operationType;
    private String query;
    private Object replacedVersion;
    private Object requestBody;
    private Set<String> deletedIds;

    public void setQuery(DomainId domainId) {
        setQuery(CommonConstant.COMMON_ENTITY_ID + CommonConstant.QUERY_DELIMITER + domainId.getDomainId());
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
