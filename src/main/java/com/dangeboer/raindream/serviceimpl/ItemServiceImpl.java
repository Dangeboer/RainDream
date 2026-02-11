package com.dangeboer.raindream.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dangeboer.raindream.converter.ItemConverter;
import com.dangeboer.raindream.mapper.FanficMapper;
import com.dangeboer.raindream.mapper.ItemMapper;
import com.dangeboer.raindream.mapper.MediaMapper;
import com.dangeboer.raindream.model.entity.Fanfic;
import com.dangeboer.raindream.model.entity.Item;
import com.dangeboer.raindream.model.entity.Media;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.vo.FanficDetailVO;
import com.dangeboer.raindream.model.vo.FanficListVO;
import com.dangeboer.raindream.model.vo.ItemDetailVO;
import com.dangeboer.raindream.model.vo.ItemListVO;
import com.dangeboer.raindream.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {
    private final ItemMapper itemMapper;
    private final ItemConverter itemConverter;
    private final FanficMapper fanficMapper;
    private final MediaMapper mediaMapper;

    @Override
    public List<ItemListVO> getItemList(Long userId) {

        // 等价于 SELECT * FROM item WHERE user_id = ?
        // selectList() 是 BaseMapper 自带的方法，查询符合条件的多条记录
        // new LambdaQueryWrapper<Item>()：MyBatis-Plus 提供的条件构造器（构造查询条件），用 Java 方式构造 SQL WHERE 条件。

        List<Item> items = itemMapper.selectList(
                new LambdaQueryWrapper<Item>().eq(Item::getUserId, userId)
        );

        return items.stream().map(itemConverter::toItemListVO).toList();
    }

    @Override
    public ItemDetailVO getItemDetail(Long userId, Long itemId) {
        Item item = itemMapper.selectOne(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getUserId, userId)
                        .eq(Item::getId, itemId)
        );

        return itemConverter.toItemDetailVO(item);
    }

    @Override
    public List<FanficListVO> getFanficList(Long userId) {

        // 1. 找出当前用户所有同人文
        List<Item> items = itemMapper.selectList(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getUserId, userId)
                        .eq(Item::getContentType, 1)
        );

        List<FanficListVO> fanficListVOS = new ArrayList<>();
        for (Item item : items) {
            Long itemId = item.getId();
            // 2. 把 entity 转化成 fanficListVO，转化内部会自动匹配上 fanficVO
            FanficListVO fanficListVO = itemConverter.toFanficListVO(item, fanficMapper.selectById(itemId));
            fanficListVOS.add(fanficListVO);
        }

        return fanficListVOS;
    }

    @Override
    public FanficDetailVO getFanficDetail(Long userId, Long itemId) {
        Item item = itemMapper.selectOne(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getUserId, userId)
                        .eq(Item::getId, itemId)
        );

        return itemConverter.toFanficDetailVO(item, fanficMapper.selectById(itemId));
    }

    /**
     * 创建新项目
     *
     * @param userId   用户id
     * @param itemForm 项目信息
     * @return 项目id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer createItem(Long userId, ItemForm itemForm) {
        boolean isFanfic = false;
        boolean isMedia = false;

        if (Integer.valueOf(1).equals(itemForm.getContentType())) {
            isFanfic = true;
        } else if (!Integer.valueOf(1).equals(itemForm.getMediaType()) && !Integer.valueOf(6).equals(itemForm.getMediaType())) {
            isMedia = true;
        }

        if (isFanfic && itemForm.getFanficForm() == null) {
            throw new IllegalArgumentException("需要文章详细信息");
        }

        Item item = itemConverter.toItem(itemForm);
        item.setUserId(userId);

        // 1) 先插 item，拿到自增 id
        itemMapper.insert(item);
        Long itemId = item.getId();

        // 2) 如果是 fanfic 插 fanfic 表
        if (isFanfic) {
            Fanfic fanfic = itemConverter.toFanfic(itemForm.getFanficForm());
            fanfic.setItemId(itemId);
            fanficMapper.insert(fanfic);
        }

        // 3) 如果是 media 插 media 表
        if (isMedia) {
            // TODO: 存封面url，以及live图需要存视频url
            Media media = new Media(itemId, null, null);
            mediaMapper.insert(media);
        }

        return Math.toIntExact(itemId);
    }

}
