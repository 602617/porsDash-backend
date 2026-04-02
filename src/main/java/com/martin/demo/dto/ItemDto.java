package com.martin.demo.dto;

import com.martin.demo.model.Items;

public class ItemDto {
    private Long id;
    private String name;
    private String username;
    private String imageUrl;

    public ItemDto() {
    }

    public ItemDto(Items item) {
        this.id = item.getId();
        this.name = item.getName();
        this.username = item.getUser() != null ? item.getUser().getUsername() : "unknown";
        this.imageUrl = item.getImageData() != null ? "/api/items/" + item.getId() + "/image" : null;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
