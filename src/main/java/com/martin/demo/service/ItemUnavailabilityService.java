package com.martin.demo.service;

import com.martin.demo.model.ItemUnavailability;
import com.martin.demo.model.Items;
import com.martin.demo.repository.ItemRepository;
import com.martin.demo.repository.ItemUnavailabilityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemUnavailabilityService {
    private final ItemRepository itemsRepo;
    private final ItemUnavailabilityRepository unavailRepo;

    public ItemUnavailabilityService(ItemRepository itemsRepo,
                                     ItemUnavailabilityRepository unavailRepo) {
        this.itemsRepo   = itemsRepo;
        this.unavailRepo = unavailRepo;
    }

    public ItemUnavailability blockPeriod(Long itemId,
                                          LocalDateTime start,
                                          LocalDateTime end,
                                          String username) {
        Items item = itemsRepo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item ikke funnet"));

        if (!item.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Kun eier kan blokkere perioder");
        }

        // Sjekk at du ikke overstyrer eksisterende blokker
        if (!unavailRepo.findOverlapping(itemId, start, end).isEmpty()) {
            throw new IllegalStateException("Overlapper med eksisterende blokkert periode");
        }

        ItemUnavailability blk = new ItemUnavailability();
        blk.setItem(item);
        blk.setStartTime(start);
        blk.setEndTime(end);
        return unavailRepo.save(blk);
    }

    public List<ItemUnavailability> listBlocks(Long itemId) {
        return unavailRepo.findByItemId(itemId);
    }

    public void deleteBlock(Long itemId, Long blockId, String username) {
        Items item = itemsRepo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item med id " + itemId + " ikke funnet"));

        if (!item.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Kun eier kan slette blokk-perioder");
        }

        // 2) Hent blokkeringsperioden og sjekk at den tilhører riktig item
        ItemUnavailability block = unavailRepo.findById(blockId)
                .orElseThrow(() -> new EntityNotFoundException("Blokk-periode med id " + blockId + " ikke funnet"));

        if (!block.getItem().getId().equals(itemId)) {
            throw new IllegalArgumentException("Denne blokk-perioden tilhører ikke item med id " + itemId);
        }

        // 3) Slett
        unavailRepo.delete(block);
    }
}
