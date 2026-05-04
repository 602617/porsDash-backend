package com.martin.demo.Controller;


import com.martin.demo.dto.AvailabilityRequest;
import com.martin.demo.model.ItemAvailability;
import com.martin.demo.model.Items;
import com.martin.demo.repository.ItemRepository;
import com.martin.demo.service.FriendshipService;
import com.martin.demo.service.ItemAvailabilityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/items/{itemId}/availability")
public class ItemAvailabilityController {

    private final ItemAvailabilityService service;
    private final ItemRepository itemRepository;
    private final FriendshipService friendshipService;

    public ItemAvailabilityController(ItemAvailabilityService service,
                                      ItemRepository itemRepository,
                                      FriendshipService friendshipService) {
        this.service = service;
        this.itemRepository = itemRepository;
        this.friendshipService = friendshipService;
    }

    @GetMapping
    public List<ItemAvailability> list(@PathVariable Long itemId, Principal principal) {
        Items item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        if (!friendshipService.areFriends(principal.getName(), item.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du har ikke tilgang");
        }
        return service.listSlots(itemId);
    }

    @PostMapping
    public ResponseEntity<ItemAvailability> create(
            @PathVariable Long itemId,
            @RequestBody AvailabilityRequest req,
            Principal principal) {


        ItemAvailability slot = service.createSlot(
                itemId,
                req.getStartTime(),
                req.getEndTime(),
                principal.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(slot);
    }


    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> delete(
            @PathVariable Long itemId,
            @PathVariable Long slotId,
            Principal principal) {

        service.deleteSlot(itemId, slotId, principal.getName());
        return ResponseEntity.noContent().build();
    }

}
