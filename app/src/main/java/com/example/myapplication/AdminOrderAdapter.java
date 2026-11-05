package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    public interface OnOrderActionListener {
        void onChangeStatus(Order order);
        void onDeleteOrder(Order order);
        void onReceivedOrder(Order order);
        void onCancelOrder(Order order);
    }

    private List<Order> orderList;
    private OnOrderActionListener listener;
    private boolean isCustomerView;

    public AdminOrderAdapter(List<Order> orderList, OnOrderActionListener listener) {
        this(orderList, listener, false);
    }

    public AdminOrderAdapter(List<Order> orderList, OnOrderActionListener listener, boolean isCustomerView) {
        this.orderList = orderList;
        this.listener = listener;
        this.isCustomerView = isCustomerView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(isCustomerView ? R.layout.row_customer_order : R.layout.row_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("Đơn hàng " + order.getDisplayId() + " (" + order.getDate() + ")");
        holder.tvCustomer.setText("Khách hàng: " + order.getUserEmail());
        holder.tvSummary.setText(order.getSummaryText());
        holder.tvTotal.setText("Tổng: " + order.getTotalPrice() + " VNĐ");

        String displayStatus = order.getStatusVietnamese();
        holder.tvStatus.setText("Trạng thái: " + displayStatus + ("Đang giao".equals(displayStatus) ? " · " + order.getDeliveryProgress() + "%" : ""));

        if ("Chờ xác nhận".equals(displayStatus) || "Pending".equalsIgnoreCase(displayStatus)) {
            holder.tvStatus.setTextColor(Color.parseColor("#E65100"));
        } else if ("Đang giao".equals(displayStatus) || "Delivering".equalsIgnoreCase(displayStatus)) {
            holder.tvStatus.setTextColor(Color.parseColor("#1976D2"));
        } else if ("Đã hoàn thành".equals(displayStatus) || "Completed".equalsIgnoreCase(displayStatus)) {
            holder.tvStatus.setTextColor(Color.parseColor("#00B14F"));
        } else if ("Đã hủy".equals(displayStatus) || "Cancelled".equalsIgnoreCase(displayStatus)) {
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F"));
        }

        if (isCustomerView) {
            holder.tvOrderId.setText("Đơn #" + order.getDisplayId().substring(Math.max(0,order.getDisplayId().length()-8)));
            holder.tvCustomer.setText(order.getDate());
            holder.tvStatus.setText(displayStatus);
            holder.btnStatus.setVisibility(View.VISIBLE);
            holder.btnStatus.setText("Xem chi tiết →");
            holder.btnStatus.setOnClickListener(v -> OrderDetails.show(v.getContext(),order));
            holder.itemView.setOnClickListener(v -> OrderDetails.show(v.getContext(),order));
            holder.btnDelete.setVisibility(View.GONE);

            // Allow Cancel if Pending/Chờ xác nhận
            if ("Chờ xác nhận".equals(displayStatus) || "Pending".equalsIgnoreCase(displayStatus)) {
                holder.btnCancelOrder.setVisibility(View.VISIBLE);
                holder.btnCancelOrder.setOnClickListener(v -> {
                    if (listener != null) listener.onCancelOrder(order);
                });
            } else {
                holder.btnCancelOrder.setVisibility(View.GONE);
            }

            // Allow Received if Delivering/Đang giao
            if ("Đang giao".equals(displayStatus) || "Delivering".equalsIgnoreCase(displayStatus)) {
                holder.btnReceived.setVisibility(View.VISIBLE);
                holder.btnReceived.setOnClickListener(v -> {
                    if (listener != null) listener.onReceivedOrder(order);
                });
            } else {
                holder.btnReceived.setVisibility(View.GONE);
            }
        } else {
            holder.btnStatus.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnReceived.setVisibility(View.GONE);
            holder.btnCancelOrder.setVisibility(View.GONE);

            holder.btnStatus.setOnClickListener(v -> {
                if (listener != null) listener.onChangeStatus(order);
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteOrder(order);
            });
        }
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomer, tvSummary, tvTotal, tvStatus;
        Button btnStatus, btnReceived, btnCancelOrder;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomer = itemView.findViewById(R.id.tvOrderCustomer);
            tvSummary = itemView.findViewById(R.id.tvOrderSummary);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            btnStatus = itemView.findViewById(R.id.btnChangeStatus);
            btnReceived = itemView.findViewById(R.id.btnReceivedOrder);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
            btnDelete = itemView.findViewById(R.id.btnDeleteOrder);
        }
    }
}
