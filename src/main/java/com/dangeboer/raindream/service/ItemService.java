package com.dangeboer.raindream.service;

import com.dangeboer.raindream.model.entity.Item;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.vo.ItemListVO;

import java.util.List;

public interface ItemService {
    List<ItemListVO> getItemByUserId(Long userId);

    Integer createItem(Long id, ItemForm itemForm);
}
