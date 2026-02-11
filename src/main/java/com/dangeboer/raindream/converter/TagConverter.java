package com.dangeboer.raindream.converter;

import com.dangeboer.raindream.model.entity.Tag;
import com.dangeboer.raindream.model.vo.TagVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagConverter {
    TagVO toVO(Tag tag);
}
