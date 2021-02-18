package com.mt.common.query;

import com.mt.common.sql.SumPagedRep;

import java.math.BigDecimal;
import java.util.HashSet;
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
}
