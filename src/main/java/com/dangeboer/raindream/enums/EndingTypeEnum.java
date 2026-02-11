package com.dangeboer.raindream.enums;

import com.dangeboer.raindream.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum EndingTypeEnum implements IBaseEnum<Integer> {

    HE(1, "HE"),
    BE(2, "BE"),
    OE(3, "OE"),
    ME(4, "ME");

    private final Integer value;
    private final String label;

    EndingTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
