package com.dangeboer.raindream.controller;

import com.dangeboer.raindream.model.entity.User;
import com.dangeboer.raindream.model.form.TagForm;
import com.dangeboer.raindream.model.vo.TagVO;
import com.dangeboer.raindream.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    public List<TagVO> getTag(@AuthenticationPrincipal User user) {
        return tagService.getTag(user.getId());
    }

    @PostMapping("/create")
    public Long createTag(@AuthenticationPrincipal User user, @RequestBody TagForm tagForm) {
        return tagService.createTag(user.getId(), tagForm);
    }

    @PutMapping("/update/{tagId}")
    public Long updateTag(@AuthenticationPrincipal User user, @PathVariable Long tagId, @RequestBody TagForm tagForm) {
        return tagService.updateTag(user.getId(), tagId, tagForm);
    }

    @DeleteMapping("/delete/{tagId}")
    public Long deleteTag(@AuthenticationPrincipal User user, @PathVariable Long tagId) {
        return tagService.deleteTag(user.getId(), tagId);
    }
}
