package com.dangeboer.raindream.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dangeboer.raindream.converter.TagConverter;
import com.dangeboer.raindream.exception.BadRequestException;
import com.dangeboer.raindream.exception.CanNotFoundException;
import com.dangeboer.raindream.exception.ForbiddenException;
import com.dangeboer.raindream.mapper.ItemTagMapper;
import com.dangeboer.raindream.mapper.TagMapper;
import com.dangeboer.raindream.model.entity.Tag;
import com.dangeboer.raindream.model.form.TagForm;
import com.dangeboer.raindream.model.vo.TagVO;
import com.dangeboer.raindream.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    private final TagMapper tagMapper;
    private final ItemTagMapper itemTagMapper;
    private final TagConverter tagConverter;

    @Override
    public List<TagVO> getTag(Long userId) {
        List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getUserId, userId));

        if (tags == null || tags.isEmpty()) {
            throw new CanNotFoundException();
        }
        return tags.stream().map(tagConverter::toVO).toList();
    }

    @Override
    public Long createTag(Long userId, TagForm tagForm) {
        if (tagForm == null || tagForm.getName() == null) {
            throw new BadRequestException();
        }
        String name = tagForm.getName().trim();
        if (name.isEmpty()) {
            throw new BadRequestException("标签名不能为空");
        }
        Long duplicated = tagMapper.countByUserIdAndExactName(userId, name);
        if (duplicated != null && duplicated > 0) {
            throw new BadRequestException("标签已存在");
        }

        Tag tag = new Tag(userId, name);
        return (long) tagMapper.insert(tag);
    }

    @Override
    public Long updateTag(Long userId, Long tagId, TagForm tagForm, Boolean force) {
        Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getId, tagId));

        if (tag == null) {
            throw new CanNotFoundException();
        } else if (!Objects.equals(tag.getUserId(), userId)) {
            throw new ForbiddenException();
        }

        Long relationCount = itemTagMapper.countByTagId(tagId);
        if (relationCount != null && relationCount > 0 && !Boolean.TRUE.equals(force)) {
            throw new BadRequestException("该标签已关联条目，确认后可继续更新");
        }

        Tag toUpdate = new Tag(tagId, tagForm.getName());

        tagMapper.updateById(toUpdate);
        return tagId;
    }

    @Override
    public Long deleteTag(Long userId, Long tagId) {
        Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getId, tagId));
        if (tag == null) {
            throw new CanNotFoundException();
        } else if (!Objects.equals(tag.getUserId(), userId)) {
            throw new ForbiddenException();
        }

        Long relationCount = itemTagMapper.countByTagId(tagId);
        if (relationCount != null && relationCount > 0) {
            throw new BadRequestException("该标签已关联条目，无法删除");
        }

        return (long) tagMapper.deleteById(tagId);
    }
}
