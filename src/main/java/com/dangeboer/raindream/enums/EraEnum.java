package com.dangeboer.raindream.enums;

import com.dangeboer.raindream.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum EraEnum implements IBaseEnum<Integer> {

    HISTORICAL(1, "古代"),
    REPUBLICAN(2, "民国"),
    MODERN(3, "现代");

    private final Integer value;
    private final String label;

    EraEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
