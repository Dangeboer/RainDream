package com.dangeboer.raindream.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dangeboer.raindream.model.entity.Plt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PltMapper extends BaseMapper<Plt> {
    List<Plt> selectByItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);
}
