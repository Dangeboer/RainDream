package com.dangeboer.raindream.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dangeboer.raindream.mapper.TagMapper;
import com.dangeboer.raindream.model.entity.Tag;
import com.dangeboer.raindream.service.TagService;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
}
