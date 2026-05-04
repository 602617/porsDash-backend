package com.martin.demo.Controller;

import com.martin.demo.dto.AvailabilityRequest;
import com.martin.demo.dto.UnavailabilityDto;
import com.martin.demo.model.ItemUnavailability;
import com.martin.demo.model.Items;
import com.martin.demo.repository.ItemRepository;
import com.martin.demo.service.FriendshipService;
import com.martin.demo.service.ItemUnavailabilityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/items/{itemId}/unavailability")
public class ItemUnavailabilityController {

    private final ItemUnavailabilityService service;
    private final ItemRepository itemRepository;
    private final FriendshipService friendshipService;

    public ItemUnavailabilityController(ItemUnavailabilityService service,
                                        ItemRepository itemRepository,
                                        FriendshipService friendshipService) {
        this.service = service;
        this.itemRepository = itemRepository;
        this.friendshipService = friendshipService;
    }

    @GetMapping
    public List<UnavailabilityDto> list(@PathVariable Long itemId, Principal principal) {
        Items item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        if (!friendshipService.areFriends(principal.getName(), item.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du har ikke tilgang");
        }
        return service.listBlocks(itemId).stream()
                .map(slot -> new UnavailabilityDto(
                        slot.getId(),
                        slot.getItem().getId(),
                        slot.getStartTime(),
                        slot.getEndTime()
                ))
                .toList();
    }

    @PostMapping
    public ResponseEntity<ItemUnavailability> blockPeriod(
            @PathVariable Long itemId,
            @RequestBody AvailabilityRequest req,
            Principal principal) {

        ItemUnavailability blk = service.blockPeriod(
                itemId,
                req.getStartTime(),
                req.getEndTime(),
                principal.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(blk);
    }

    @DeleteMapping("/{blockId}")
    public ResponseEntity<?> delete(
            @PathVariable Long itemId,
            @PathVariable Long blockId,
            Principal principal) {

        service.deleteBlock(itemId, blockId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
