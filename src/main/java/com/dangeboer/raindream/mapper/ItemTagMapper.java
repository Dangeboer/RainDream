package com.dangeboer.raindream.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dangeboer.raindream.model.entity.ItemTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemTagMapper extends BaseMapper<ItemTag> {
    int insertBatch(@Param("list") List<ItemTag> list);

    Long countByTagId(@Param("tagId") Long tagId);
}
