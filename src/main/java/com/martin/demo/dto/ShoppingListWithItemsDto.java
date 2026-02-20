/*package com.martin.demo.dto;

import com.martin.demo.ShoppingList;
import java.time.Instant;
import java.util.List;

public class ShoppingListWithItemsDto {
    private Long id;
    private String name;
    private String ownerUsername;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ShoppingListItemDto> items;

    public ShoppingListWithItemsDto(ShoppingList list, List<ShoppingListItemDto> items) {
        this.id = list.getId();
        this.name = list.getName();
        this.ownerUsername = list.getOwner().getUsername();
        this.createdAt = list.getCreatedAt();
        this.updatedAt = list.getUpdatedAt();
        this.items = items;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getOwnerUsername() { return ownerUsername; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<ShoppingListItemDto> getItems() { return items; }
}

*/