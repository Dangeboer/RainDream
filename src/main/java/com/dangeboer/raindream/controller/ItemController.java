package com.dangeboer.raindream.controller;

import com.dangeboer.raindream.base.PageResult;
import com.dangeboer.raindream.model.entity.User;
import com.dangeboer.raindream.model.form.ItemBatchForm;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.vo.FanficDetailVO;
import com.dangeboer.raindream.model.vo.FanficListVO;
import com.dangeboer.raindream.model.vo.ItemDetailVO;
import com.dangeboer.raindream.model.vo.ItemListVO;
import com.dangeboer.raindream.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/item")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/list")
    public PageResult<ItemListVO> getItemList(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long page,
            @RequestParam(required = false) Long size
    ) {
        return itemService.getItemList(user.getId(), page, size);
    }

    @GetMapping("/detail/{itemId}")
    public ItemDetailVO getItemDetail(@AuthenticationPrincipal User user, @PathVariable Long itemId) {
        return itemService.getItemDetail(user.getId(), itemId);
    }

    @GetMapping("/fanfic/list")
    public PageResult<FanficListVO> getFanficList(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long page,
            @RequestParam(required = false) Long size
    ) {
        return itemService.getFanficList(user.getId(), page, size);
    }

    @GetMapping("/fanfic/detail/{itemId}")
    public FanficDetailVO getFanficDetail(@AuthenticationPrincipal User user, @PathVariable Long itemId) {
        return itemService.getFanficDetail(user.getId(), itemId);
    }

    @PostMapping("/create")
    public Long createItem(@AuthenticationPrincipal User user, @RequestBody ItemForm itemForm) {
        return itemService.createItem(user.getId(), itemForm);
    }

    @DeleteMapping("/delete/{itemId}")
    public Long deleteItem(@AuthenticationPrincipal User user, @PathVariable Long itemId) {
        return itemService.deleteItem(user.getId(), itemId);
    }

    @PutMapping("/update/{itemId}")
    public Long updateItem(@AuthenticationPrincipal User user, @PathVariable Long itemId, @RequestBody ItemForm itemForm) {
        return itemService.updateItem(user.getId(), itemId, itemForm);
    }

    @PostMapping("/create/batch")
    public List<Long> createBatchItem(@AuthenticationPrincipal User user, ItemBatchForm itemBatchForm) {
        return itemService.createBatchItem(user.getId(), itemBatchForm);
    }
}
