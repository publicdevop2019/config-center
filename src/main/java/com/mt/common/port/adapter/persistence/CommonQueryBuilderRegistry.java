package com.mt.common.port.adapter.persistence;

import com.mt.common.port.adapter.persistence.idempotent.SpringDataJpaChangeRecordRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonQueryBuilderRegistry {
    @Getter
    private static SpringDataJpaChangeRecordRepository.JpaCriteriaApiChangeRecordAdaptor changeRecordQueryBuilder;

    @Autowired
    public void setChangeRecordQueryBuilder(SpringDataJpaChangeRecordRepository.JpaCriteriaApiChangeRecordAdaptor changeRecordQueryBuilder) {
        CommonQueryBuilderRegistry.changeRecordQueryBuilder = changeRecordQueryBuilder;
    }

}
