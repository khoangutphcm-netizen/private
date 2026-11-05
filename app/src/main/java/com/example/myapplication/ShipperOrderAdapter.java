package com.example.myapplication;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipperOrderAdapter extends RecyclerView.Adapter<ShipperOrderAdapter.ViewHolder> {

    public interface OnShipperActionListener {
        void onAcceptOrder(Order order);
        void onCompleteDelivery(Order order);
        void onSimulateProgressUpdate(Order order, int progress, String statusText);
    }

    private List<Order> orderList;
    private OnShipperActionListener listener;
    private int tabPosition; // 0 = Available, 1 = Active (Simulate), 2 = History
    private Map<Integer, Integer> progressMap = new HashMap<>(); // Track active simulation progress per order ID
    private Map<Integer, Boolean> isSimulatingMap = new HashMap<>();

    public ShipperOrderAdapter(List<Order> orderList, OnShipperActionListener listener, int tabPosition) {
        this.orderList = orderList;
        this.listener = listener;
        this.tabPosition = tabPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_shipper_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("Đơn hàng " + order.getDisplayId() + " (" + order.getDate() + ")");
        holder.tvCustomer.setText("Khách hàng: " + order.getUserEmail());
        holder.tvSummary.setText(order.getSummaryText());
        holder.tvTotal.setText("Tổng tiền: " + order.getTotalPrice() + " VNĐ (Phí ship: +15,000 VNĐ)");

        String displayStatus = order.getStatusVietnamese();
        holder.tvStatus.setText("Trạng thái: " + displayStatus);

        if (tabPosition == 0) {
            // Tab 0: Available Orders to accept
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnSimulate.setVisibility(View.GONE);
            holder.btnComplete.setVisibility(View.GONE);
            holder.layoutSimulation.setVisibility(View.GONE);

            holder.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAcceptOrder(order);
            });
        } else if (tabPosition == 1) {
            // Tab 1: Active Delivery Orders (Simulate Ship)
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnSimulate.setVisibility(View.VISIBLE);
            holder.btnComplete.setVisibility(View.VISIBLE);
            holder.layoutSimulation.setVisibility(View.VISIBLE);

            int currentProgress = order.getDeliveryProgress();
            holder.pbProgress.setProgress(currentProgress);
            updateSimulateStatusText(holder.tvSimulateStatus, currentProgress);

            boolean isRunning = CloudRepository.getInstance(holder.itemView.getContext()).isSimulating(order.getId());
            if (isRunning) {
                holder.btnSimulate.setEnabled(false);
                holder.btnSimulate.setText("Đang Giao... 🛵");
            } else {
                holder.btnSimulate.setEnabled(true);
                holder.btnSimulate.setText("Chạy Mô Phỏng Ship 🚀");
            }

            holder.btnSimulate.setOnClickListener(v -> {
                CloudRepository.getInstance(holder.itemView.getContext()).simulateDelivery(order.getId());
                holder.btnSimulate.setEnabled(false);
            });

            holder.btnComplete.setOnClickListener(v -> {
                if (listener != null) listener.onCompleteDelivery(order);
            });
        } else {
            // Tab 2: History Orders
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnSimulate.setVisibility(View.GONE);
            holder.btnComplete.setVisibility(View.GONE);
            holder.layoutSimulation.setVisibility(View.GONE);
            holder.tvStatus.setTextColor(Color.parseColor("#00B14F"));
        }
    }

    private void startSimulateShip(Order order, ViewHolder holder) {
        int orderId = order.getId();
        if (isSimulatingMap.getOrDefault(orderId, false)) return;

        isSimulatingMap.put(orderId, true);
        holder.btnSimulate.setEnabled(false);
        holder.btnSimulate.setText("Đang Giao... 🛵");

        Handler handler = new Handler(Looper.getMainLooper());
        final int[] progress = {progressMap.getOrDefault(orderId, 0)};

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                progress[0] += 20;
                if (progress[0] > 100) progress[0] = 100;

                progressMap.put(orderId, progress[0]);
                holder.pbProgress.setProgress(progress[0]);

                String statusText = updateSimulateStatusText(holder.tvSimulateStatus, progress[0]);

                if (listener != null) {
                    listener.onSimulateProgressUpdate(order, progress[0], statusText);
                }

                if (progress[0] < 100) {
                    handler.postDelayed(this, 1200); // Progress tick every 1.2s
                } else {
                    isSimulatingMap.put(orderId, false);
                    holder.btnSimulate.setEnabled(true);
                    holder.btnSimulate.setText("Đã Đến Nơi 🎉");
                }
            }
        };

        handler.post(runnable);
    }

    private String updateSimulateStatusText(TextView tvSimulateStatus, int progress) {
        String statusText;
        if (progress <= 0) {
            statusText = "🚦 Tiến trình: Đã nhận đơn giao hàng";
            tvSimulateStatus.setTextColor(Color.parseColor("#1565C0"));
        } else if (progress <= 25) {
            statusText = "🛵 Tiến trình: Đã lấy hàng từ quán";
            tvSimulateStatus.setTextColor(Color.parseColor("#E65100"));
        } else if (progress <= 65) {
            statusText = "🚚 Tiến trình: Đang di chuyển trên đường";
            tvSimulateStatus.setTextColor(Color.parseColor("#1976D2"));
        } else if (progress <= 95) {
            statusText = "📍 Tiến trình: Đã đến địa chỉ khách hàng";
            tvSimulateStatus.setTextColor(Color.parseColor("#6A1B9A"));
        } else {
            statusText = "✅ Tiến trình: Giao hàng hoàn tất!";
            tvSimulateStatus.setTextColor(Color.parseColor("#00B14F"));
        }
        tvSimulateStatus.setText(statusText);
        return statusText;
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomer, tvSummary, tvTotal, tvStatus, tvSimulateStatus;
        Button btnAccept, btnSimulate, btnComplete;
        ProgressBar pbProgress;
        LinearLayout layoutSimulation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvShipperOrderId);
            tvCustomer = itemView.findViewById(R.id.tvShipperCustomer);
            tvSummary = itemView.findViewById(R.id.tvShipperSummary);
            tvTotal = itemView.findViewById(R.id.tvShipperTotal);
            tvStatus = itemView.findViewById(R.id.tvShipperOrderStatus);
            tvSimulateStatus = itemView.findViewById(R.id.tvSimulateStatus);

            btnAccept = itemView.findViewById(R.id.btnAcceptOrder);
            btnSimulate = itemView.findViewById(R.id.btnSimulateShip);
            btnComplete = itemView.findViewById(R.id.btnCompleteDelivery);

            pbProgress = itemView.findViewById(R.id.pbDeliveryProgress);
            layoutSimulation = itemView.findViewById(R.id.layoutSimulationSection);
        }
    }
}
