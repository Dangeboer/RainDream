package com.dangeboer.raindream.enums;

import com.dangeboer.raindream.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum TrackingTypeEnum implements IBaseEnum<Integer> {

    TODO(1, "想看"),
    DOING(2, "在看"),
    UPTODATE(3, "追平"),
    DONE(4, "看过"),
    ARCHIVED(5, "归档");

    private final Integer value;
    private final String label;

    TrackingTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
