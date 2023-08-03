package com.expensetracker.expensetracker;

public class Category {

    String category_id, category, iconUrl, type;
    boolean isSelected;

    public Category(String category_id, String category, String iconUrl, String type, boolean isSelected) {
        this.category_id = category_id;
        this.category = category;
        this.iconUrl = iconUrl;
        this.type = type;
        this.isSelected = isSelected;
    }
}
