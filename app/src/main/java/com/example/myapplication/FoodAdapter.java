package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface OnFoodItemClickListener {
        void onFoodItemClick(Food food);
    }

    private List<Food> foodList;
    private OnFoodItemClickListener listener;

    public FoodAdapter(List<Food> foodList) {
        this.foodList = foodList;
    }

    public FoodAdapter(List<Food> foodList, OnFoodItemClickListener listener) {
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.tvFoodName.setText(food.getName());
        holder.tvFoodPrice.setText(food.getPrice() + " VNĐ");
        holder.imgFood.setImageResource(food.getImage() != 0 ? food.getImage() : R.drawable.ic_seveneleven);

        if (food.isAvailable()) {
            holder.tvFoodStatus.setText("● Còn hàng");
            holder.tvFoodStatus.setTextColor(Color.parseColor("#00B14F"));
            holder.tvFoodStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
        } else {
            holder.tvFoodStatus.setText("● Hết hàng");
            holder.tvFoodStatus.setTextColor(Color.parseColor("#D32F2F"));
            holder.tvFoodStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodItemClick(food);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvFoodName, tvFoodPrice, tvFoodStatus;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.ivImage);
            tvFoodName = itemView.findViewById(R.id.tvName);
            tvFoodPrice = itemView.findViewById(R.id.tvPrice);
            tvFoodStatus = itemView.findViewById(R.id.tvFoodStatus);
        }
    }
}
