package com.dangeboer.raindream.model.form;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class FanficForm implements Serializable {
    private Integer era;
    private String charSetting;
    private Integer lengthType;
    private Integer workType;
    private LocalDate updateDate;
    private Integer endingType;
    private Integer readCount;
}
