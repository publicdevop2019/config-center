package com.mt.common.domain.model.idempotent;

import com.fasterxml.jackson.databind.JsonSerializable;
import com.mt.common.application.idempotent.CreateChangeRecordCommand;
import com.mt.common.domain.CommonDomainRegistry;
import com.mt.common.domain.model.audit.Auditable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"changeId", "entityType"}))
@Data
@NoArgsConstructor
public class ChangeRecord extends Auditable {
    public static final String CHANGE_ID = "changeId";
    public static final String ENTITY_TYPE = "entityType";
    @Id
    private Long id;
    @Column(nullable = false)
    private String changeId;
    @Column(nullable = false)
    private String entityType;
    @Column(nullable = false)
    private String serviceBeanName;

    @Lob
    @Column(columnDefinition = "BLOB")
    //@Convert(converter = CustomByteArraySerializer.class)
    // not using converter due to lazy load , no session error
    private String replacedVersion;
    @Lob
    @Column(columnDefinition = "BLOB")
    private String requestBody;
    private HashSet<String> deletedIds;
    private OperationType operationType;
    private String query;

    public ChangeRecord(Long id, CreateChangeRecordCommand command) {
        this.id = id;
        this.changeId = command.getChangeId();
        this.entityType = command.getEntityType();
        this.serviceBeanName = command.getServiceBeanName();
        if (command.getRequestBody() instanceof JsonSerializable) {
            this.requestBody = CommonDomainRegistry.getCustomObjectSerializer().serialize(command.getRequestBody());
        } else {
            if (command.getRequestBody() instanceof MultipartFile) {
                this.requestBody = "MultipartFile skipped";
            } else {
                this.requestBody = CommonDomainRegistry.getCustomObjectSerializer().serialize(command.getRequestBody());
            }
        }
        this.operationType = command.getOperationType();
        this.query = command.getQuery();
        this.replacedVersion = CommonDomainRegistry.getCustomObjectSerializer().serialize(command.getReplacedVersion());
        if (command.getDeletedIds() != null)
            this.deletedIds = new HashSet<>(command.getDeletedIds());
    }
}
