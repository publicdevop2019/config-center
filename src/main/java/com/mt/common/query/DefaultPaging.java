package com.mt.common.query;

public class DefaultPaging {
    public static final String PAGING_NUM = "num";
    public static final String PAGING_SIZE = "size";
    private Long pageNumber = 0L;
    private Integer pageSize = 10;
    private final String value;

    public String value() {
        return value;
    }

    public DefaultPaging(String pagingParamStr) {
        if (pagingParamStr == null) {
            value = PAGING_NUM + ":" + pageNumber + "," + PAGING_SIZE + ":" + pageSize;
        } else {
            value = pagingParamStr;
            String[] split = pagingParamStr.split(",");
            for (String str : split) {
                String[] split1 = str.split(":");
                if (split1.length != 2)
                    throw new PagingParseException();
                if (PAGING_NUM.equalsIgnoreCase(split1[0])) {
                    try {
                        pageNumber = Long.parseLong(split1[1]);
                    } catch (Exception ex) {
                        throw new PagingParseException();
                    }
                }
                if (PAGING_SIZE.equalsIgnoreCase(split1[0])) {
                    try {
                        pageSize = Integer.parseInt(split1[1]);
                    } catch (Exception ex) {
                        throw new PagingParseException();
                    }
                }
            }
            if (pageNumber == null || pageSize == null)
                throw new PagingParseException();
        }
    }

    public long getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public DefaultPaging() {
        value = PAGING_NUM + ":" + pageNumber + "," + PAGING_SIZE + ":" + pageSize;
    }

    public DefaultPaging(Long pageNumber, Integer pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.value = PAGING_NUM + ":" + pageNumber + "," + PAGING_SIZE + ":" + pageSize;
    }

    public DefaultPaging nextPage() {
        return new DefaultPaging(pageNumber + 1, pageSize);
    }

    private static class PagingParseException extends RuntimeException {
    }
}
