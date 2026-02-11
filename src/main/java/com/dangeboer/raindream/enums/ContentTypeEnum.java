package com.dangeboer.raindream.enums;

import com.dangeboer.raindream.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum ContentTypeEnum implements IBaseEnum<Integer> {

    FANFIC(1, "同人文"),
    FANART(2, "同人图"),
    PHOTO(3, "精修"),
    EDIT(4, "混剪"),
    META(5, "解析"),
    COMMENTARY(6, "吐槽"),
    CREATOR(7, "主创说"),
    RPS(8, "真人"),
    MISC(9, "其他");

    private final Integer value;
    private final String label;

    ContentTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
