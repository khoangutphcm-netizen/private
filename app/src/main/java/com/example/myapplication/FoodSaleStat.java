package com.example.myapplication;

public class FoodSaleStat {
    private String foodName;
    private int quantitySold;
    private int totalRevenue;

    public FoodSaleStat(String foodName, int quantitySold, int totalRevenue) {
        this.foodName = foodName;
        this.quantitySold = quantitySold;
        this.totalRevenue = totalRevenue;
    }

    public String getFoodName() {
        return foodName;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public int getTotalRevenue() {
        return totalRevenue;
    }
}
