package com.dangeboer.raindream.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dangeboer.raindream.base.PageResult;
import com.dangeboer.raindream.converter.ItemConverter;
import com.dangeboer.raindream.converter.PltConverter;
import com.dangeboer.raindream.converter.TagConverter;
import com.dangeboer.raindream.exception.BadRequestException;
import com.dangeboer.raindream.exception.CanNotFoundException;
import com.dangeboer.raindream.exception.ForbiddenException;
import com.dangeboer.raindream.mapper.*;
import com.dangeboer.raindream.model.entity.*;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.vo.FanficDetailVO;
import com.dangeboer.raindream.model.vo.FanficListVO;
import com.dangeboer.raindream.model.vo.ItemDetailVO;
import com.dangeboer.raindream.model.vo.ItemListVO;
import com.dangeboer.raindream.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public PageResult<ItemListVO> getItemList(Long userId, Long page, Long size) {
        // 参数兜底，防止前端乱传
        page = page == null ? 1 : page;
        size = size == null ? 20 : Math.min(size, 100);

        // 查询条件
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<Item>()
                .eq(Item::getUserId, userId)
                .ne(Item::getContentType, 1)
                .orderByDesc(Item::getCreatedAt);

        // MyBatis-Plus 分页对象
        Page<Item> mpPage = new Page<>(page, size);
        IPage<Item> itemPage = itemMapper.selectPage(mpPage, wrapper);

        // records -> VO
        List<ItemListVO> voList = itemPage.getRecords()
                .stream()
                .map(itemConverter::toItemListVO)
                .toList();

        // 组装返回
        PageResult<ItemListVO> result = new PageResult<>();
        result.setTotal(itemPage.getTotal());
        result.setPage(itemPage.getCurrent());
        result.setSize(itemPage.getSize());
        // 不是 PageResult<List<ItemListVO>> 的原因是泛型设定的是 private List<T> data
        result.setData(voList);

        return result;
    }

    @Override
    public ItemDetailVO getItemDetail(Long userId, Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new CanNotFoundException();
        } else if (!Objects.equals(item.getUserId(), userId)) {
            throw new ForbiddenException();
        }

        ItemDetailVO itemDetailVO = itemConverter.toItemDetailVO(item);

        List<Tag> tags = tagMapper.selectByItemId(userId, itemId);
        List<Plt> plts = pltMapper.selectByItemId(userId, itemId);

        itemDetailVO.setTagVOS(tags.stream().map(tagConverter::toVO).toList());
        itemDetailVO.setPltVOS(plts.stream().map(pltConverter::toVO).toList());

        return itemDetailVO;
    }

    @Override
    public PageResult<FanficListVO> getFanficList(Long userId, Long page, Long size) {
        // 等价于 SELECT * FROM item WHERE user_id = ?
        // selectList() 是 BaseMapper 自带的方法，查询符合条件的多条记录
        // new LambdaQueryWrapper<Item>()：MyBatis-Plus 提供的条件构造器（构造查询条件），用 Java 方式构造 SQL WHERE 条件。

//        List<Item> items = itemMapper.selectList(
//                new LambdaQueryWrapper<Item>()
//                        .eq(Item::getUserId, userId)
//                        .eq(Item::getContentType, 1)
//        );

        page = (page == null || page < 1) ? 1 : page;
        size = (size == null || size < 1) ? 20 : Math.min(size, 100);

        // 1) 分页查当前用户的 fanfic item
        Page<Item> mpPage = new Page<>(page, size);

        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<Item>()
                .eq(Item::getUserId, userId)
                .eq(Item::getContentType, 1)
                .orderByDesc(Item::getCreatedAt);

        IPage<Item> itemPage = itemMapper.selectPage(mpPage, wrapper);
        List<Item> items = itemPage.getRecords();

        // 2) 批量查 fanfic（避免 N+1）
        List<Long> itemIds = items.stream().map(Item::getId).toList();

        // itemIds 为空时避免 in()
        List<Fanfic> fanfics = itemIds.isEmpty()
                ? List.of()
                : fanficMapper.selectList(new LambdaQueryWrapper<Fanfic>()
                .in(Fanfic::getItemId, itemIds));

        // 3) fanfic 按 itemId 建索引，方便组装
        Map<Long, Fanfic> idToFanfic = fanfics.stream()
                .collect(java.util.stream.Collectors.toMap(Fanfic::getItemId, f -> f));

        // 4) 组装 VO（item + fanfic），内部会自动匹配上 fanficVO
        List<FanficListVO> voList = items.stream()
                .map(item -> itemConverter.toFanficListVO(item, idToFanfic.get(item.getId())))
                .toList();

        // 5) 返回 PageResult
        PageResult<FanficListVO> result = new PageResult<>();
        result.setTotal(itemPage.getTotal());
        result.setPage(itemPage.getCurrent());
        result.setSize(itemPage.getSize());
        result.setData(voList);
        return result;
    }

    @Override
    public FanficDetailVO getFanficDetail(Long userId, Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new CanNotFoundException();
        } else if (!Objects.equals(item.getUserId(), userId)) {
            throw new ForbiddenException();
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
    public Long createItem(Long userId, ItemForm itemForm) {
        if (itemForm == null) {
            throw new BadRequestException();
        }
        boolean isFanfic = false;
        boolean isMedia = false;

        if (Integer.valueOf(1).equals(itemForm.getContentType())) {
            isFanfic = true;
        } else if (!Integer.valueOf(1).equals(itemForm.getMediaType()) && !Integer.valueOf(6).equals(itemForm.getMediaType())) {
            isMedia = true;
        }

        if (isFanfic && itemForm.getFanficForm() == null) {
            throw new BadRequestException("请提供文章详细信息");
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

        return itemId;
    }

    @Override
    public Long deleteItem(Long userId, Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new CanNotFoundException();
        } else if (!Objects.equals(item.getUserId(), userId)) {
            throw new ForbiddenException();
        }

        // 返回的是受影响的行数
        return (long) itemMapper.deleteById(itemId);
    }

    @Override
    public Long updateItem(Long userId, Long itemId, ItemForm itemForm) {
        // 0. 校验归属：只能更新自己的 item
        Item dbItem = itemMapper.selectById(itemId);
        if (dbItem == null) {
            throw new CanNotFoundException();
        } else if (!Objects.equals(dbItem.getUserId(), userId)) {
            throw new ForbiddenException();
        }

        // 1. 重新判断类型（用新表单决定最终形态）
        boolean isFanfic = false;
        boolean isMedia = false;

        if (Integer.valueOf(1).equals(itemForm.getContentType())) {
            isFanfic = true;
        } else if (!Integer.valueOf(1).equals(itemForm.getMediaType())
                && !Integer.valueOf(6).equals(itemForm.getMediaType())) {
            isMedia = true;
        }

        if (isFanfic && itemForm.getFanficForm() == null) {
            throw new BadRequestException("需要文章详细信息");
        }

        // 2. 更新 item 主表（注意：要 setId，否则 MP 不知道更新哪条）
        Item toUpdate = itemConverter.toItem(itemForm);
        toUpdate.setId(itemId);
        toUpdate.setUserId(userId); // 防止被前端篡改
        itemMapper.updateById(toUpdate);

        // 3. 处理 fanfic / media 切换
        // 你现在设计：fanfic/media 都以 itemId 为主键（或唯一）
        // 规则：以新表单为准 -> 该有的 upsert，不该有的 delete

        if (isFanfic) {
            // fanfic：upsert（存在就更新，不存在就插入）
            Fanfic existingFanfic = fanficMapper.selectById(itemId); // 前提：fanfic 的主键= item_id
            Fanfic fanfic = itemConverter.toFanfic(itemForm.getFanficForm());
            fanfic.setItemId(itemId);

            if (existingFanfic == null) {
                fanficMapper.insert(fanfic);
            } else {
                fanficMapper.updateById(fanfic);
            }

            // fanfic 形态下，不该有 media
            mediaMapper.deleteById(itemId);

        } else if (isMedia) {
            // media：upsert
            Media existingMedia = mediaMapper.selectById(itemId); // 前提：media 主键= item_id
            Media media = new Media(itemId, null, null); // TODO 你后续补字段
            if (existingMedia == null) {
                mediaMapper.insert(media);
            } else {
                mediaMapper.updateById(media);
            }

            // media 形态下，不该有 fanfic
            fanficMapper.deleteById(itemId);

        } else {
            // 既不是 fanfic 也不是 media：两个扩展表都不该有
            fanficMapper.deleteById(itemId);
            mediaMapper.deleteById(itemId);
        }

        // 这里没有处理 tag 和 plt 表的变化，用户体验可能变差：“我只是暂时没给任何 item 用这个 tag，但我还想以后继续用”——结果它被自动删了。
        // 4. tags：重建关系（以这次传参为准）
        // null=不修改，空数组=清空
        if (itemForm.getTags() != null) {
            itemTagService.remove(new LambdaQueryWrapper<ItemTag>().eq(ItemTag::getItemId, itemId));
            handleTags(userId, itemId, itemForm.getTags());
        }

        // 5. plts：同理
        if (itemForm.getPlts() != null) {
            itemPltService.remove(new LambdaQueryWrapper<ItemPlt>().eq(ItemPlt::getItemId, itemId));
            handlePlts(userId, itemId, itemForm.getPlts());
        }

        return itemId;
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
