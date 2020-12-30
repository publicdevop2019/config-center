package com.mt.common.sql;

import lombok.Data;

import java.io.Serializable;

/**
 * used for json patch and create/delete operation
 */
@Data
public class PatchCommand implements Comparable<PatchCommand>, Serializable {
    private static final long serialVersionUID = 1;
    private String op;
    private String path;
    private Object value;
    private Integer expect;

    @Override
    public int compareTo(PatchCommand to) {
        if (parseDomainId(path).equals(parseDomainId(to.path)))
            return 0;
        else
            return 1;
    }

    private Long parseId(String path) {
        String[] split = path.split("/");
        return Long.parseLong(split[1]);
    }

    private String parseDomainId(String path) {
        String[] split = path.split("/");
        return split[1];
    }
}
