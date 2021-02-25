package com.mt.common.domain.model.restful.query;

import com.mt.common.domain.model.audit.Auditable;
import com.mt.common.domain.model.restful.SumPagedRep;
import com.mt.common.domain.model.sql.builder.SelectQueryBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class QueryUtility {
    public static <T, S extends QueryCriteria> Set<T> getAllByQuery(BiFunction<S, PageConfig, SumPagedRep<T>> ofQuery, S query) {
        PageConfig queryPagingParam = new PageConfig();
        SumPagedRep<T> tSumPagedRep = ofQuery.apply(query, queryPagingParam);
        if (tSumPagedRep.getData().size() == 0)
            return new HashSet<>();
        double l = (double) tSumPagedRep.getTotalItemCount() / tSumPagedRep.getData().size();//for accuracy
        double ceil = Math.ceil(l);
        int i = BigDecimal.valueOf(ceil).intValue();
        Set<T> data = new HashSet<>(tSumPagedRep.getData());
        for (int a = 1; a < i; a++) {
            data.addAll(ofQuery.apply(query, queryPagingParam.pageOf(a)).getData());
        }
        return data;
    }

    public static <T extends Auditable> SumPagedRep<T> pagedQuery(SelectQueryBuilder<T> queryBuilder, QueryCriteria query, PageConfig page, QueryConfig config, Class<T> clazz) {
        List<T> select = queryBuilder.select(query, page, clazz);
        Long aLong = null;
        if (config.count()) {
            aLong = queryBuilder.count(query, clazz);
        }
        return new SumPagedRep<>(select, aLong);
    }
}
