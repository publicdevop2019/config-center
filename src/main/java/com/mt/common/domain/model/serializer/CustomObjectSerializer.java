package com.mt.common.domain.model.serializer;

import com.github.fge.jsonpatch.JsonPatch;

import java.util.Collection;
import java.util.List;

public interface CustomObjectSerializer {
    byte[] nativeSerialize(Object object);
    <T> T nativeDeepCopy(T object);
    Object nativeDeserialize(byte[] bytes);
    
    <T> String serialize(T object);
    <T> T deserialize(String str, Class<T> clazz);
    <T> T deepCopy(T object, Class<T> clazz);

    <T> String serializeCollection(Collection<T> object);
    <T> Collection<T> deserializeCollection(String str, Class<T> clazz);
    <T> Collection<T> deepCopyCollection(Collection<T> object);






    <T> T applyJsonPatch(JsonPatch command, T beforePatch, Class<T> clazz);
}
