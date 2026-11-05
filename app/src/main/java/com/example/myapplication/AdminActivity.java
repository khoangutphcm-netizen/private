package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class AdminActivity extends CloudActivity {

    private final Runnable cloudRefresh = this::refreshCloud;
    private void refreshCloud() {
        if (dbHelper != null && dbHelper.isReady()) { loadTabContent(currentTabPosition); }
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

    private TabLayout tabLayoutAdmin;
    private RecyclerView rvAdminContent;
    private FloatingActionButton fabAdd;
    private Button btnAdminSignOut, btnSwitchUserView;

    private LinearLayout layoutStatsCards;
    private TextView tvTotalRevenue, tvTotalUsersCount, tvTotalFoodsCount, tvTotalOrdersCount;

    private CloudRepository dbHelper;
    private int currentTabPosition = 0; // 0 = Thống kê, 1 = Foods, 2 = Users, 3 = Restaurants, 4 = Orders

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = CloudRepository.getInstance(this);

        tabLayoutAdmin = findViewById(R.id.tabLayoutAdmin);
        rvAdminContent = findViewById(R.id.rvAdminContent);
        fabAdd = findViewById(R.id.fabAdd);
        btnAdminSignOut = findViewById(R.id.btnAdminSignOut);
        btnSwitchUserView = findViewById(R.id.btnSwitchUserView);

        layoutStatsCards = findViewById(R.id.layoutStatsCards);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalUsersCount = findViewById(R.id.tvTotalUsersCount);
        tvTotalFoodsCount = findViewById(R.id.tvTotalFoodsCount);
        tvTotalOrdersCount = findViewById(R.id.tvTotalOrdersCount);

        rvAdminContent.setLayoutManager(new LinearLayoutManager(this));

        setupTabs();

        btnAdminSignOut.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            prefs.edit().clear().apply();

            Intent intent = new Intent(AdminActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnSwitchUserView.setOnClickListener(v -> {
            Toast.makeText(AdminActivity.this, "Đang chuyển sang Chế độ Xem App Khách", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            startActivity(intent);
        });

        fabAdd.setOnClickListener(v -> handleFabClick());

        loadTabContent(0);
        if (dbHelper.isReady() && dbHelper.getAllRestaurants().isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Danh mục đang trống")
                .setMessage("Nạp 4 nhà hàng, 16 món ăn và topping mẫu lên Firebase để test?")
                .setPositiveButton("Nạp dữ liệu mẫu", (dialog, which) ->
                    CloudUi.write(this, dbHelper.seedDemoCatalog(), () -> loadTabContent(currentTabPosition)))
                .setNegativeButton("Để sau", null).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTabContent(currentTabPosition);
    }

    private void setupTabs() {
        tabLayoutAdmin.addTab(tabLayoutAdmin.newTab().setText("Thống Kê"));
        tabLayoutAdmin.addTab(tabLayoutAdmin.newTab().setText("Món Ăn"));
        tabLayoutAdmin.addTab(tabLayoutAdmin.newTab().setText("Người Dùng"));
        tabLayoutAdmin.addTab(tabLayoutAdmin.newTab().setText("Nhà Hàng"));
        tabLayoutAdmin.addTab(tabLayoutAdmin.newTab().setText("Đơn Hàng"));

        tabLayoutAdmin.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                loadTabContent(currentTabPosition);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadTabContent(int position) {
        if (position == 0) {
            layoutStatsCards.setVisibility(View.VISIBLE);
        } else {
            layoutStatsCards.setVisibility(View.GONE);
        }

        switch (position) {
            case 0:
                loadStatsTab();
                break;
            case 1:
                loadFoodsTab();
                break;
            case 2:
                loadUsersTab();
                break;
            case 3:
                loadRestaurantsTab();
                break;
            case 4:
                loadOrdersTab();
                break;
        }
    }

    // --- TAB 0: STATISTICS ---
    private void loadStatsTab() {
        fabAdd.setVisibility(View.GONE);

        long totalRev = dbHelper.getTotalRevenue();
        int totalUsers = dbHelper.getTotalUsersCount();
        int totalFoods = dbHelper.getTotalFoodsCount();
        int totalOrders = dbHelper.getTotalOrdersCount();

        tvTotalRevenue.setText(totalRev + " VND");
        tvTotalUsersCount.setText(totalUsers + " User");
        tvTotalFoodsCount.setText(totalFoods + " Món");
        tvTotalOrdersCount.setText(totalOrders + " Đơn");

        List<FoodSaleStat> stats = dbHelper.getFoodSalesStatistics();
        AdminStatAdapter adapter = new AdminStatAdapter(stats);
        rvAdminContent.setAdapter(adapter);
    }

    // --- TAB 1: FOODS ---
    private void loadFoodsTab() {
        fabAdd.setVisibility(View.VISIBLE);
        List<Food> foods = dbHelper.getAllFoods();
        AdminFoodAdapter adapter = new AdminFoodAdapter(foods, new AdminFoodAdapter.OnFoodActionListener() {
            @Override
            public void onEditFood(Food food) {
                showAddEditFoodDialog(food);
            }

            @Override
            public void onDeleteFood(Food food) {
                new AlertDialog.Builder(AdminActivity.this)
                        .setTitle("Xóa Món Ăn")
                        .setMessage("Bạn có chắc muốn xóa món '" + food.getName() + "'?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            CloudUi.write(AdminActivity.this, dbHelper.deleteFood(food.getId()), () -> {
                                Toast.makeText(AdminActivity.this, "Đã xóa món ăn", Toast.LENGTH_SHORT).show();
                                loadFoodsTab();
                            });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onToggleAvailability(Food food) {
                boolean newStatus = !food.isAvailable();
                CloudUi.write(AdminActivity.this, dbHelper.toggleFoodAvailability(food.getId(), newStatus), () -> {
                    Toast.makeText(AdminActivity.this, "Đã đổi trạng thái: " + (newStatus ? "Còn hàng" : "Hết hàng"), Toast.LENGTH_SHORT).show();
                    loadFoodsTab();
                });
            }

            @Override
            public void onManageOptions(Food food) {
                showManageOptionsDialog(food);
            }
        });
        rvAdminContent.setAdapter(adapter);
    }

    private void showManageOptionsDialog(Food food) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manage_options, null);
        TextView tvTitle = dialogView.findViewById(R.id.tvOptionDialogTitle);
        LinearLayout layoutExisting = dialogView.findViewById(R.id.layoutExistingOptions);
        EditText etName = dialogView.findViewById(R.id.etOptionName);
        EditText etPrice = dialogView.findViewById(R.id.etOptionPrice);
        Button btnAdd = dialogView.findViewById(R.id.btnAddOption);

        tvTitle.setText("Toppings cho: " + food.getName());

        Runnable refreshOptions = new Runnable() {
            @Override
            public void run() {
                layoutExisting.removeAllViews();
                List<FoodOption> options = dbHelper.getOptionsForFood(food.getId());
                if (options.isEmpty()) {
                    TextView tvEmpty = new TextView(AdminActivity.this);
                    tvEmpty.setText("Chưa có topping/tùy chọn nào.");
                    tvEmpty.setTextColor(0xFF757575);
                    layoutExisting.addView(tvEmpty);
                } else {
                    for (FoodOption opt : options) {
                        LinearLayout row = new LinearLayout(AdminActivity.this);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setPadding(0, 8, 0, 8);

                        TextView tvOpt = new TextView(AdminActivity.this);
                        tvOpt.setText("• " + opt.getOptionName() + " (+" + opt.getExtraPrice() + " VND)");
                        tvOpt.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                        tvOpt.setTextColor(0xFF212121);

                        Button btnDel = new Button(AdminActivity.this);
                        btnDel.setText("Xóa");
                        btnDel.setTextSize(11);
                        btnDel.setBackgroundColor(0xFFD32F2F);
                        btnDel.setTextColor(0xFFFFFFFF);
                        btnDel.setOnClickListener(v -> {
                            CloudUi.write(AdminActivity.this, dbHelper.deleteFoodOption(opt.getId()), this);
                        });

                        row.addView(tvOpt);
                        row.addView(btnDel);
                        layoutExisting.addView(row);
                    }
                }
            }
        };

        refreshOptions.run();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setNegativeButton("Đóng", null)
                .create();

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(AdminActivity.this, "Vui lòng nhập tên và giá topping", Toast.LENGTH_SHORT).show();
                return;
            }

            int extra = CloudUi.number(this,priceStr);
            if(extra<0) return;
            FoodOption newOpt = new FoodOption(food.getId(), name, extra);
            CloudUi.write(AdminActivity.this, dbHelper.addFoodOption(newOpt), () -> {
                Toast.makeText(AdminActivity.this, "Đã thêm topping!", Toast.LENGTH_SHORT).show();
                etName.setText("");
                etPrice.setText("");
                refreshOptions.run();
            });
        });

        dialog.show();
        dbHelper.observe(refreshOptions);
        dialog.setOnDismissListener(ignored -> dbHelper.unobserve(refreshOptions));
    }

    // --- TAB 2: USERS ---
    private void loadUsersTab() {
        fabAdd.setVisibility(View.VISIBLE);
        List<User> users = dbHelper.getAllUsers();
        AdminUserAdapter adapter = new AdminUserAdapter(users, new AdminUserAdapter.OnUserActionListener() {
            @Override
            public void onToggleRole(User user) {
                String[] roles = {"user", "shipper", "admin"};
                new AlertDialog.Builder(AdminActivity.this).setTitle("Chọn quyền")
                    .setItems(roles, (dialog, which) -> CloudUi.write(AdminActivity.this,
                        dbHelper.updateUserRole(user.getId(), roles[which]), () -> loadUsersTab())).show();

            }

            @Override
            public void onDeleteUser(User user) {
                new AlertDialog.Builder(AdminActivity.this)
                        .setTitle(user.isDisabled() ? "Mở khóa tài khoản" : "Khóa tài khoản")
                        .setMessage((user.isDisabled() ? "Mở khóa " : "Khóa quyền truy cập ứng dụng của ") + user.getEmail() + "?")
                        .setPositiveButton("Xác nhận", (dialog, which) -> {
                            CloudUi.write(AdminActivity.this, dbHelper.deleteUser(user.getId()), () -> {
                                Toast.makeText(AdminActivity.this, "Đã cập nhật quyền truy cập tài khoản", Toast.LENGTH_SHORT).show();
                                loadUsersTab();
                            });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        rvAdminContent.setAdapter(adapter);
    }

    // --- TAB 3: RESTAURANTS ---
    private void loadRestaurantsTab() {
        fabAdd.setVisibility(View.VISIBLE);
        List<Restaurant> restaurants = dbHelper.getAllRestaurants();
        AdminRestaurantAdapter adapter = new AdminRestaurantAdapter(restaurants, restaurant -> new AlertDialog.Builder(AdminActivity.this)
                .setTitle("Xóa Nhà Hàng")
                .setMessage("Xóa nhà hàng '" + restaurant.getName() + "' sẽ xóa tất cả món ăn liên quan. Tiếp tục?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    CloudUi.write(AdminActivity.this, dbHelper.deleteRestaurant(restaurant.getId()), () -> {
                        Toast.makeText(AdminActivity.this, "Đã xóa nhà hàng", Toast.LENGTH_SHORT).show();
                        loadRestaurantsTab();
                    });
                })
                .setNegativeButton("Hủy", null)
                .show());
        rvAdminContent.setAdapter(adapter);
    }

    // --- TAB 4: ORDERS ---
    private void loadOrdersTab() {
        fabAdd.setVisibility(View.GONE);
        List<Order> orders = dbHelper.getAllOrders();
        AdminOrderAdapter adapter = new AdminOrderAdapter(orders, new AdminOrderAdapter.OnOrderActionListener() {
            @Override
            public void onChangeStatus(Order order) {
                showOrderStatusSelectorDialog(order);
            }

            @Override
            public void onDeleteOrder(Order order) {
                new AlertDialog.Builder(AdminActivity.this)
                        .setTitle("Xóa Đơn Hàng")
                        .setMessage("Xóa đơn hàng #" + order.getId() + "?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            CloudUi.write(AdminActivity.this, dbHelper.deleteOrder(order.getId()), () -> {
                                Toast.makeText(AdminActivity.this, "Đã xóa đơn hàng", Toast.LENGTH_SHORT).show();
                                loadOrdersTab();
                            });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onReceivedOrder(Order order) {
                // Not used in Admin View
            }

            @Override
            public void onCancelOrder(Order order) {
                // Not used in Admin View
            }
        });
        rvAdminContent.setAdapter(adapter);
    }

    private void showOrderStatusSelectorDialog(Order order) {
        String[] statuses = new String[]{"Chờ xác nhận", "Đang giao", "Đã hoàn thành", "Đã hủy"};
        int currentSelection = 0;
        String orderStatusVN = order.getStatusVietnamese();
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(orderStatusVN)) {
                currentSelection = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn Trạng Thái Đơn #" + order.getId())
                .setSingleChoiceItems(statuses, currentSelection, (dialog, which) -> {
                    String selectedStatus = statuses[which];
                    CloudUi.write(AdminActivity.this, dbHelper.updateOrderStatus(order.getId(), selectedStatus), () -> {
                        Toast.makeText(AdminActivity.this, "Đã đổi đơn hàng sang: " + selectedStatus, Toast.LENGTH_SHORT).show();
                        loadOrdersTab();
                    });
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void handleFabClick() {
        if (currentTabPosition == 1) {
            showAddEditFoodDialog(null);
        } else if (currentTabPosition == 2) {
            showAddUserDialog();
        } else if (currentTabPosition == 3) {
            showAddRestaurantDialog();
        }
    }

    private void showAddEditFoodDialog(Food foodToEdit) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_food, null);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etName = dialogView.findViewById(R.id.etFoodName);
        EditText etPrice = dialogView.findViewById(R.id.etFoodPrice);
        EditText etRestId = dialogView.findViewById(R.id.etRestaurantId);
        StringBuilder restaurantsHint=new StringBuilder("ID nhà hàng: ");
        for(Restaurant r:dbHelper.getAllRestaurants()) restaurantsHint.append(r.getId()).append(" = ").append(r.getName()).append("; ");
        etRestId.setHint(restaurantsHint.toString());

        boolean isEdit = (foodToEdit != null);
        if (isEdit) {
            tvTitle.setText("Chỉnh Sửa Món Ăn");
            etName.setText(foodToEdit.getName());
            etPrice.setText(String.valueOf(foodToEdit.getPrice()));
            etRestId.setText(String.valueOf(foodToEdit.getRestaurantId()));
        }

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Lưu" : "Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String restIdStr = etRestId.getText().toString().trim();

                    if (name.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(AdminActivity.this, "Tên và giá món ăn không được trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int price = CloudUi.number(this,priceStr);
                    int restId = CloudUi.number(this,restIdStr);
                    if(price<0||restId<0) return;
                    boolean exists=false;
                    for(Restaurant r:dbHelper.getAllRestaurants()) if(r.getId()==restId) exists=true;
                    if(!exists) {Toast.makeText(this,"ID nhà hàng không tồn tại. Xem gợi ý trong ô nhập.",Toast.LENGTH_LONG).show();return;}

                    if (isEdit) {
                        foodToEdit.setName(name);
                        foodToEdit.setPrice(price);
                        foodToEdit.setRestaurantId(restId);
                        CloudUi.write(AdminActivity.this, dbHelper.updateFood(foodToEdit), () -> {
                            Toast.makeText(AdminActivity.this, "Đã cập nhật món ăn!", Toast.LENGTH_SHORT).show();
                            loadFoodsTab();
                        });
                    } else {
                        Food newFood = new Food(0, restId, name, R.drawable.ic_food, price, "Món ăn mới", true);
                        CloudUi.write(AdminActivity.this, dbHelper.addFood(newFood), () -> {
                            Toast.makeText(AdminActivity.this, "Thêm món mới thành công!", Toast.LENGTH_SHORT).show();
                            loadFoodsTab();
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddUserDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_signup, null);

        EditText etName = dialogView.findViewById(R.id.etSignUpName);
        EditText etEmail = dialogView.findViewById(R.id.etSignUpEmail);
        EditText etPass = dialogView.findViewById(R.id.etSignUpPassword);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Tạo User", (dialog, which) -> {
                    String email = etEmail.getText().toString().trim();
                    String pass = etPass.getText().toString();
                    String name = etName.getText().toString().trim();

                    if (email.isEmpty() || pass.isEmpty()) {
                        Toast.makeText(AdminActivity.this, "Email và mật khẩu là bắt buộc", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User newUser = new User(email, pass, name.isEmpty() ? "Người dùng" : name, "user");
                    CloudUi.write(AdminActivity.this, dbHelper.registerUser(newUser), () -> {
                        Toast.makeText(this, "Đã tạo tài khoản Firebase.", Toast.LENGTH_LONG).show();
                        loadUsersTab();
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddRestaurantDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_restaurant, null);
        EditText etName = dialogView.findViewById(R.id.etRestName);
        EditText etAddress = dialogView.findViewById(R.id.etRestAddress);
        EditText etHours = dialogView.findViewById(R.id.etRestOpenHours);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Thêm Nhà Hàng", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String address = etAddress.getText().toString().trim();
                    String hours = etHours.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(AdminActivity.this, "Tên nhà hàng không được trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Restaurant restaurant = new Restaurant(name, BrandLogos.forName(name), R.drawable.cover_menu_1,
                            address.isEmpty() ? "Địa chỉ chưa cập nhật" : address,
                            hours.isEmpty() ? "08:00 - 22:00" : hours);

                    CloudUi.write(AdminActivity.this, dbHelper.addRestaurant(restaurant), () -> {
                        Toast.makeText(AdminActivity.this, "Đã thêm nhà hàng!", Toast.LENGTH_SHORT).show();
                        loadRestaurantsTab();
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
