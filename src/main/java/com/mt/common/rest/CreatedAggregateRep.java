package com.mt.common.rest;

import lombok.Data;

@Data
public class CreatedAggregateRep extends CreatedRep {
    private Long id;

    public CreatedAggregateRep(Aggregate entity) {
        this.id = entity.getId();
    }

    public CreatedAggregateRep() {

    }
}
