package com.dangeboer.raindream.model.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class ItemListVO implements Serializable {
    private String storeUrl;
    private String content;

    private String title;
    private String cp;
    private String author;
    private String sourceUrl;

    private Integer trackingType;
    private BigDecimal rating;
}
