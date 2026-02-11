package com.dangeboer.raindream.enums;

import com.dangeboer.raindream.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum MediaTypeEnum implements IBaseEnum<Integer> {
    TEXT(1, "文本"),
    STATIC(2, "静图"),
    GIF(3, "动图"),
    LIVE(4, "实况照片"),
    VIDEO(5, "视频"),
    LINK(6, "链接");

    private final Integer value;
    private final String label;

    MediaTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
