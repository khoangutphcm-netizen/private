package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int userId;
    private String userEmail;
    private int totalPrice;
    private String date;
    private String status; // "Chờ xác nhận", "Đang giao", "Đã hoàn thành", "Đã hủy"
    private int shipperId;
    private String shipperName;
    private List<OrderItem> items;
    private int deliveryProgress;
    private String cloudKey;
    public int getDeliveryProgress() { return deliveryProgress; }
    public void setDeliveryProgress(int progress) { deliveryProgress = progress; }
    public String getDisplayId() { return cloudKey == null ? String.valueOf(id) : cloudKey; }
    public void setCloudKey(String key) { cloudKey = key; }

    public Order(int id, int userId, String userEmail, int totalPrice, String date, String status, int shipperId, String shipperName) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.totalPrice = totalPrice;
        this.date = date;
        this.status = status;
        this.shipperId = shipperId;
        this.shipperName = shipperName;
        this.items = new ArrayList<>();
    }

    public Order(int id, int userId, String userEmail, int totalPrice, String date, String status) {
        this(id, userId, userEmail, totalPrice, date, status, 0, "");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }

    public int getTotalPrice() { return totalPrice; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }

    public String getDate() { return date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getShipperId() { return shipperId; }
    public void setShipperId(int shipperId) { this.shipperId = shipperId; }

    public String getShipperName() { return shipperName; }
    public void setShipperName(String shipperName) { this.shipperName = shipperName; }

    public String getStatusVietnamese() {
        if ("Pending".equalsIgnoreCase(status) || "Chờ xác nhận".equalsIgnoreCase(status)) return "Chờ xác nhận";
        if ("Processing".equalsIgnoreCase(status) || "Đang xử lý".equalsIgnoreCase(status)) return "Đang xử lý";
        if ("Delivering".equalsIgnoreCase(status) || "Đang giao".equalsIgnoreCase(status)) return "Đang giao";
        if ("Completed".equalsIgnoreCase(status) || "Đã hoàn thành".equalsIgnoreCase(status)) return "Đã hoàn thành";
        if ("Cancelled".equalsIgnoreCase(status) || "Đã hủy".equalsIgnoreCase(status)) return "Đã hủy";
        return status != null ? status : "Chờ xác nhận";
    }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getSummaryText() {
        if (items == null || items.isEmpty()) {
            return "Mã đơn #" + id + " - Tổng: " + totalPrice + " VNĐ";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            sb.append(item.getFoodName()).append(" x").append(item.getQuantity());
            if (i < items.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
