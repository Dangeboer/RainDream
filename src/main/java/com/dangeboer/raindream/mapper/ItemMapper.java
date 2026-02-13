package com.dangeboer.raindream.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dangeboer.raindream.model.entity.Item;
import com.dangeboer.raindream.model.vo.ItemListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {
    List<ItemListVO> getItemList();

    int insertBatch(@Param("list") List<Item> list);
}
