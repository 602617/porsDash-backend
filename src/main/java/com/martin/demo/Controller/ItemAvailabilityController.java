package com.martin.demo.Controller;


import com.martin.demo.dto.AvailabilityRequest;
import com.martin.demo.model.ItemAvailability;
import com.martin.demo.service.ItemAvailabilityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/items/{itemId}/availability")
public class ItemAvailabilityController {

    private final ItemAvailabilityService service;

    public ItemAvailabilityController(ItemAvailabilityService service) {
        this.service = service;
    }

    @GetMapping
    public List<ItemAvailability> list(@PathVariable Long itemId) {
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

    /**
    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> delete(
            @PathVariable Long itemId,
            @PathVariable Long slotId,
            Principal principal) {

        service.deleteSlot(itemId, slotId, principal.getName());
        return ResponseEntity.noContent().build();
    }
            */
}
