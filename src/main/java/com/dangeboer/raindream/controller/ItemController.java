package com.dangeboer.raindream.controller;

import com.dangeboer.raindream.model.entity.User;
import com.dangeboer.raindream.model.form.ItemForm;
import com.dangeboer.raindream.model.vo.FanficDetailVO;
import com.dangeboer.raindream.model.vo.FanficListVO;
import com.dangeboer.raindream.model.vo.ItemDetailVO;
import com.dangeboer.raindream.model.vo.ItemListVO;
import com.dangeboer.raindream.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/item")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/list")
    public List<ItemListVO> getItemList(@AuthenticationPrincipal User user) {
        return itemService.getItemList(user.getId());
    }

    @GetMapping("/detail/{itemId}")
    public ItemDetailVO getItemDetail(@AuthenticationPrincipal User user, @PathVariable Long itemId) {
        return itemService.getItemDetail(user.getId(), itemId);
    }

    @GetMapping("/fanfic/list")
    public List<FanficListVO> getFanficList(@AuthenticationPrincipal User user) {
        return itemService.getFanficList(user.getId());
    }

    @GetMapping("/fanfic/detail/{itemId}")
    public FanficDetailVO getFanficDetail(@AuthenticationPrincipal User user, @PathVariable Long itemId) {
        return itemService.getFanficDetail(user.getId(), itemId);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
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
}
