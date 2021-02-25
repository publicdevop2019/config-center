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

    public static QueryConfig skip() {

        return new QueryConfig("sc:1");
    }

    public boolean count() {
        return value == null || value.contains("sc:1");
    }
}
