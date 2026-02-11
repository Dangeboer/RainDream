package com.dangeboer.raindream.model.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 同人文 detail
 */
@Getter
@Setter
public class FanficDetailVO extends ItemDetailVO {
    // 补充上同人文信息
    private FanficVO fanficVO;
}
