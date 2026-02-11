package com.dangeboer.raindream.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dangeboer.raindream.model.entity.Item;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.vo.FanficDetailVO;
import com.dangeboer.raindream.model.vo.FanficListVO;
import com.dangeboer.raindream.model.vo.ItemDetailVO;
import com.dangeboer.raindream.model.vo.ItemListVO;

import java.util.List;

public interface ItemService extends IService<Item> {
    List<ItemListVO> getItemList(Long userId);
    ItemDetailVO getItemDetail(Long userId, Long itemId);

    List<FanficListVO> getFanficList(Long userId);
    FanficDetailVO getFanficDetail(Long userId, Long itemId);

    Long createItem(Long userId, ItemForm itemForm);

    Long deleteItem(Long userId, Long itemId);
    Long updateItem(Long userId, Long itemId, ItemForm itemForm);
}
