package com.mt.common.serializer;

import com.github.fge.jsonpatch.JsonPatch;

import java.util.List;

public interface CustomObjectSerializer {
    <T> String serialize(T object);

    <T> T deepCopy(T object);

    <T> List<T> deepCopy(List<T> object);

    <T> T deserialize(String str, Class<T> clazz);

    <T> T applyJsonPatch(JsonPatch command, T beforePatch, Class<T> clazz);
}
