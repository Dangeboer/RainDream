package com.dangeboer.raindream.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dangeboer.raindream.model.entity.Fanfic;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FanficMapper extends BaseMapper<Fanfic> {
    int insertBatch(@Param("list") List<Fanfic> list);
}
