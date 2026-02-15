package com.dangeboer.raindream.model.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 除同人文以外所有 item 的 list
 */
@Getter
@Setter
public class ItemListVO implements Serializable {
    private Long id;

    private Integer mediaType;
    private String mediaTypeLabel; // label

    private Integer contentType;
    private String contentTypeLabel; // label

    private String storeUrl;
    private String content;

    private String title;
    private String cp;
    private String author;
    private String sourceUrl;

    private Integer trackingType;
    private String trackingTypeLabel; // label

    private BigDecimal rating;
}
