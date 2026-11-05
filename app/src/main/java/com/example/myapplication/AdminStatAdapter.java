package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminStatAdapter extends RecyclerView.Adapter<AdminStatAdapter.ViewHolder> {

    private List<FoodSaleStat> statList;

    public AdminStatAdapter(List<FoodSaleStat> statList) {
        this.statList = statList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_admin_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodSaleStat stat = statList.get(position);
        holder.tvName.setText(stat.getFoodName());
        holder.tvQtySold.setText("Đã bán: " + stat.getQuantitySold() + " phần");
        holder.tvRevenue.setText(stat.getTotalRevenue() + " VND");
    }

    @Override
    public int getItemCount() {
        return statList != null ? statList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQtySold, tvRevenue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStatFoodName);
            tvQtySold = itemView.findViewById(R.id.tvStatQtySold);
            tvRevenue = itemView.findViewById(R.id.tvStatRevenue);
        }
    }
}
