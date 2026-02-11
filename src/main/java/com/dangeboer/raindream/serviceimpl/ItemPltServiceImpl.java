package com.dangeboer.raindream.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dangeboer.raindream.mapper.ItemPltMapper;
import com.dangeboer.raindream.model.entity.ItemPlt;
import com.dangeboer.raindream.service.ItemPltService;
import org.springframework.stereotype.Service;

@Service
public class ItemPltServiceImpl extends ServiceImpl<ItemPltMapper, ItemPlt> implements ItemPltService {
}
