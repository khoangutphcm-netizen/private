package com.example.myapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminFoodAdapter extends RecyclerView.Adapter<AdminFoodAdapter.ViewHolder> {

    public interface OnFoodActionListener {
        void onEditFood(Food food);
        void onDeleteFood(Food food);
        void onToggleAvailability(Food food);
        void onManageOptions(Food food);
    }

    private List<Food> foodList;
    private OnFoodActionListener listener;

    public AdminFoodAdapter(List<Food> foodList, OnFoodActionListener listener) {
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_admin_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.tvName.setText(food.getName());
        holder.tvPrice.setText(food.getPrice() + " VND");
        holder.ivImage.setImageResource(food.getImage() != 0 ? food.getImage() : R.drawable.ic_seveneleven);

        if (food.isAvailable()) {
            holder.btnToggleAvailability.setText("● Còn hàng");
            holder.btnToggleAvailability.setTextColor(Color.parseColor("#00B14F"));
            holder.btnToggleAvailability.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
        } else {
            holder.btnToggleAvailability.setText("● Hết hàng");
            holder.btnToggleAvailability.setTextColor(Color.parseColor("#D32F2F"));
            holder.btnToggleAvailability.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
        }

        holder.btnToggleAvailability.setOnClickListener(v -> {
            if (listener != null) listener.onToggleAvailability(food);
        });

        holder.btnManageOptions.setOnClickListener(v -> {
            if (listener != null) listener.onManageOptions(food);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditFood(food);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteFood(food);
        });
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice;
        Button btnToggleAvailability, btnManageOptions;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivAdminFood);
            tvName = itemView.findViewById(R.id.tvAdminFoodName);
            tvPrice = itemView.findViewById(R.id.tvAdminFoodPrice);
            btnToggleAvailability = itemView.findViewById(R.id.btnToggleAvailability);
            btnManageOptions = itemView.findViewById(R.id.btnManageOptions);
            btnEdit = itemView.findViewById(R.id.btnEditFood);
            btnDelete = itemView.findViewById(R.id.btnDeleteFood);
        }
    }
}
