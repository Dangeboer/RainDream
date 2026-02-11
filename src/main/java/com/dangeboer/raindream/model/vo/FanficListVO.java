package com.dangeboer.raindream.model.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 同人文 list
 */
@Getter
@Setter
public class FanficListVO extends ItemListVO {
    // 补充上同人文信息
    private FanficVO fanficVO;
}
