package com.mt.common.domain.model.clazz;

public class ClassUtility {
    public static <T> String getShortName(Class<T> tClass) {
        String[] split = tClass.getName().split("\\.");
        return split[split.length - 1];
    }
}
