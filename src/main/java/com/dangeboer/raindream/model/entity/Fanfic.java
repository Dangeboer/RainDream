package com.dangeboer.raindream.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Fanfic {
    @TableId(value = "item_id", type = IdType.INPUT)
    private Long itemId;

    private Integer era;
    private String charSetting;
    private Integer lengthType;
    private Integer workType;
    private LocalDate updateDate;
    private Integer endingType;
    private Integer readCount;
}
