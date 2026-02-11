package com.dangeboer.raindream.model.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 同人文 list
 */
public class FanficListVO implements Serializable {
    private String storeUrl;
    private String content;

    private String title;
    private String cp;
    private String author;
    private String sourceUrl;

    private Integer trackingType;
    private String trackingTypeLabel; // label

    private BigDecimal rating;

    // 补充上同人文信息
    private FanficVO fanficVO;
}
