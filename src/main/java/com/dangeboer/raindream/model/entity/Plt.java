package com.dangeboer.raindream.model.entity;

import com.dangeboer.raindream.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Plt extends BaseEntity {
    private Long userId;
    private String pltName;
}
