package com.dangeboer.raindream.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dangeboer.raindream.converter.ItemConverter;
import com.dangeboer.raindream.converter.PltConverter;
import com.dangeboer.raindream.converter.TagConverter;
import com.dangeboer.raindream.mapper.*;
import com.dangeboer.raindream.model.entity.*;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.vo.FanficDetailVO;
import com.dangeboer.raindream.model.vo.FanficListVO;
import com.dangeboer.raindream.model.vo.ItemDetailVO;
import com.dangeboer.raindream.model.vo.ItemListVO;
import com.dangeboer.raindream.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {
    private final ItemMapper itemMapper;
    private final ItemConverter itemConverter;
    private final TagConverter tagConverter;
    private final PltConverter pltConverter;

    private final FanficMapper fanficMapper;
    private final MediaMapper mediaMapper;
    private final TagMapper tagMapper;
    private final PltMapper pltMapper;

    private final TagService tagService;
    private final PltService pltService;
    private final ItemTagService itemTagService;
    private final ItemPltService itemPltService;

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
    public ItemDetailVO getItemDetail(Long userId, Long itemId) throws NotFoundException {
        Item item = itemMapper.selectOne(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getUserId, userId)
                        .eq(Item::getId, itemId)
        );

        if (item == null) {
            throw new NotFoundException("未找到此项目");
        }

        ItemDetailVO itemDetailVO = itemConverter.toItemDetailVO(item);

        List<Tag> tags = tagMapper.selectByItemId(userId, itemId);
        List<Plt> plts = pltMapper.selectByItemId(userId, itemId);

        itemDetailVO.setTagVOS(tags.stream().map(tagConverter::toVO).toList());
        itemDetailVO.setPltVOS(plts.stream().map(pltConverter::toVO).toList());

        return itemDetailVO;
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
    public FanficDetailVO getFanficDetail(Long userId, Long itemId) throws NotFoundException {
        Item item = itemMapper.selectOne(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getUserId, userId)
                        .eq(Item::getId, itemId)
        );

        if (item == null) {
            throw new NotFoundException("未找到此项目");
        }

        FanficDetailVO fanficDetailVO = itemConverter.toFanficDetailVO(item, fanficMapper.selectById(itemId));

        List<Tag> tags = tagMapper.selectByItemId(userId, itemId);
        List<Plt> plts = pltMapper.selectByItemId(userId, itemId);

        fanficDetailVO.setTagVOS(tags.stream().map(tagConverter::toVO).toList());
        fanficDetailVO.setPltVOS(plts.stream().map(pltConverter::toVO).toList());

        return fanficDetailVO;
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

        // 1. 先插 item，拿到自增 id
        itemMapper.insert(item);
        Long itemId = item.getId();

        // 2. 如果是 fanfic 插 fanfic 表
        if (isFanfic) {
            Fanfic fanfic = itemConverter.toFanfic(itemForm.getFanficForm());
            fanfic.setItemId(itemId);
            fanficMapper.insert(fanfic);
        }
        // 3. 如果是 media 插 media 表
        else if (isMedia) {
            // TODO: 存封面url，以及live图需要存视频url
            Media media = new Media(itemId, null, null);
            mediaMapper.insert(media);
        }

        // 4. 处理 tags
        handleTags(userId, itemId, itemForm.getTags());

        // 5. 处理 plts
        handlePlts(userId, itemId, itemForm.getPlts());

        return Math.toIntExact(itemId);
    }

    private void handleTags(Long userId, Long itemId, List<String> tagsRaw) {
        handleNameRelations(
                userId, itemId, tagsRaw,

                names -> tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getUserId, userId)
                        .in(Tag::getTagName, names)),

                tagService::saveBatch,

                names -> tagService.list(new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getUserId, userId)
                        .in(Tag::getTagName, names)),

                name -> new Tag(userId, name),

                Tag::getTagName,
                Tag::getId,

                tagId -> new ItemTag(itemId, tagId),

                itemTagService::saveBatch
        );
    }

    private void handlePlts(Long userId, Long itemId, List<String> pltsRaw) {
        handleNameRelations(
                userId, itemId, pltsRaw,

                names -> pltMapper.selectList(new LambdaQueryWrapper<Plt>()
                        .eq(Plt::getUserId, userId)
                        .in(Plt::getPltName, names)),

                pltService::saveBatch,

                names -> pltService.list(new LambdaQueryWrapper<Plt>()
                        .eq(Plt::getUserId, userId)
                        .in(Plt::getPltName, names)),

                name -> new Plt(userId, name),

                Plt::getPltName,
                Plt::getId,

                pltId -> new ItemPlt(itemId, pltId),

                itemPltService::saveBatch
        );
    }

    private <E, R> void handleNameRelations(
            Long userId,
            Long itemId,
            List<String> rawNames,

            Function<List<String>, List<E>> selectExisting,
            Consumer<List<E>> saveEntitiesBatch,
            Function<List<String>, List<E>> selectAll,

            Function<String, E> newEntity,
            Function<E, String> getName,
            Function<E, Long> getId,

            Function<Long, R> newRelation,
            Consumer<List<R>> saveRelationsBatch
    ) {
        if (rawNames == null || rawNames.isEmpty()) return;

        // 1. 清洗
        List<String> names = rawNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (names.isEmpty()) return;

        // 2. 查已有
        List<E> existing = selectExisting.apply(names);
        Map<String, Long> nameToId = existing.stream()
                .collect(Collectors.toMap(getName, getId, (a, b) -> a));

        // 3. 插入缺失
        List<E> toInsert = names.stream()
                .filter(n -> !nameToId.containsKey(n))
                .map(newEntity)
                .toList();

        if (!toInsert.isEmpty()) {
            saveEntitiesBatch.accept(toInsert);
        }

        // 4. 再查全量
        List<E> all = selectAll.apply(names);
        Map<String, Long> nameToId2 = all.stream()
                .collect(Collectors.toMap(getName, getId, (a, b) -> a));

        // 5. 建立关系
        List<R> relations = names.stream()
                .map(nameToId2::get)
                .filter(Objects::nonNull)
                .map(newRelation)
                .toList();

        if (!relations.isEmpty()) {
            saveRelationsBatch.accept(relations);
        }
    }

//       原始方法
//    // 辅助函数：处理标签信息
//    private void handleTags(Long userId, Long itemId, List<String> tagsRaw) {
//        if (tagsRaw == null || tagsRaw.isEmpty()) {
//            return;
//        }
//
//        // 1. 清洗：trim、过滤空、去重（保持用户输入顺序）
//        List<String> tagNames = tagsRaw.stream()
//                .filter(s -> s != null)
//                .map(String::trim)
//                .filter(s -> !s.isEmpty())
//                .distinct()
//                .toList();
//
//        if (tagNames.isEmpty()) {
//            return;
//        }
//
//        // 2. 查已有 tag（当前用户范围内）
//        List<Tag> existing = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
//                .eq(Tag::getUserId, userId)
//                .in(Tag::getTagName, tagNames)
//        );
//
//        // name -> id
//        Map<String, Long> nameToId = existing.stream()
//                .collect(Collectors.toMap(Tag::getTagName, Tag::getId, (a, b) -> a));
//
//        // 3. 找出缺失的 tag
//        List<Tag> toInsert = tagNames.stream()
//                .filter(name -> !nameToId.containsKey(name))
//                .map(name -> new Tag(userId, name))
//                .toList();
//
//        // 批量插入
//        if (!toInsert.isEmpty()) {
//            tagService.saveBatch(toInsert);
//        }
//
//        // 重新查找出所有 tag
//        List<Tag> all = tagService.list(new LambdaQueryWrapper<Tag>()
//                .eq(Tag::getUserId, userId)
//                .in(Tag::getTagName, tagNames)
//        );
//        Map<String, Long> nameToId2 = all.stream()
//                .collect(Collectors.toMap(Tag::getTagName, Tag::getId, (a, b) -> a));
//
//
//        // 4. 绑定 item_tag
//        List<ItemTag> relations = tagNames.stream()
//                .map(name -> new ItemTag(itemId, nameToId2.get(name)))
//                .toList();
//
//        itemTagService.saveBatch(relations);
//    }
//
//    // 辅助函数：处理平台信息
//    private void handlePlts(Long userId, Long itemId, List<String> pltsRows) {
//
//        if (pltsRows == null || pltsRows.isEmpty()) {
//            return;
//        }
//
//        // 1. 清洗：trim、过滤空、去重（保持用户输入顺序）
//        List<String> pltNames = pltsRows.stream()
//                .filter(s -> s != null)
//                .map(String::trim)
//                .filter(s -> !s.isEmpty())
//                .distinct()
//                .toList();
//
//        if (pltNames.isEmpty()) {
//            return;
//        }
//
//        // 2. 查已有 plt（当前用户范围内）
//        List<Plt> existing = pltMapper.selectList(new LambdaQueryWrapper<Plt>()
//                .eq(Plt::getUserId, userId)
//                .in(Plt::getPltName, pltNames)
//        );
//
//        // name -> id
//        Map<String, Long> nameToId = existing.stream()
//                .collect(Collectors.toMap(Plt::getPltName, Plt::getId, (a, b) -> a));
//
//        // 3. 找出缺失的 plt
//        List<Plt> toInsert = pltNames.stream()
//                .filter(name -> !nameToId.containsKey(name))
//                .map(name -> new Plt(userId, name))
//                .toList();
//
//        // 批量插入
//        if (!toInsert.isEmpty()) {
//            pltService.saveBatch(toInsert);
//        }
//
//        // 重新查找出所有 plt
//        List<Plt> all = pltService.list(new LambdaQueryWrapper<Plt>()
//                .eq(Plt::getUserId, userId)
//                .in(Plt::getPltName, pltNames)
//        );
//        Map<String, Long> nameToId2 = all.stream()
//                .collect(Collectors.toMap(Plt::getPltName, Plt::getId, (a, b) -> a));
//
//
//        // 4. 绑定 item_plt
//        List<ItemPlt> relations = pltNames.stream()
//                .map(name -> new ItemPlt(itemId, nameToId2.get(name)))
//                .toList();
//
//        itemPltService.saveBatch(relations);
//    }
}
