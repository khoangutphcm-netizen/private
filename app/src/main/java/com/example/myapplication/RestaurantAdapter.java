package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    public interface OnRestaurantItemClickListener {
        void onRestaurantItemClick(Restaurant restaurant);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress;
        ImageView ivImage;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvAddress = itemView.findViewById(R.id.tvAddress);
        }
    }

    private List<Restaurant> mRestaurants;
    private OnRestaurantItemClickListener mListener;

    public RestaurantAdapter(List<Restaurant> restaurants, OnRestaurantItemClickListener listener) {
        mRestaurants = restaurants;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final Restaurant restaurant = mRestaurants.get(position);
        viewHolder.tvName.setText(restaurant.name);
        viewHolder.ivImage.setImageResource(restaurant.logo);
        viewHolder.tvAddress.setText(restaurant.address);
        viewHolder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onRestaurantItemClick(restaurant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRestaurants != null ? mRestaurants.size() : 0;
    }
}