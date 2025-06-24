package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.model.ItemAvailability;
import com.martin.demo.model.Items;
import com.martin.demo.repository.ItemAvailabilityRepository;
import com.martin.demo.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemAvailabilityService {
    private final ItemRepository repoItem;
    private final ItemAvailabilityRepository repoAvail;

    public ItemAvailabilityService(ItemRepository repoItem,
                                   ItemAvailabilityRepository repoAvail) {
        this.repoItem  = repoItem;
        this.repoAvail = repoAvail;
    }

    public ItemAvailability createSlot(Long itemId, LocalDateTime start, LocalDateTime end, String ownerUsername) {
        Items item = repoItem.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item ikke funnet"));
        AppUser owner = item.getUser();
        if (owner == null) {
            throw new IllegalStateException("Item med id " + itemId + " har ingen eier");
        }
        if (!owner.getUsername().equals(ownerUsername)) {
            throw new AccessDeniedException("Kun eier kan endre tilgjengelighet");
        }

        var conflicts = repoAvail.findOverlapping(itemId, start, end);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Tidsrommet overlapper med eksisterende ledige perioder");
        }

        ItemAvailability slot = new ItemAvailability();
        slot.setItem(item);
        slot.setStartTime(start);
        slot.setEndTime(end);
        return repoAvail.save(slot);
    }


    public List<ItemAvailability> listSlots(Long itemId) {
        // Valgfritt: valider at item finnes
        repoItem.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item med id " + itemId + " ikke funnet"));
        // Returner alle slotene
        return repoAvail.findByItemIdOrderByStartTime(itemId);
    }
}
