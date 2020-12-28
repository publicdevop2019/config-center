package com.mt.common;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumSetConverter<E extends Enum<E>> implements AttributeConverter<Set<E>, String> {
    private final Class<E> type;

    public EnumSetConverter(Class<E> type) {
        this.type = type;
    }

    @Override
    public String convertToDatabaseColumn(Set<E> es) {
        if (ObjectUtils.isEmpty(es))
            return "";
        return String.join(",", es.stream().map(Enum::name).collect(Collectors.toSet()));
    }

    @Override
    public Set<E> convertToEntityAttribute(String s) {
        if (StringUtils.hasText(s))
            return Arrays.stream(s.split(",")).map(e -> E.valueOf(type, e)).collect(Collectors.toSet());
        return Collections.emptySet();
    }
}
