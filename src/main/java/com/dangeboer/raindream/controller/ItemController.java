package com.dangeboer.raindream.controller;

import com.dangeboer.raindream.model.entity.User;
import com.dangeboer.raindream.model.form.ItemForm;
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

    @GetMapping
    public List<ItemListVO> getMyItems(@AuthenticationPrincipal User user) {
        return itemService.getItemByUserId(user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Integer createItem(@AuthenticationPrincipal User user, @RequestBody ItemForm itemForm) {
        return itemService.createItem(user.getId(), itemForm);
    }
}
