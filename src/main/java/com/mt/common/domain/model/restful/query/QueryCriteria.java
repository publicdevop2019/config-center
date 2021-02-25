package com.mt.common.domain.model.restful.query;

import com.mt.common.CommonConstant;
import com.mt.common.domain.model.domainId.DomainId;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public class QueryCriteria {
    protected final String rawValue;
    protected final Map<String, String> parsed = new HashMap<>();

    public QueryCriteria(String queryParam) {
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

    public boolean isGetAll() {
        return parsed.isEmpty();
    }

    public QueryCriteria(DomainId domainId) {
        this(CommonConstant.COMMON_ENTITY_ID + CommonConstant.QUERY_DELIMITER + domainId.getDomainId());
    }

    public QueryCriteria(Set<String> domainIds) {
        this(CommonConstant.COMMON_ENTITY_ID + CommonConstant.QUERY_DELIMITER + String.join(CommonConstant.QUERY_OR_DELIMITER, domainIds));
    }

    public static class QueryParseException extends RuntimeException {
    }
}
