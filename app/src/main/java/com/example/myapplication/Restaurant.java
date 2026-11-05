package com.example.myapplication;

import java.util.ArrayList;

public class Restaurant {
    private int id;
    public String name;
    public int logo;
    public int cover;
    public String address;
    public String openHours;
    public ArrayList<Food> menu;

    public Restaurant(int id, String name, int logo, int cover, String address, String openHours) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.cover = cover;
        this.address = address;
        this.openHours = openHours;
    }

    public Restaurant(String name, int logo, int cover, String address, String openHours) {
        this(0, name, logo, cover, address, openHours);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getLogo() {
        return logo;
    }

    public int getCover() {
        return cover;
    }

    public String getAddress() {
        return address;
    }

    public String getOpenHoursRaw() {
        return openHours;
    }

    public void setMenu(ArrayList<Food> menu) {
        this.menu = menu;
    }

    public ArrayList<Food> getMenu() {
        return menu;
    }

    public String getOpenHours() {
        return "Open Hours: " + openHours;
    }
}
