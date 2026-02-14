package com.dangeboer.raindream.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dangeboer.raindream.base.PageResult;
import com.dangeboer.raindream.model.entity.Item;
import com.dangeboer.raindream.model.form.ItemBatchForm;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.vo.FanficDetailVO;
import com.dangeboer.raindream.model.vo.FanficListVO;
import com.dangeboer.raindream.model.vo.ItemDetailVO;
import com.dangeboer.raindream.model.vo.ItemListVO;

import java.util.List;

public interface ItemService extends IService<Item> {
    PageResult<ItemListVO> getItemList(Long userId, Long page, Long size, Integer contentType, Integer mediaType);

    ItemDetailVO getItemDetail(Long userId, Long itemId);

    PageResult<FanficListVO> getFanficList(Long userId, Long page, Long size);
    FanficDetailVO getFanficDetail(Long userId, Long itemId);

    Long createItem(Long userId, ItemForm itemForm);

    Long deleteItem(Long userId, Long itemId);
    Long updateItem(Long userId, Long itemId, ItemForm itemForm);

    List<Long> createBatchItem(Long userId, ItemBatchForm itemBatchForm);
}
