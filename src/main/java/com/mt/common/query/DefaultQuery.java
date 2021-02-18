package com.mt.common.query;

import com.mt.common.domain.model.domainId.DomainId;

import java.util.HashMap;
import java.util.Map;

public class DefaultQuery {
    protected final String rawValue;
    protected final Map<String, String> parsed = new HashMap<>();

    public DefaultQuery(String queryParam) {
        this.rawValue = queryParam;
        if (queryParam != null) {
            String[] split = queryParam.split(",");
            for (String str : split) {
                String[] split1 = str.split(":");
                if (split1.length != 2)
                    throw new QueryParseException();
                parsed.put(split1[0], split1[1]);
            }
        }
    }

    public String value() {
        return rawValue;
    }

    public boolean isGetAll() {
        return parsed.isEmpty();
    }

    public DefaultQuery(DomainId domainId) {
        this("id:" + domainId.getDomainId());
    }

    public static class QueryParseException extends RuntimeException {
    }
}
