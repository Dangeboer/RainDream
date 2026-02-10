package com.dangeboer.raindream.model.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class ItemDetailVO implements Serializable {
    private Integer mediaType;
    private Integer contentType;

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
    private BigDecimal rating;
    private String notes;
    private String summary;
}
