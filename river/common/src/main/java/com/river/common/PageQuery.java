package com.river.common;

import lombok.Data;

@Data
public class PageQuery {
    /**
     * 当前页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
    
    /**
     * 排序列
     */
    private String orderByColumn;
    
    /**
     * 排序方向 asc/desc
     */
    private String isAsc;
}