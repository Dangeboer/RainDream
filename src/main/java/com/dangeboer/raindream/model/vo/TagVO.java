package com.dangeboer.raindream.model.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TagVO implements Serializable {
    private Long id;
    private Long userId;
    private String tagName;
}
