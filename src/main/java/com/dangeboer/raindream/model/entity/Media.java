package com.dangeboer.raindream.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Media {
    private Long itemId;
    private String thumbUrl;
    private String liveUrl;
}
