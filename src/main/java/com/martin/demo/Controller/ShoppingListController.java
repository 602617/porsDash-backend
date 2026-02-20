package com.martin.demo.Controller;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.*;
import com.martin.demo.model.ShoppingList;
import com.martin.demo.model.ShoppingListItem;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.ShoppingListItemRepository;
import com.martin.demo.repository.ShoppingListRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/shoppinglists")
public class ShoppingListController {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository itemRepository;
    private final AppUserRepository appUserRepository;

    public ShoppingListController(ShoppingListRepository shoppingListRepository,
                                  ShoppingListItemRepository itemRepository,
                                  AppUserRepository appUserRepository) {
        this.shoppingListRepository = shoppingListRepository;
        this.itemRepository = itemRepository;
        this.appUserRepository = appUserRepository;
    }

    // -------------------------------------------------
    // LISTS
    // -------------------------------------------------

    /*
    // Get my shopping lists
    @GetMapping
    public List<ShoppingListDto> getMyLists(Principal principal) {
        AppUser user = getUser(principal);

        return shoppingListRepository.findByOwner(user)
                .stream()
                .map(ShoppingListDto::new)
                .toList();
    }
*/
    // Create new list
    @PostMapping
    public ResponseEntity<?> createList(@RequestBody CreateShoppingListRequest request,
                                        Principal principal) {

        AppUser user = getUser(principal);

        ShoppingList list = new ShoppingList();
        list.setName(request.getName());
        list.setOwner(user);

        shoppingListRepository.save(list);

        return ResponseEntity.status(HttpStatus.CREATED).body("Shopping list created");
    }
/*
    // Get list with items
    @GetMapping("/{listId}")
    public ShoppingListWithItemsDto getList(@PathVariable Long listId,
                                            Principal principal) {

        AppUser user = getUser(principal);

        ShoppingList list = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));

        // Security: only owner can see
        if (!list.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not allowed");
        }

        List<ShoppingListItemDto> items =
                itemRepository.findByListOrderByPositionAsc(listId)
                        .stream()
                        .map(ShoppingListItemDto::new)
                        .toList();

        return new ShoppingListWithItemsDto(list, items);
    }
*/
    // Delete list
    @DeleteMapping("/{listId}")
    public ResponseEntity<?> deleteList(@PathVariable Long listId,
                                        Principal principal) {

        AppUser user = getUser(principal);

        ShoppingList list = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));

        if (!list.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not allowed");
        }

        shoppingListRepository.delete(list);
        return ResponseEntity.ok("List deleted");
    }

    // -------------------------------------------------
    // ITEMS
    // -------------------------------------------------

    // Add item to list
    @PostMapping("/{listId}/items")
    public ResponseEntity<?> addItem(@PathVariable Long listId,
                                     @RequestBody CreateShoppingListItemRequest request,
                                     Principal principal) {

        AppUser user = getUser(principal);

        ShoppingList list = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));

        if (!list.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not allowed");
        }

        ShoppingListItem item = new ShoppingListItem();
        item.setList(list);
        item.setName(request.getName());
        item.setQuantity(request.getQuantity());
        item.setUnit(request.getUnit());
        item.setPosition(request.getPosition());

        itemRepository.save(item);

        return ResponseEntity.status(HttpStatus.CREATED).body("Item added");
    }

    // Toggle bought / unbought
    @PutMapping("/{listId}/items/{itemId}/toggle")
    public ResponseEntity<?> toggleItem(@PathVariable Long listId,
                                        @PathVariable Long itemId,
                                        Principal principal) {

        AppUser user = getUser(principal);

        ShoppingList list = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));

        if (!list.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not allowed");
        }

        ShoppingListItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setBought(!item.isBought());
        item.setBoughtAt(item.isBought() ? Instant.now() : null);

        itemRepository.save(item);

        return ResponseEntity.ok("Item updated");
    }

    // Delete item
    @DeleteMapping("/{listId}/items/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long listId,
                                        @PathVariable Long itemId,
                                        Principal principal) {

        AppUser user = getUser(principal);

        ShoppingList list = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));

        if (!list.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not allowed");
        }

        itemRepository.deleteById(itemId);

        return ResponseEntity.ok("Item deleted");
    }

    // Reorder items
    @PutMapping("/{listId}/items/reorder")
    public ResponseEntity<?> reorderItems(@PathVariable Long listId,
                                          @RequestBody List<ReorderRequest> request,
                                          Principal principal) {

        AppUser user = getUser(principal);

        ShoppingList list = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));

        if (!list.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not allowed");
        }

        for (ReorderRequest r : request) {
            ShoppingListItem item = itemRepository.findById(r.getItemId())
                    .orElseThrow();
            item.setPosition(r.getPosition());
            itemRepository.save(item);
        }

        return ResponseEntity.ok("Reordered");
    }

    // -------------------------------------------------
    // Helper
    // -------------------------------------------------

    private AppUser getUser(Principal principal) {
        return appUserRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}

