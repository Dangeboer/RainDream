package com.dangeboer.raindream.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dangeboer.raindream.model.entity.ItemEntity;
import com.dangeboer.raindream.model.vo.ItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItemMapper extends BaseMapper<ItemEntity> {
    List<ItemEntity> getItem();
}
