package com.dangeboer.raindream.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dangeboer.raindream.model.entity.Item;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.vo.FanficDetailVO;
import com.dangeboer.raindream.model.vo.FanficListVO;
import com.dangeboer.raindream.model.vo.ItemDetailVO;
import com.dangeboer.raindream.model.vo.ItemListVO;
import org.apache.ibatis.javassist.NotFoundException;

import java.util.List;

public interface ItemService extends IService<Item> {
    List<ItemListVO> getItemList(Long userId);
    ItemDetailVO getItemDetail(Long userId, Long itemId) throws NotFoundException;

    List<FanficListVO> getFanficList(Long userId);
    FanficDetailVO getFanficDetail(Long userId, Long itemId) throws NotFoundException;

    Long createItem(Long userId, ItemForm itemForm);

    Long deleteItem(Long itemId) throws NotFoundException;
    Long updateItem(Long userId, Long itemId, ItemForm itemForm);
}
