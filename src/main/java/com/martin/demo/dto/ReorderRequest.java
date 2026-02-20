package com.martin.demo.dto;

public class ReorderRequest {
    private Long itemId;
    private int position;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
