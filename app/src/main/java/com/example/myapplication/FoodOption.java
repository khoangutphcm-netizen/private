package com.example.myapplication;

public class FoodOption {
    private int id;
    private int foodId;
    private String optionName;
    private int extraPrice;
    private boolean isSelected;

    public FoodOption(int id, int foodId, String optionName, int extraPrice) {
        this.id = id;
        this.foodId = foodId;
        this.optionName = optionName;
        this.extraPrice = extraPrice;
        this.isSelected = false;
    }

    public FoodOption(int foodId, String optionName, int extraPrice) {
        this(0, foodId, optionName, extraPrice);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public int getExtraPrice() {
        return extraPrice;
    }

    public void setExtraPrice(int extraPrice) {
        this.extraPrice = extraPrice;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
