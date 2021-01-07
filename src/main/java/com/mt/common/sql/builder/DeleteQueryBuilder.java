package com.mt.common.sql.builder;

public interface DeleteQueryBuilder<T> {
    Integer delete(String search, Class<T> clazz);
}
