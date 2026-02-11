package com.dangeboer.raindream.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dangeboer.raindream.mapper.ItemTagMapper;
import com.dangeboer.raindream.model.entity.ItemTag;
import com.dangeboer.raindream.service.ItemTagService;
import org.springframework.stereotype.Service;

@Service
public class ItemTagServiceImpl extends ServiceImpl<ItemTagMapper, ItemTag> implements ItemTagService {
}
