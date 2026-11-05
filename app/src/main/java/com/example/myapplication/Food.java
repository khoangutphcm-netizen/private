package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class Food {
    private int id;
    private int restaurantId;
    public String name;
    public int image;
    public int price;
    public String description;
    public boolean isAvailable;
    private List<FoodOption> options;

    public Food(int id, int restaurantId, String name, int image, int price, String description, boolean isAvailable) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.name = name;
        this.image = image;
        this.price = price;
        this.description = description != null ? description : "Món ăn ngon đậm đà chuẩn vị.";
        this.isAvailable = isAvailable;
        this.options = new ArrayList<>();
    }

    public Food(int id, int restaurantId, String name, int image, int price) {
        this(id, restaurantId, name, image, price, "Món ăn ngon đậm đà chuẩn vị.", true);
    }

    public Food(String name, int image, int price) {
        this(0, 0, name, image, price, "Món ăn ngon đậm đà chuẩn vị.", true);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public List<FoodOption> getOptions() {
        return options;
    }

    public void setOptions(List<FoodOption> options) {
        this.options = options;
    }

    public static ArrayList<Food> getMockData() {
        ArrayList<Food> list = new ArrayList<>();
        list.add(new Food(1, 1, "Uranium Pizza", R.drawable.ic_seveneleven, 120000, "Bánh Pizza phô mai thơm ngon giòn rụm", true));
        list.add(new Food(2, 1, "Abyssal Burger", R.drawable.ic_seveneleven, 55000, "Burger bò nướng phô mai đậm đà", true));
        list.add(new Food(3, 1, "Nuclear Coke", R.drawable.ic_seveneleven, 35000, "Nước ngọt lạnh mát sảng khoái", true));
        list.add(new Food(4, 1, "Noddle of the Damned", R.drawable.ic_seveneleven, 65000, "Mì xào hải sản cay nồng", true));
        return list;
    }
}
