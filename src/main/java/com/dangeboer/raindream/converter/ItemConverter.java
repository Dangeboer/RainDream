package com.dangeboer.raindream.converter;

import com.dangeboer.raindream.model.entity.Item;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.vo.ItemListVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemConverter {

    // entity -> listVO
    ItemListVO toListVO(Item item);

    // form -> entity
    Item toItem(ItemForm itemForm);
}
