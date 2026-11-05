package com.example.myapplication;

public class OrderItem {
    private int id;
    private int orderId;
    private int foodId;
    private String foodName;
    private int price;
    private int quantity;

    public OrderItem(int id, int orderId, int foodId, String foodName, int price, int quantity) {
        this.id = id;
        this.orderId = orderId;
        this.foodId = foodId;
        this.foodName = foodName;
        this.price = price;
        this.quantity = quantity;
    }

    public OrderItem(String foodName, int price, int quantity) {
        this(0, 0, 0, foodName, price, quantity);
    }

    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public int getFoodId() { return foodId; }
    public String getFoodName() { return foodName; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void setOrderId(int orderId) { this.orderId = orderId; }
}
