package com.dangeboer.raindream.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dangeboer.raindream.model.entity.Tag;
import com.dangeboer.raindream.model.form.TagForm;
import com.dangeboer.raindream.model.vo.TagVO;

import java.util.List;

public interface TagService extends IService<Tag> {
    List<TagVO> getTag(Long userId);
    Long createTag(Long userId, TagForm tagForm);
    Long updateTag(Long userId, Long tagId, TagForm tagForm);
    Long deleteTag(Long userId, Long tagId);
}
