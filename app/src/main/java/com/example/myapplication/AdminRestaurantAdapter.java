package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminRestaurantAdapter extends RecyclerView.Adapter<AdminRestaurantAdapter.ViewHolder> {

    public interface OnRestaurantActionListener {
        void onDeleteRestaurant(Restaurant restaurant);
    }

    private List<Restaurant> restaurantList;
    private OnRestaurantActionListener listener;

    public AdminRestaurantAdapter(List<Restaurant> restaurantList, OnRestaurantActionListener listener) {
        this.restaurantList = restaurantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_admin_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        holder.tvName.setText(restaurant.getName());
        holder.tvAddress.setText(restaurant.getAddress());
        holder.tvOpenHours.setText(restaurant.getOpenHours());
        holder.ivLogo.setImageResource(restaurant.getLogo() != 0 ? restaurant.getLogo() : R.drawable.ic_seveneleven);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteRestaurant(restaurant);
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList != null ? restaurantList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvName, tvAddress, tvOpenHours;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.ivRestLogo);
            tvName = itemView.findViewById(R.id.tvRestName);
            tvAddress = itemView.findViewById(R.id.tvRestAddress);
            tvOpenHours = itemView.findViewById(R.id.tvRestOpenHours);
            btnDelete = itemView.findViewById(R.id.btnDeleteRestaurant);
        }
    }
}
