package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RestaurantFragment extends Fragment implements RestaurantAdapter.OnRestaurantItemClickListener {

    private final Runnable cloudRefresh = this::refreshCloud;
    private void refreshCloud() {
        if (dbHelper != null && dbHelper.isReady()) { loadRestaurants(); }
    }
    @Override public void onStart() {
        super.onStart();
        dbHelper.observe(cloudRefresh);
        cloudRefresh.run();
    }
    @Override public void onStop() {
        dbHelper.unobserve(cloudRefresh);
        super.onStop();
    }

    private RecyclerView rvRestaurants;
    private RestaurantAdapter adapter;
    private CloudRepository dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);

        rvRestaurants = view.findViewById(R.id.rvRestaurants);
        dbHelper = CloudRepository.getInstance(getContext());

        rvRestaurants.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRestaurants();
    }

    private void loadRestaurants() {
        List<Restaurant> list = dbHelper.getAllRestaurants();
        adapter = new RestaurantAdapter(list, this);
        rvRestaurants.setAdapter(adapter);
    }

    @Override
    public void onRestaurantItemClick(Restaurant restaurant) {
        Intent intent = new Intent(getActivity(), RestaurantActivity.class);
        if (restaurant != null) {
            intent.putExtra("restaurant_id", restaurant.getId());
            intent.putExtra("restaurant_name", restaurant.getName());
            intent.putExtra("restaurant_address", restaurant.getAddress());
            intent.putExtra("restaurant_open_hours", restaurant.getOpenHours());
            intent.putExtra("restaurant_cover", restaurant.getCover());
            intent.putExtra("restaurant_logo", restaurant.getLogo());
        }
        startActivity(intent);
    }
}
