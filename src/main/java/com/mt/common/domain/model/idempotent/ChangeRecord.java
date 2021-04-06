package com.mt.common.domain.model.idempotent;

import com.fasterxml.jackson.databind.JsonSerializable;
import com.mt.common.application.idempotent.CreateChangeRecordCommand;
import com.mt.common.domain.CommonDomainRegistry;
import com.mt.common.domain.model.sql.converter.StringSetConverter;
import com.mt.common.infrastructure.audit.SpringDataJpaConfig;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"changeId", "entityType"}))
@Data
@NoArgsConstructor
public class ChangeRecord {
    private String createdBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    public static final String CHANGE_ID = "changeId";
    public static final String ENTITY_TYPE = "entityType";
    @Id
    private Long id;
    @Column(nullable = false)
    private String changeId;
    @Column(nullable = false)
    private String entityType;

    @Column(columnDefinition = "BLOB")
    private String requestBody;
    @Convert(converter = StringSetConverter.class)
    private Set<String> updatedIds;
    @Convert(converter = OperationType.DBConverter.class)
    private OperationType operationType;
    private String query;

    public ChangeRecord(Long id, CreateChangeRecordCommand command) {
        this.id = id;
        this.changeId = command.getChangeId();
        this.createdAt = Date.from(Instant.now());
        SpringDataJpaConfig.AuditorAwareImpl.getAuditor().ifPresent(e -> {
            this.createdBy = e;
        });
        this.entityType = command.getEntityType();
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
        if (command.getDeletedIds() != null)
            this.updatedIds = new HashSet<>(command.getDeletedIds());
    }
}
