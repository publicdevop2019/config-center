package com.mt.common.idempotent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mt.common.idempotent.command.AppCreateChangeRecordCommand;
import com.mt.common.idempotent.model.ChangeRecord;
import com.mt.common.idempotent.representation.AppChangeRecordCardRep;
import com.mt.common.rest.CreatedAggregateRep;
import com.mt.common.rest.RoleBasedRestfulService;
import com.mt.common.rest.VoidTypedClass;
import com.mt.common.sql.RestfulQueryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppChangeRecordApplicationService extends RoleBasedRestfulService<ChangeRecord, AppChangeRecordCardRep, Void, VoidTypedClass> {
    {
        entityClass = ChangeRecord.class;
        role = RestfulQueryRegistry.RoleEnum.APP;
    }
    @Autowired
    ApplicationContext context;
    @Autowired
    ObjectMapper om;

    @Override
    public ChangeRecord replaceEntity(ChangeRecord changeRecord, Object command) {
        return null;
    }

    @Override
    public AppChangeRecordCardRep getEntitySumRepresentation(ChangeRecord changeRecord) {
        return new AppChangeRecordCardRep(changeRecord);
    }

    @Transactional
    public CreatedAggregateRep create(AppCreateChangeRecordCommand command) {
        long id = idGenerator.id();
        ChangeRecord changeRecord = ChangeRecord.create(id, command, om);
        ChangeRecord saved = repo.save(changeRecord);
        return new CreatedAggregateRep(saved);
    }

    @Transactional
    public void deleteByQuery(String queryParam) {
        List<AppChangeRecordCardRep> allByQuery = getAllByQuery(queryParam);
        allByQuery.forEach(e -> {
            Class<?> aClass = null;
            try {
                aClass = Class.forName(e.getServiceBeanName());
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            RoleBasedRestfulService bean = (RoleBasedRestfulService) context.getBean(aClass);
            bean.rollback(e.getChangeId());
        });

    }
}
