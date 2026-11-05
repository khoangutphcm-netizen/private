package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class ShipperActivity extends CloudActivity {

    private final Runnable cloudRefresh = this::refreshCloud;
    private void refreshCloud() {
        if (dbHelper != null && dbHelper.isReady()) { loadTabContent(currentTab); }
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

    private TextView tvHeaderName, tvCompletedCount, tvEarnings, tvEmptyText;
    private Button btnSwitchView, btnSignOut;
    private TabLayout tabLayoutShipper;
    private RecyclerView rvShipperContent;

    private CloudRepository dbHelper;
    private int currentShipperId = 3;
    private String currentShipperName = "Shipper Nguyễn Văn Giao";
    private int currentTab = 0; // 0 = Available, 1 = Active (Simulate), 2 = History

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper);

        dbHelper = CloudRepository.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentShipperId = dbHelper.currentUserId();
        currentShipperName = prefs.getString("user_name", "Shipper Nguyễn Văn Giao");

        tvHeaderName = findViewById(R.id.tvShipperHeaderName);
        tvCompletedCount = findViewById(R.id.tvShipperCompletedCount);
        tvEarnings = findViewById(R.id.tvShipperEarnings);
        tvEmptyText = findViewById(R.id.tvShipperEmptyText);

        btnSwitchView = findViewById(R.id.btnShipperSwitchView);
        btnSignOut = findViewById(R.id.btnShipperSignOut);

        tabLayoutShipper = findViewById(R.id.tabLayoutShipper);
        rvShipperContent = findViewById(R.id.rvShipperContent);

        rvShipperContent.setLayoutManager(new LinearLayoutManager(this));

        tvHeaderName.setText(currentShipperName);

        setupTabs();
        updateEarningsSummary();

        btnSwitchView.setOnClickListener(v -> {
            Toast.makeText(ShipperActivity.this, "Đang chuyển sang Chế độ Xem App Khách", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ShipperActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnSignOut.setOnClickListener(v -> {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            prefs.edit().clear().apply();
            Intent intent = new Intent(ShipperActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        loadTabContent(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTabContent(currentTab);
    }

    private void setupTabs() {
        tabLayoutShipper.addTab(tabLayoutShipper.newTab().setText("Đơn Cần Giao"));
        tabLayoutShipper.addTab(tabLayoutShipper.newTab().setText("Đơn Đang Giao"));
        tabLayoutShipper.addTab(tabLayoutShipper.newTab().setText("Lịch Sử Giao"));

        tabLayoutShipper.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadTabContent(currentTab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateEarningsSummary() {
        List<Order> history = dbHelper.getShipperHistoryOrders(currentShipperId);
        int completedCount = 0;
        if (history != null) {
            for (Order o : history) {
                if ("Đã hoàn thành".equalsIgnoreCase(o.getStatus()) || "Completed".equalsIgnoreCase(o.getStatus())) {
                    completedCount++;
                }
            }
        }
        int totalEarnings = completedCount * 15000;

        tvCompletedCount.setText(completedCount + " Đơn");
        tvEarnings.setText(totalEarnings + " VNĐ");
    }

    private void loadTabContent(int position) {
        if (position == 0) {
            loadAvailableOrders();
        } else if (position == 1) {
            loadActiveOrders();
        } else {
            loadHistoryOrders();
        }
        updateEarningsSummary();
    }

    private void loadAvailableOrders() {
        List<Order> list = dbHelper.getAvailableOrdersForShipper();
        showOrdersList(list, 0);
    }

    private void loadActiveOrders() {
        List<Order> list = dbHelper.getShipperActiveOrders(currentShipperId);
        showOrdersList(list, 1);
    }

    private void loadHistoryOrders() {
        List<Order> list = dbHelper.getShipperHistoryOrders(currentShipperId);
        showOrdersList(list, 2);
    }

    private void showOrdersList(List<Order> list, int tabPos) {
        if (list == null || list.isEmpty()) {
            tvEmptyText.setVisibility(View.VISIBLE);
            rvShipperContent.setVisibility(View.GONE);
        } else {
            tvEmptyText.setVisibility(View.GONE);
            rvShipperContent.setVisibility(View.VISIBLE);

            ShipperOrderAdapter adapter = new ShipperOrderAdapter(list, new ShipperOrderAdapter.OnShipperActionListener() {
                @Override
                public void onAcceptOrder(Order order) {
                    CloudUi.write(ShipperActivity.this, dbHelper.assignOrderToShipper(order.getId(), currentShipperId, currentShipperName), () -> {

                        Toast.makeText(ShipperActivity.this, "Đã nhận giao đơn hàng #" + order.getId() + "!", Toast.LENGTH_SHORT).show();
                        if (tabLayoutShipper.getTabAt(1) != null) {
                            tabLayoutShipper.getTabAt(1).select();
                        }
                    });
                }

                @Override
                public void onCompleteDelivery(Order order) {
                    new AlertDialog.Builder(ShipperActivity.this)
                            .setTitle("Xác Nhận Giao Hàng Thành Công")
                            .setMessage("Bạn đã giao xong đơn hàng #" + order.getId() + " cho khách?")
                            .setPositiveButton("Xác Nhận Đã Giao", (dialog, which) -> {
                                CloudUi.write(ShipperActivity.this, dbHelper.updateOrderStatus(order.getId(), "Đã hoàn thành"), () -> {

                                    Toast.makeText(ShipperActivity.this, "Cảm ơn bạn! Đã hoàn tất đơn hàng #" + order.getId(), Toast.LENGTH_SHORT).show();
                                    loadActiveOrders();
                                });
                            })
                            .setNegativeButton("Chưa", null)
                            .show();
                }

                @Override
                public void onSimulateProgressUpdate(Order order, int progress, String statusText) {
                    dbHelper.updateProgress(order.getId(), progress, statusText)
                        .addOnFailureListener(e -> Toast.makeText(ShipperActivity.this, "Không cập nhật được tiến độ giao hàng.", Toast.LENGTH_SHORT).show());
                }
            }, tabPos);

            rvShipperContent.setAdapter(adapter);
        }
    }
}
