package com.dangeboer.raindream.service;

import com.dangeboer.raindream.model.entity.ItemEntity;
import com.dangeboer.raindream.model.vo.ItemVO;

import java.util.List;

public interface ItemService {
    List<ItemEntity> getItem();
}
