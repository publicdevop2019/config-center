package com.mt.common.persistence;

public class QueryConfig {
    private final String value;

    public String value() {
        return value;
    }

    public QueryConfig(String configParam) {
        value = configParam;
    }

    public QueryConfig() {
        value = null;
    }

    public boolean count() {
        return value == null || value.contains("sc:1");
    }
}
