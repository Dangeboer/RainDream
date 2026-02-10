package com.dangeboer.raindream.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dangeboer.raindream.mapper.ItemMapper;
import com.dangeboer.raindream.model.entity.ItemEntity;
import com.dangeboer.raindream.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, ItemEntity> implements ItemService {
    private final ItemMapper itemMapper;

    @Override
    public List<ItemEntity> getItem() {
        return itemMapper.getItem();
    }
}
