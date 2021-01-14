package com.mt.common.query;

import java.util.HashMap;
import java.util.Map;

public class DefaultQuery {
    protected final Map<String, String> parsed = new HashMap<>();

    public DefaultQuery(String queryParam) {
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

    public boolean getAll() {
        return parsed.isEmpty();
    }

    public static class QueryParseException extends RuntimeException {
    }
}
