package com.dangeboer.raindream.enums;

import com.dangeboer.raindream.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum WorkTypeEnum implements IBaseEnum<Integer> {

    COMPLETED(1, "已完结"),
    ON_GOING(2, "更新中"),
    ON_HIATUS(3, "断更"),
    DROPPED(4, "已弃");

    private final Integer value;
    private final String label;

    WorkTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
