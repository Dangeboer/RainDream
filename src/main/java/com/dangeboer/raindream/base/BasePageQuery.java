package com.dangeboer.raindream.base;

import lombok.Data;

@Data
public class BasePageQuery {
    // 页码
    private Long pageNum;
    // 每页数量
    private Long pageSize;
}
