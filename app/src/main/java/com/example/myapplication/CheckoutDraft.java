package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import java.util.LinkedHashMap;
import java.util.Map;

/** A draft belongs to the current login session; it is not a submitted order. */
public final class CheckoutDraft {
    static final Map<Food,Integer> items = new LinkedHashMap<>();
    static Restaurant restaurant;
    static String signature = "", owner = "";
    static String uid() { return FirebaseAuth.getInstance().getCurrentUser()==null?"":FirebaseAuth.getInstance().getCurrentUser().getUid(); }
    static boolean exists() { if (!owner.equals(uid())) clear(); return restaurant!=null&&!items.isEmpty(); }
    static void clear() { items.clear(); restaurant=null; signature=""; owner=""; }
    static void save(Restaurant r, Map<Food,Integer> cart, String version) {
        if(cart.isEmpty()) { if(restaurant!=null&&restaurant.getId()==r.getId()) clear(); return; }
        owner=uid(); restaurant=r; items.clear(); items.putAll(cart); signature=version;
    }
    static long total(){long total=0;for(Map.Entry<Food,Integer> e:items.entrySet())total+=(long)e.getKey().getPrice()*e.getValue();return total;}
    static void open(Context c) {
        if(!exists())return;
        Intent i=new Intent(c,RestaurantActivity.class);
        i.putExtra("restaurant_id",restaurant.getId());i.putExtra("restaurant_name",restaurant.getName());
        i.putExtra("restaurant_address",restaurant.getAddress());i.putExtra("restaurant_cover",restaurant.getCover());
        i.putExtra("restaurant_logo",restaurant.getLogo());i.putExtra("restaurant_open_hours",restaurant.getOpenHoursRaw());
        i.putExtra("open_checkout",true);c.startActivity(i);
    }
}
