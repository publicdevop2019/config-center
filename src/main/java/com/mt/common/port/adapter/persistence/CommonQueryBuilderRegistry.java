package com.mt.common.port.adapter.persistence;

import com.mt.common.port.adapter.persistence.idempotent.ChangeRecordQueryBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonQueryBuilderRegistry {
    @Getter
    private static ChangeRecordQueryBuilder changeRecordQueryBuilder;

    @Autowired
    public void setChangeRecordQueryBuilder(ChangeRecordQueryBuilder changeRecordQueryBuilder) {
        CommonQueryBuilderRegistry.changeRecordQueryBuilder = changeRecordQueryBuilder;
    }

}
