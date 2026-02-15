package com.dangeboer.raindream.converter;

import com.dangeboer.raindream.model.entity.Fanfic;
import com.dangeboer.raindream.model.entity.Item;
import com.dangeboer.raindream.model.entity.Media;
import com.dangeboer.raindream.model.form.FanficForm;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.form.MediaForm;
import com.dangeboer.raindream.model.vo.*;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemConverter {
    // ======================= VO 相关 =======================================
    // itemEntity -> itemListVO
    @Mapping(target = "trackingTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(item.getTrackingType(), com.dangeboer.raindream.enums.TrackingTypeEnum.class))")
    @Mapping(target = "contentTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(item.getContentType(), com.dangeboer.raindream.enums.ContentTypeEnum.class))")
    @Mapping(target = "mediaTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(item.getMediaType(), com.dangeboer.raindream.enums.MediaTypeEnum.class))")
    ItemListVO toItemListVO(Item item);

    // itemEntity -> fanficListVO
    @InheritConfiguration(name = "toItemListVO")
    @Mapping(target = "trackingTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(item.getTrackingType(), com.dangeboer.raindream.enums.TrackingTypeEnum.class))")
    @Mapping(target = "fanficVO", source = "fanfic") // 会自动调用下面的 toFanficVO
    FanficListVO toFanficListVO(Item item, Fanfic fanfic);

    // itemEntity -> itemDetailVO
    @Mapping(
            target = "trackingTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(item.getTrackingType(), com.dangeboer.raindream.enums.TrackingTypeEnum.class))"
    )
    @Mapping(
            target = "contentTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(item.getContentType(), com.dangeboer.raindream.enums.ContentTypeEnum.class))"
    )
    @Mapping(
            target = "mediaTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(item.getMediaType(), com.dangeboer.raindream.enums.MediaTypeEnum.class))"
    )
    ItemDetailVO toItemDetailVO(Item item);

    // itemEntity -> fanficDetailVO
    @InheritConfiguration(name = "toItemDetailVO")
    @Mapping(target = "trackingTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(item.getTrackingType(), com.dangeboer.raindream.enums.TrackingTypeEnum.class))")
    @Mapping(target = "contentTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(item.getContentType(), com.dangeboer.raindream.enums.ContentTypeEnum.class))")
    @Mapping(target = "mediaTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(item.getMediaType(), com.dangeboer.raindream.enums.MediaTypeEnum.class))")
    @Mapping(target = "fanficVO", source = "fanfic") // 会自动调用下面的 toFanficVO
    FanficDetailVO toFanficDetailVO(Item item, Fanfic fanfic);

    // fanficEntity -> fanficVO
    @Mapping(
            target = "eraLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(fanfic.getEra(), com.dangeboer.raindream.enums.EraEnum.class))"
    )
    @Mapping(
            target = "lengthTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(fanfic.getLengthType(), com.dangeboer.raindream.enums.LengthTypeEnum.class))"
    )
    @Mapping(
            target = "workTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(fanfic.getWorkType(), com.dangeboer.raindream.enums.WorkTypeEnum.class))"
    )
    @Mapping(
            target = "endingTypeLabel",
            expression = "java(com.dangeboer.raindream.base.IBaseEnum.labelOf(fanfic.getEndingType(), com.dangeboer.raindream.enums.EndingTypeEnum.class))"
    )
    FanficVO toFanficVO(Fanfic fanfic);

    // ======================= FORM 相关 =======================================
    // itemForm -> itemEntity
    Item toItem(ItemForm itemForm);

    // FanficForm -> FanficEntity
    Fanfic toFanfic(FanficForm fanficForm);

    // MediaForm -> MediaEntity
    Media toMedia(MediaForm mediaForm);
}
