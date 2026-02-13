package com.dangeboer.raindream.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dangeboer.raindream.model.entity.ItemPlt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemPltMapper extends BaseMapper<ItemPlt> {
    int insertBatch(@Param("list") List<ItemPlt> list);
}
