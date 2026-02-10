package com.dangeboer.raindream.model.form;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class ItemForm implements Serializable {
    @NotNull
    private Integer mediaType;
    @NotNull
    private Integer contentType;

    private String content;

    private String title;
    @NotNull
    private String fandom;
    @NotNull
    private String cp;
    private String author;
    private String sourceUrl;
    private Integer releaseYear;

    private Integer trackingType;
    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private BigDecimal rating;
    private String notes;
    private String summary;
}
