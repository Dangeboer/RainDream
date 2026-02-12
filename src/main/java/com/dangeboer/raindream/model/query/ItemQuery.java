package com.dangeboer.raindream.model.query;

import com.dangeboer.raindream.base.BasePageQuery;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemQuery extends BasePageQuery {
    // 筛选
    private Integer mediaType;
    private Integer contentType;

    private String author;
    private String sourceUrl;

    private Integer trackingType;
    private BigDecimal rating;

    // 模糊搜索
    private String keyword;

    // 排序
    private String sortBy;
    private String order; // asc / desc
}
