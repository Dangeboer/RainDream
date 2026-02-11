package com.dangeboer.raindream.model.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 同人文 detail
 */
public class FanficDetailVO implements Serializable {
    private Integer mediaType;
    private String mediaTypeLabel; // label

    private Integer contentType;
    private String contentTypeLabel; // label

    private String storeUrl;
    private String content;

    private String title;
    private String fandom;
    private String cp;
    private String author;
    private String sourceUrl;
    private Integer releaseYear;
    private Long sizeBytes;

    private Integer trackingType;
    private String trackingTypeLabel; // label

    private BigDecimal rating;
    private String notes;
    private String summary;

    private FanficVO fanficVO;
}
