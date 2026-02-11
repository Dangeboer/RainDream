package com.dangeboer.raindream.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Media {
    @TableId(value = "item_id", type = IdType.INPUT)
    private Long itemId;

    private String thumbUrl;
    private String liveUrl;
}
