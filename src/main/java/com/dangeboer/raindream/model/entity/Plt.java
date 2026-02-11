package com.dangeboer.raindream.model.entity;

import com.dangeboer.raindream.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Plt extends BaseEntity {
    private Long userId;
    private String pltName;
}
