package com.dangeboer.raindream.controller;

import com.dangeboer.raindream.model.entity.ItemEntity;
import com.dangeboer.raindream.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/item")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemEntity> getItem() {
        return itemService.getItem();
    }
}
