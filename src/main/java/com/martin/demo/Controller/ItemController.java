package com.martin.demo.Controller;

import com.martin.demo.model.Items;
import com.martin.demo.repository.ItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public List<Items> getAllItems() {
        return itemRepository.findAll();
    }

    @PostMapping
    public Items createItem(@RequestBody Items item) {
        return itemRepository.save(item);
    }


}
