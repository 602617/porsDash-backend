package com.martin.demo.dto;

import java.math.BigDecimal;

public class CreateShoppingListItemRequest {
    private String name;
    private BigDecimal quantity;
    private String unit;
    private int position;
    private String note;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
