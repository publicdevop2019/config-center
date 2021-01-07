package com.mt.common.idempotent;

import com.mt.common.idempotent.model.ChangeRecord;
import com.mt.common.idempotent.representation.RootChangeRecordCardRep;
import com.mt.common.rest.RoleBasedRestfulService;
import com.mt.common.rest.VoidTypedClass;
import com.mt.common.rest.exception.AggregateNotExistException;
import com.mt.common.sql.RestfulQueryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RootChangeRecordApplicationService extends RoleBasedRestfulService<ChangeRecord, RootChangeRecordCardRep, Void, VoidTypedClass> {
    {
        entityClass = ChangeRecord.class;
        role = RestfulQueryRegistry.RoleEnum.ROOT;
    }
    @Autowired
    ApplicationContext context;

    @Transactional
    public void deleteById(Long id) {
        ChangeRecord changeRecord = repo.findById(id).orElseThrow(AggregateNotExistException::new);
        Class<?> aClass = null;
        try {
            aClass = Class.forName(changeRecord.getServiceBeanName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        RoleBasedRestfulService bean = (RoleBasedRestfulService) context.getBean(aClass);
        bean.rollback(changeRecord.getChangeId());
    }

    @Override
    public RootChangeRecordCardRep getEntitySumRepresentation(ChangeRecord changeRecord) {
        return new RootChangeRecordCardRep(changeRecord);
    }

    @Transactional
    public void deleteByQuery(String queryParam) {
        List<RootChangeRecordCardRep> allByQuery = getAllByQuery(queryParam);
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
