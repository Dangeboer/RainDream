package com.dangeboer.raindream.model.vo;

import com.dangeboer.raindream.model.form.MediaForm;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 除同人文以外所有 item 的 detail
 */
@Getter
@Setter
public class ItemDetailVO implements Serializable {
    private Long id;

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
    private String fileName;
    private Integer isFavorite;

    private Integer trackingType;
    private String trackingTypeLabel; // label

    private BigDecimal rating;
    private String notes;
    private String summary;

    // 媒体补充信息（实况照片）
    private MediaForm mediaForm;

    // 标签和平台信息
    private List<TagVO> tagVOS;
    private List<PltVO> pltVOS;
}
