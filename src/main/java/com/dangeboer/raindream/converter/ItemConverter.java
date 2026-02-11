package com.dangeboer.raindream.converter;

import com.dangeboer.raindream.model.entity.Fanfic;
import com.dangeboer.raindream.model.entity.Item;
import com.dangeboer.raindream.model.form.FanficForm;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.vo.ItemListVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemConverter {

    // entity -> listVO
    @Mapping(
            target = "trackingTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(item.getTrackingType(), com.dangeboer.raindream.enums.TrackingTypeEnum.class))"
    )
    ItemListVO toListVO(Item item);

    // form -> entity
    Item toItem(ItemForm itemForm);

    // form -> entity (fanfic)
    Fanfic toFanfic(FanficForm fanficForm);
}
