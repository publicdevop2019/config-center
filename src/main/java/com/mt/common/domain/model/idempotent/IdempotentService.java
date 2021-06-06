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
        SumPagedRep<ChangeRecord> forwardChanges = CommonApplicationServiceRegistry
                .getChangeRecordApplicationService()
                .changeRecords(CHANGE_ID + ":" + changeId + "," + ENTITY_TYPE + ":" + aggregateName);
        if (isCancelChange(changeId)) {
            Optional<ChangeRecord> forwardChange = forwardChanges.findFirst();
            if (forwardChange.isPresent()) {
                CreateChangeRecordCommand command = createChangeRecordCommand(changeId, aggregateName, null);
                CommonApplicationServiceRegistry.getChangeRecordApplicationService().createReverse(command);
                return function.apply(command);
            } else {
                //change not found
                DomainEventPublisher.instance().publish(new HangingTxDetected(changeId));
                CreateChangeRecordCommand command = createChangeRecordCommand(changeId, aggregateName, null);
                CommonApplicationServiceRegistry.getChangeRecordApplicationService().createEmptyReverse(command);
                return null;
            }
        } else {
            SumPagedRep<ChangeRecord> reverseChanges = CommonApplicationServiceRegistry
                    .getChangeRecordApplicationService()
                    .changeRecords(CHANGE_ID + ":" + changeId + "_cancelled" + "," + ENTITY_TYPE + ":" + aggregateName);
            Optional<ChangeRecord> reverseChange = reverseChanges.findFirst();
            if (reverseChange.isPresent()) {
                //change has been cancelled, perform null operation
                CreateChangeRecordCommand command = createChangeRecordCommand(changeId, aggregateName, null);
                CommonApplicationServiceRegistry.getChangeRecordApplicationService().createEmptyForward(command);
                return null;
            }
            Optional<ChangeRecord> forwardChange = forwardChanges.findFirst();
            if (forwardChange.isPresent()) {
                DomainEventPublisher.instance().publish(new HangingTxDetected(changeId));
                return forwardChange.get().getReturnValue();
            } else {
                CreateChangeRecordCommand command = createChangeRecordCommand(changeId, aggregateName, null);
                CommonApplicationServiceRegistry.getChangeRecordApplicationService().createForward(command);
                return function.apply(command);
            }
        }
    }

    private boolean isCancelChange(String changeId) {
        return changeId.contains("_cancelled");
    }

    private CreateChangeRecordCommand createChangeRecordCommand(String changeId, String aggregateName, @Nullable String returnValue) {
        CreateChangeRecordCommand changeRecord = new CreateChangeRecordCommand();
        changeRecord.setChangeId(changeId);
        changeRecord.setAggregateName(aggregateName);
        changeRecord.setReturnValue(returnValue);
        return changeRecord;
    }

}
