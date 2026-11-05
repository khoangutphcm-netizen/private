package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderFragment extends Fragment {

    private final Runnable cloudRefresh = this::refreshCloud;
    private void refreshCloud() {
        if (dbHelper != null && dbHelper.isReady()) { loadOrders(); }
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

    private RecyclerView rvCustomerOrders;
    private TextView tvNoOrders;
    private CloudRepository dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        rvCustomerOrders = view.findViewById(R.id.rvCustomerOrders);
        tvNoOrders = view.findViewById(R.id.tvNoOrders);
        dbHelper = CloudRepository.getInstance(getContext());

        rvCustomerOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        if (getContext() == null || getView()==null) return;
        android.widget.Button draft=getView().findViewById(R.id.btnPendingCheckout);
        draft.setVisibility(CheckoutDraft.exists()?View.VISIBLE:View.GONE);
        if(CheckoutDraft.exists()) {
            draft.setText("Chờ thanh toán · " + CheckoutDraft.restaurant.getName()+"\n"+String.format(java.util.Locale.getDefault(),"%,d đ · Xem giỏ và thanh toán →",CheckoutDraft.total()));
            draft.setOnClickListener(v -> CheckoutDraft.open(requireContext()));
        }
        SharedPreferences prefs = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = dbHelper.currentUserId();

        List<Order> orders = dbHelper.getOrdersByUserId(userId);

        if (orders == null || orders.isEmpty()) {
            tvNoOrders.setVisibility(View.VISIBLE);
            rvCustomerOrders.setVisibility(View.GONE);
        } else {
            tvNoOrders.setVisibility(View.GONE);
            rvCustomerOrders.setVisibility(View.VISIBLE);

            AdminOrderAdapter adapter = new AdminOrderAdapter(orders, new AdminOrderAdapter.OnOrderActionListener() {
                @Override
                public void onChangeStatus(Order order) {
                    // Customer view
                }

                @Override
                public void onDeleteOrder(Order order) {
                    // Customer view
                }

                @Override
                public void onReceivedOrder(Order order) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Xác Nhận Nhận Hàng")
                            .setMessage("Bạn đã nhận đủ món ăn cho Đơn hàng #" + order.getId() + " chưa?")
                            .setPositiveButton("Đã nhận hàng", (dialog, which) -> {
                                CloudUi.write(OrderFragment.this, dbHelper.updateOrderStatus(order.getId(), "Đã hoàn thành"), () -> {
                                    Toast.makeText(getContext(), "Cảm ơn bạn! Đơn hàng đã hoàn thành.", Toast.LENGTH_LONG).show();
                                    loadOrders();
                                });
                            })
                            .setNegativeButton("Chưa", null)
                            .show();
                }

                @Override
                public void onCancelOrder(Order order) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Xác Nhận Hủy Đơn")
                            .setMessage("Bạn có chắc chắn muốn hủy Đơn hàng #" + order.getId() + " không?")
                            .setPositiveButton("Hủy Đơn Hàng", (dialog, which) -> {
                                CloudUi.write(OrderFragment.this, dbHelper.updateOrderStatus(order.getId(), "Đã hủy"), () -> {
                                    Toast.makeText(getContext(), "Đã hủy đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                                    loadOrders();
                                });
                            })
                            .setNegativeButton("Quay lại", null)
                            .show();
                }
            }, true); // true = Customer View

            rvCustomerOrders.setAdapter(adapter);
        }
    }
}
