package com.martin.demo.Controller;

import com.martin.demo.dto.AvailabilityRequest;
import com.martin.demo.dto.UnavailabilityDto;
import com.martin.demo.model.ItemUnavailability;
import com.martin.demo.service.ItemUnavailabilityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/items/{itemId}/unavailability")
public class ItemUnavailabilityController {

    private final ItemUnavailabilityService service;

    public ItemUnavailabilityController(ItemUnavailabilityService service) {
        this.service = service;
    }

    @GetMapping
    public List<UnavailabilityDto> list(@PathVariable Long itemId) {
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
