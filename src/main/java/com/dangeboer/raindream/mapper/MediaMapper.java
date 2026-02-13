package com.dangeboer.raindream.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dangeboer.raindream.model.entity.Media;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MediaMapper extends BaseMapper<Media> {
    int insertBatch(@Param("list") List<Media> list);
}
