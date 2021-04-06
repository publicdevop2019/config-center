package com.mt.common.application.idempotent;

import com.mt.common.CommonConstant;
import com.mt.common.domain.CommonDomainRegistry;
import com.mt.common.domain.model.idempotent.ChangeRecord;
import com.mt.common.domain.model.idempotent.ChangeRecordQuery;
import com.mt.common.domain.model.restful.SumPagedRep;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeRecordApplicationService {
    public SumPagedRep<ChangeRecord> changeRecords(String s) {
        return CommonDomainRegistry.getChangeRecordRepository().changeRecordsOfQuery(new ChangeRecordQuery(s));
    }

    public SumPagedRep<ChangeRecord> changeRecords(String s, String s1, String s2) {
        return CommonDomainRegistry.getChangeRecordRepository().changeRecordsOfQuery(new ChangeRecordQuery(s, s1, s2));
    }

    @Transactional
    public void create(CreateChangeRecordCommand changeRecord) {
        long id = CommonDomainRegistry.getUniqueIdGeneratorService().id();
        ChangeRecord changeRecord1 = new ChangeRecord(id, changeRecord);
        if (changeRecord.isRollbackChangeNotFound()) {
            CommonDomainRegistry.getChangeRecordRepository().addIfCounterChangeNotExist(changeRecord1, changeRecord.getChangeId() + CommonConstant.CHANGE_REVOKED);
        } else {
            CommonDomainRegistry.getChangeRecordRepository().addIfCounterChangeNotExist(changeRecord1, changeRecord.getChangeId().replace(CommonConstant.CHANGE_REVOKED, ""));
        }
    }
}
