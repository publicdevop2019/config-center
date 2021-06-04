package com.mt.common.port.adapter.persistence;

import com.mt.common.port.adapter.persistence.idempotent.SpringDataJpaSagaChangeRecordRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonQueryBuilderRegistry {
    @Getter
    private static SpringDataJpaSagaChangeRecordRepository.JpaCriteriaApiChangeRecordAdaptor changeRecordQueryBuilder;

    @Autowired
    public void setChangeRecordQueryBuilder(SpringDataJpaSagaChangeRecordRepository.JpaCriteriaApiChangeRecordAdaptor changeRecordQueryBuilder) {
        CommonQueryBuilderRegistry.changeRecordQueryBuilder = changeRecordQueryBuilder;
    }

}
