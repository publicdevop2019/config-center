package com.mt.common.application.idempotent;

import com.mt.common.domain.CommonDomainRegistry;
import com.mt.common.domain.model.idempotent.ChangeRecord;
import com.mt.common.domain.model.idempotent.ChangeRecordQuery;
import com.mt.common.domain.model.restful.query.QueryConfig;
import com.mt.common.domain.model.restful.query.PageConfig;
import com.mt.common.domain.model.restful.SumPagedRep;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeRecordApplicationService {
    public SumPagedRep<ChangeRecord> changeRecords(String s, String s1) {
        return CommonDomainRegistry.getChangeRecordRepository().changeRecordsOfQuery(new ChangeRecordQuery(s), new PageConfig(), new QueryConfig(s1));
    }

    public SumPagedRep<ChangeRecord> changeRecords(String s, String s1, String s2) {
        return CommonDomainRegistry.getChangeRecordRepository().changeRecordsOfQuery(new ChangeRecordQuery(s), new PageConfig(s1, 100), new QueryConfig(s2));
    }
    @Transactional
    public void create(CreateChangeRecordCommand changeRecord) {
        long id = CommonDomainRegistry.getUniqueIdGeneratorService().id();
        ChangeRecord changeRecord1 = new ChangeRecord(id, changeRecord);
        CommonDomainRegistry.getChangeRecordRepository().add(changeRecord1);
    }
}
