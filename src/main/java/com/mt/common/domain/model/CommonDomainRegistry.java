package com.mt.common.domain.model;


import com.mt.common.domain.model.domainId.UniqueIdGeneratorService;
import com.mt.common.serializer.CustomObjectSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommonDomainRegistry {
    private static UniqueIdGeneratorService uniqueIdGeneratorService;
    private static CustomObjectSerializer objectSerializer;

    @Autowired
    public void setCustomObjectSerializer(CustomObjectSerializer customObjectSerializer) {
        CommonDomainRegistry.objectSerializer = customObjectSerializer;
    }

    @Autowired
    public void setUniqueIdGeneratorService(UniqueIdGeneratorService uniqueIdGeneratorService) {
        CommonDomainRegistry.uniqueIdGeneratorService = uniqueIdGeneratorService;
    }

    public static UniqueIdGeneratorService uniqueIdGeneratorService() {
        return uniqueIdGeneratorService;
    }

    public static CustomObjectSerializer customObjectSerializer() {
        return objectSerializer;
    }

}
