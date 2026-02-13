package com.dangeboer.raindream.model.form;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ItemForm implements Serializable {
    @NotNull
    private Integer mediaType;
    @NotNull
    private Integer contentType;

    private String storeUrl;
    private String content;

    private String title;
    @NotNull
    private String fandom;
    @NotNull
    private String cp;
    private String author;
    private String sourceUrl;
    private Integer releaseYear;
    private Long sizeBytes;

    private Integer trackingType;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private BigDecimal rating;
    private String notes;
    private String summary;

    // 文章补充信息
    private FanficForm fanficForm;

    // 媒体补充信息
    private MediaForm mediaForm;

    // 标签和平台补充信息
    private List<String> tags;
    private List<String> plts;
}
