package com.dangeboer.raindream.enums;

import com.dangeboer.raindream.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum LengthTypeEnum implements IBaseEnum<Integer> {

    SHORT(1, "短篇"),
    MEDIUM(2, "中篇"),
    LONG(3, "长篇");

    private final Integer value;
    private final String label;

    LengthTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
