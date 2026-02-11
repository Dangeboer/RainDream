package com.dangeboer.raindream.base;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {
    private Long total;
    private Long page;
    private Long size;
    private List<T> data;
}
