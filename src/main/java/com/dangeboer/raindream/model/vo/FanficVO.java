package com.dangeboer.raindream.model.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class FanficVO implements Serializable {

    private Integer era;
    private String eraLabel; // label

    private String charSetting;

    private Integer lengthType;
    private String lengthTypeLabel; // label

    private Integer workType;
    private String workTypeLabel; // label

    private LocalDate updateDate;

    private Integer endingType;
    private String endingTypeLabel; // label

    private Integer readCount;
}
