package com.mt.common.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Slf4j
@Service
public class JacksonObjectSerializer implements CustomObjectSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> String serialize(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("error during object mapper serialize", e);
            throw new UnableToSerializeException();
        }
    }

    @Override
    public byte[] nativeSerialize(Object object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("error during native serialize", e);
            throw new UnableToNativeSerializeException();
        }
    }

    @Override
    public <T> T deepCopy(T object) {
        return deserialize(serialize(object));
    }

    @Override
    public <T> T nativeDeepCopy(T object) {
        return (T) nativeDeserialize(nativeSerialize(object));
    }

    @Override
    public <T> List<T> deepCopy(List<T> object) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(object), new TypeReference<List<T>>() {
            });
        } catch (IOException e) {
            log.error("error during object mapper list deep copy", e);
            throw new UnableToDeepCopyListException();
        }
    }

    @Override
    public <T> T deserialize(String str) {
        try {
            return objectMapper.readValue(str, new TypeReference<List<T>>() {
            });
        } catch (IOException e) {
            log.error("error during object mapper deserialize", e);
            throw new UnableToDeSerializeException();
        }
    }

    @Override
    public Object nativeDeserialize(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            log.error("error during native deserialize", e);
            throw new UnableToNativeDeserializeException();
        }
    }

    @Override
    public <T> T applyJsonPatch(JsonPatch command, T beforePatch, Class<T> clazz) {
        try {
            JsonNode pathCommand = objectMapper.convertValue(beforePatch, JsonNode.class);
            JsonNode patchedNode = command.apply(pathCommand);
            return objectMapper.treeToValue(patchedNode, clazz);
        } catch (JsonPatchException | JsonProcessingException e) {
            log.error("error during object mapper json patch", e);
            throw new UnableToJsonPatchException();
        }
    }

    public static class UnableToSerializeException extends RuntimeException {
    }

    public static class UnableToDeSerializeException extends RuntimeException {
    }

    public static class UnableToJsonPatchException extends RuntimeException {
    }

    public static class UnableToDeepCopyListException extends RuntimeException {
    }

    public static class UnableToNativeSerializeException extends RuntimeException {
    }

    public static class UnableToNativeDeserializeException extends RuntimeException {
    }
}
