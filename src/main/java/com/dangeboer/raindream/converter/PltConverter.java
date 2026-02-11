package com.dangeboer.raindream.converter;

import com.dangeboer.raindream.model.entity.Plt;
import com.dangeboer.raindream.model.vo.PltVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PltConverter {
    PltVO toVO(Plt plt);
}
