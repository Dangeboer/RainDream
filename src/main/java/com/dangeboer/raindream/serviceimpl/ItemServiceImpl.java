package com.dangeboer.raindream.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dangeboer.raindream.converter.ItemConverter;
import com.dangeboer.raindream.mapper.ItemMapper;
import com.dangeboer.raindream.model.entity.Item;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.vo.ItemListVO;
import com.dangeboer.raindream.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {
    private final ItemMapper itemMapper;
    private final ItemConverter itemConverter;

    @Override
    public List<ItemListVO> getItemByUserId(Long userId) {

        // 等价于 SELECT * FROM item WHERE user_id = ?
        // selectList() 是 BaseMapper 自带的方法，查询符合条件的多条记录
        // new LambdaQueryWrapper<Item>()：MyBatis-Plus 提供的条件构造器（构造查询条件），用 Java 方式构造 SQL WHERE 条件。

        List<Item> items =  itemMapper.selectList(
                new LambdaQueryWrapper<Item>().eq(Item::getUserId, userId)
        );

        return items.stream().map(itemConverter::toListVO).toList();
    }

    @Override
    public Integer createItem(Long userId, ItemForm itemForm) {
        Item item = itemConverter.toItem(itemForm);
        item.setUserId(userId);
        return itemMapper.insert(item);
    }
}
