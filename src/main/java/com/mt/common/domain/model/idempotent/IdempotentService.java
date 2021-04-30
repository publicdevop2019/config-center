package com.mt.common.domain.model.idempotent;

import com.mt.common.application.CommonApplicationServiceRegistry;
import com.mt.common.application.idempotent.CreateChangeRecordCommand;
import com.mt.common.domain.model.domain_event.DomainEventPublisher;
import com.mt.common.domain.model.idempotent.event.HangingTxDetected;
import com.mt.common.domain.model.restful.SumPagedRep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

import static com.mt.common.domain.model.idempotent.ChangeRecord_.CHANGE_ID;
import static com.mt.common.domain.model.idempotent.ChangeRecord_.ENTITY_TYPE;

@Service
@Slf4j
public class IdempotentService {

    public <T> String idempotent(String changeId, Function<CreateChangeRecordCommand, String> function, String aggregateName) {
        SumPagedRep<ChangeRecord> appChangeRecordCardRepSumPagedRep = CommonApplicationServiceRegistry.getChangeRecordApplicationService().changeRecords(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + aggregateName);
        Optional<ChangeRecord> first = appChangeRecordCardRepSumPagedRep.findFirst();
        if (first.isPresent()) {
            DomainEventPublisher.instance().publish(new HangingTxDetected(changeId));
            return first.get().getReturnValue();
        } else {
            CreateChangeRecordCommand createChangeRecordCommand = saveChangeRecord(changeId, aggregateName, null);
            return function.apply(createChangeRecordCommand);
        }
    }

    private <T> CreateChangeRecordCommand saveChangeRecord(String changeId, String aggregateName, @Nullable String returnValue) {
        CreateChangeRecordCommand changeRecord = new CreateChangeRecordCommand();
        changeRecord.setChangeId(changeId);
        changeRecord.setAggregateName(aggregateName);
        changeRecord.setReturnValue(returnValue);
        CommonApplicationServiceRegistry.getChangeRecordApplicationService().create(changeRecord);
        return changeRecord;
    }

}
