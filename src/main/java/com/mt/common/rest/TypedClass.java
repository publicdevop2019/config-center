package com.mt.common.rest;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TypedClass<T> {
    private Class<T> clazz;

    public TypedClass(Class<T> clazz) {
        this.clazz = clazz;
    }
}
