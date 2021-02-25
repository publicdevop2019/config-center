package com.mt.common.domain.model.serializer;

import com.github.fge.jsonpatch.JsonPatch;

import java.util.List;

public interface CustomObjectSerializer {
    <T> String serialize(T object);

    byte[] nativeSerialize(Object object);

    <T> T deepCopy(T object, Class<T> clazz);

    <T> T nativeDeepCopy(T object);

    <T> List<T> deepCopy(List<T> object);

    <T> T deserialize(String str, Class<T> clazz);

    Object nativeDeserialize(byte[] bytes);

    <T> T applyJsonPatch(JsonPatch command, T beforePatch, Class<T> clazz);
}
