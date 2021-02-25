package com.mt.common.application;

import com.mt.common.application.idempotent.ChangeRecordApplicationService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommonApplicationServiceRegistry {
    @Getter
    private static ChangeRecordApplicationService changeRecordApplicationService;

    @Autowired
    public void setChangeRecordApplicationService(ChangeRecordApplicationService idempotentApplicationService) {
        CommonApplicationServiceRegistry.changeRecordApplicationService = idempotentApplicationService;
    }

}
