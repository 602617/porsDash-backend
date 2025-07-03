package com.martin.demo.Controller;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.CreateItemRequest;
import com.martin.demo.dto.ItemDto;
import com.martin.demo.model.Items;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.ItemRepository;
import com.martin.demo.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemRepository itemRepository;
    private final AppUserRepository appUserRepository;

    private final ItemService itemService;

    public ItemController(ItemRepository itemRepository, AppUserRepository appUserRepository, ItemService itemService) {
        this.itemRepository = itemRepository;
        this.appUserRepository = appUserRepository;
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemDto> getAllItems() {
        return itemRepository.findAll()
                .stream()
                .map(ItemDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/myproducts")
    public List<ItemDto> getMyProducts(Principal principal) {

        String username = principal.getName();
        AppUser user = appUserRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found") );

        return itemRepository.findByUser(user).stream().map(ItemDto::new).collect(Collectors.toList());

    }

    @PostMapping
    public ResponseEntity<?> createItem(@RequestBody CreateItemRequest request, Principal principal) {

        String username = principal.getName();

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Items item = new Items();
        item.setName(request.getName());
        item.setUser(user);

        itemRepository.save(item);

        return ResponseEntity.status(HttpStatus.CREATED).body("Item created");
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId, Principal principal) {

    AppUser user = appUserRepository.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException("Username not found"));

    Items item = itemRepository.findById(itemId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")
    );

    if(!item.getUser().getId().equals(user.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Du eier ikke dette produktet");
    }

    itemRepository.delete(item);

    return ResponseEntity.ok().build();

    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId, Principal principal) {
        // if you need auth: you can load the user from principal here
        return itemService
                .findById(itemId)
                .map(ItemDto::new)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item ikke funnet"));
    }

}
