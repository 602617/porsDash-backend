package com.martin.demo.service;

import com.martin.demo.model.Items;
import com.martin.demo.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Optional<Items> findById(Long id) {
        return itemRepository.findById(id);
    }
}
