package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RestaurantActivity extends CloudActivity implements FoodAdapter.OnFoodItemClickListener {

    private final Runnable cloudRefresh = this::refreshCloud;
    private void refreshCloud() {
        if (dbHelper != null && dbHelper.isReady()) {
            restaurant.setMenu(dbHelper.getFoodsByRestaurantId(restaurant.getId()));
            StringBuilder version=new StringBuilder();
            for(Food f:restaurant.getMenu()) {
                version.append(f.getId()).append(':').append(f.getName()).append(':').append(f.getPrice()).append(':').append(f.isAvailable());
                for(FoodOption o:f.getOptions()) version.append('/').append(o.getId()).append(':').append(o.getExtraPrice()).append(':').append(o.getOptionName());
            }
            String next=version.toString();
            if(!catalogSignature.isEmpty()&&!catalogSignature.equals(next)&&!cartMap.isEmpty()) {
                cartMap.clear(); totalItemCount=0;totalPrice=0;updateBasketUI();
                Toast.makeText(this,"Menu hoặc giá đã thay đổi. Vui lòng chọn lại món.",Toast.LENGTH_LONG).show();
            }
            catalogSignature=next;
            filterFoods(etSearchFood.getText().toString());
        }
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

    private TextView tvName, tvAddress, tvOpenHours;
    private ImageView ivCover;
    private RecyclerView rvFoods;
    private FoodAdapter foodAdapter;
    private Restaurant restaurant;

    private View layoutViewBasket;
    private TextView tvTotalPrices, tvTotalItems;
    private EditText etSearchFood;

    private CloudRepository dbHelper;
    private Map<Food, Integer> cartMap = new HashMap<>();
    private int totalItemCount = 0;
    private int totalPrice = 0;
    private String catalogSignature = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        dbHelper = CloudRepository.getInstance(this);

        Intent intent = getIntent();
        int restaurantId = intent.getIntExtra("restaurant_id", 1);
        String name = intent.getStringExtra("restaurant_name");
        String address = intent.getStringExtra("restaurant_address");
        String openHours = intent.getStringExtra("restaurant_open_hours");
        int cover = intent.getIntExtra("restaurant_cover", R.drawable.cover_menu_1);
        int logo = intent.getIntExtra("restaurant_logo", R.drawable.ic_seveneleven);

        restaurant = new Restaurant(restaurantId,
                name != null ? name : "7 Eleven",
                logo, cover,
                address != null ? address : "82 Nguyễn Thị Minh Khai, P.6, Q.3",
                openHours != null ? openHours : "06:00 - 23:00");

        ArrayList<Food> foods = dbHelper.getFoodsByRestaurantId(restaurantId);
        restaurant.setMenu(foods);

        rvFoods = findViewById(R.id.rvFoods);
        foodAdapter = new FoodAdapter(restaurant.getMenu(), this);
        rvFoods.setAdapter(foodAdapter);
        rvFoods.setLayoutManager(new LinearLayoutManager(this));

        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvOpenHours = findViewById(R.id.tvOpenHours);
        ivCover = findViewById(R.id.ivCover);

        layoutViewBasket = findViewById(R.id.layoutViewBasket);
        tvTotalPrices = findViewById(R.id.tvTotalPrices);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        etSearchFood = findViewById(R.id.etSearchFood);

        tvName.setText(restaurant.getName());
        tvAddress.setText(restaurant.getAddress());
        String rawHours = restaurant.getOpenHours() != null ? restaurant.getOpenHours().replaceAll("(?i)Giờ mở cửa:|Open Hours:", "").trim() : "06:00 - 23:00";
        tvOpenHours.setText("Giờ mở cửa: " + rawHours);
        ivCover.setImageResource(restaurant.getCover() != 0 ? restaurant.getCover() : R.drawable.cover_menu_1);

        if (etSearchFood != null) {
            etSearchFood.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterFoods(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        refreshCloud();
        if (CheckoutDraft.exists() && CheckoutDraft.restaurant.getId()==restaurant.getId()) {
            if (CheckoutDraft.signature.equals(catalogSignature)) {
                cartMap.putAll(CheckoutDraft.items);
                for(Map.Entry<Food,Integer> e:cartMap.entrySet()) { totalItemCount+=e.getValue(); totalPrice+=e.getKey().getPrice()*e.getValue(); }
            } else { CheckoutDraft.clear(); Toast.makeText(this,"Menu đã đổi. Vui lòng chọn lại món.",Toast.LENGTH_LONG).show(); }
        }
        updateBasketUI();
        if(getIntent().getBooleanExtra("open_checkout",false)&&totalItemCount>0) layoutViewBasket.post(this::showSimulatedCheckout);

        if (layoutViewBasket != null) {
            layoutViewBasket.setOnClickListener(v -> showSimulatedCheckout());
        }
    }

    private void filterFoods(String query) {
        if (restaurant == null || restaurant.getMenu() == null) return;
        if (query.trim().isEmpty()) {
            foodAdapter = new FoodAdapter(restaurant.getMenu(), this);
            rvFoods.setAdapter(foodAdapter);
        } else {
            List<Food> filteredList = new ArrayList<>();
            for (Food food : restaurant.getMenu()) {
                if (food.getName().toLowerCase().contains(query.toLowerCase()) ||
                    (food.getDescription() != null && food.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                    filteredList.add(food);
                }
            }
            foodAdapter = new FoodAdapter(filteredList, this);
            rvFoods.setAdapter(foodAdapter);
        }
    }

    @Override
    public void onFoodItemClick(Food food) {
        showFoodDetailDialog(food);
    }

    private void showFoodDetailDialog(Food food) {
        final String catalogAtOpen = catalogSignature;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_food_detail, null);

        ImageView ivImg = dialogView.findViewById(R.id.ivDetailFoodImage);
        TextView tvFoodName = dialogView.findViewById(R.id.tvDetailFoodName);
        TextView tvStatus = dialogView.findViewById(R.id.tvDetailFoodStatus);
        TextView tvPrice = dialogView.findViewById(R.id.tvDetailFoodPrice);
        TextView tvDesc = dialogView.findViewById(R.id.tvDetailFoodDesc);
        TextView tvQty = dialogView.findViewById(R.id.tvQuantity);
        Button btnMinus = dialogView.findViewById(R.id.btnMinus);
        Button btnPlus = dialogView.findViewById(R.id.btnPlus);
        Button btnAddToCart = dialogView.findViewById(R.id.btnAddToCart);

        TextView tvToppingsHeader = dialogView.findViewById(R.id.tvToppingsHeader);
        LinearLayout layoutOptionsContainer = dialogView.findViewById(R.id.layoutOptionsContainer);

        ivImg.setImageResource(food.getImage() != 0 ? food.getImage() : R.drawable.ic_seveneleven);
        tvFoodName.setText(food.getName());
        tvDesc.setText(food.getDescription());

        final int basePrice = food.getPrice();
        final int[] selectQty = {1};

        List<FoodOption> options = dbHelper.getOptionsForFood(food.getId());
        List<CheckBox> optionCheckBoxes = new ArrayList<>();

        if (options != null && !options.isEmpty()) {
            tvToppingsHeader.setVisibility(View.VISIBLE);
            layoutOptionsContainer.removeAllViews();

            for (FoodOption opt : options) {
                CheckBox cb = new CheckBox(this);
                cb.setText(opt.getOptionName() + " (+ " + opt.getExtraPrice() + " VNĐ)");
                cb.setTextSize(14);
                cb.setPadding(8, 8, 8, 8);

                cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    opt.setSelected(isChecked);
                    updateDialogTotalPrice(tvPrice, basePrice, options, selectQty[0]);
                });

                layoutOptionsContainer.addView(cb);
                optionCheckBoxes.add(cb);
            }
        } else {
            tvToppingsHeader.setVisibility(View.GONE);
        }

        updateDialogTotalPrice(tvPrice, basePrice, options, selectQty[0]);
        tvQty.setText(String.valueOf(selectQty[0]));

        if (food.isAvailable()) {
            tvStatus.setText("● Còn hàng");
            tvStatus.setTextColor(Color.parseColor("#00B14F"));
            btnAddToCart.setEnabled(true);
            btnAddToCart.setText("Thêm Vào Giỏ Hàng");
        } else {
            tvStatus.setText("● Hết hàng");
            tvStatus.setTextColor(Color.parseColor("#D32F2F"));
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Tạm Hết Hàng");
            btnAddToCart.setBackgroundColor(Color.parseColor("#BDBDBD"));
        }

        btnMinus.setOnClickListener(v -> {
            if (selectQty[0] > 1) {
                selectQty[0]--;
                tvQty.setText(String.valueOf(selectQty[0]));
                updateDialogTotalPrice(tvPrice, basePrice, options, selectQty[0]);
            }
        });

        btnPlus.setOnClickListener(v -> {
            if(selectQty[0]>=99) return;
            selectQty[0]++;
            tvQty.setText(String.valueOf(selectQty[0]));
            updateDialogTotalPrice(tvPrice, basePrice, options, selectQty[0]);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnAddToCart.setOnClickListener(v -> {
            if(!catalogAtOpen.equals(catalogSignature)) {
                Toast.makeText(this,"Menu đã thay đổi. Vui lòng chọn lại món.",Toast.LENGTH_LONG).show();dialog.dismiss();return;
            }
            if (!food.isAvailable()) {
                Toast.makeText(this, "Món ăn hiện tại đã hết hàng!", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder finalNameSb = new StringBuilder(food.getName());
            long toppingExtraPerUnit = 0;
            if (options != null) {
                List<String> chosenNames = new ArrayList<>();
                for (FoodOption opt : options) {
                    if (opt.isSelected()) {
                        chosenNames.add(opt.getOptionName());
                        toppingExtraPerUnit += opt.getExtraPrice();
                    }
                }
                if (!chosenNames.isEmpty()) {
                    finalNameSb.append(" (");
                    for (int i = 0; i < chosenNames.size(); i++) {
                        finalNameSb.append("+").append(chosenNames.get(i));
                        if (i < chosenNames.size() - 1) finalNameSb.append(", ");
                    }
                    finalNameSb.append(")");
                }
            }

            long unit = food.getPrice() + toppingExtraPerUnit;
            if(unit<0||unit*selectQty[0]+totalPrice>100000000L) {
                Toast.makeText(this,"Tổng đơn vượt giới hạn 100.000.000 VNĐ.",Toast.LENGTH_LONG).show();return;
            }
            int singleUnitPrice = (int)unit;
            Food customFoodItem = new Food(food.getId(), food.getRestaurantId(), finalNameSb.toString(), food.getImage(), singleUnitPrice, food.getDescription(), true);

            int count = cartMap.getOrDefault(customFoodItem, 0);
            cartMap.put(customFoodItem, count + selectQty[0]);
            totalItemCount += selectQty[0];
            totalPrice += (singleUnitPrice * selectQty[0]);
            updateBasketUI();

            Toast.makeText(this, "Đã thêm " + selectQty[0] + "x " + customFoodItem.getName() + " vào giỏ", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateDialogTotalPrice(TextView tvPrice, int basePrice, List<FoodOption> options, int quantity) {
        long extraPerUnit = 0;
        if (options != null) {
            for (FoodOption opt : options) {
                if (opt.isSelected()) {
                    extraPerUnit += opt.getExtraPrice();
                }
            }
        }
        long unitPrice = basePrice + extraPerUnit;
        long totalForItem = unitPrice * quantity;
        tvPrice.setText(totalForItem + " VNĐ (" + unitPrice + " VNĐ/món)");
    }

    private void updateBasketUI() {
        if(layoutViewBasket!=null) layoutViewBasket.setVisibility(totalItemCount>0?View.VISIBLE:View.GONE);
        if(restaurant!=null) CheckoutDraft.save(restaurant,cartMap,catalogSignature);
        if (tvTotalItems != null) {
            tvTotalItems.setText(totalItemCount + " món");
        }
        if (tvTotalPrices != null) {
            tvTotalPrices.setText(totalPrice + " VNĐ");
        }
    }

    private boolean checkoutPending;
    private AlertDialog checkoutDialog;
    private String checkoutMethod;

    private void showSimulatedCheckout() {
        if (checkoutPending || (checkoutDialog != null && checkoutDialog.isShowing())) return;
        if (totalItemCount == 0) {
            Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show(); return;
        }
        final String version = catalogSignature;
        View view = getLayoutInflater().inflate(R.layout.dialog_checkout, null);
        StringBuilder items = new StringBuilder();
        for (Map.Entry<Food, Integer> entry : cartMap.entrySet()) {
            items.append(entry.getKey().getName()).append(" × ").append(entry.getValue())
                .append("\n").append(String.format(Locale.getDefault(), "%,d đ", (long)entry.getKey().getPrice()*entry.getValue())).append("\n\n");
        }
        ((TextView)view.findViewById(R.id.checkoutItems)).setText(items.toString().trim());
        ((TextView)view.findViewById(R.id.checkoutTotal)).setText(String.format(Locale.getDefault(), "Tổng thanh toán: %,d đ", totalPrice));
        checkoutDialog = new AlertDialog.Builder(this).setView(view).create();
        view.findViewById(R.id.checkoutBack).setOnClickListener(v -> checkoutDialog.dismiss());
        view.findViewById(R.id.checkoutConfirm).setOnClickListener(v -> {
            if (checkoutPending) return;
            if (!version.equals(catalogSignature) || cartMap.isEmpty()) {
                checkoutDialog.dismiss();
                Toast.makeText(this, "Danh mục đã thay đổi. Vui lòng chọn lại món.", Toast.LENGTH_LONG).show(); return;
            }
            int selected = ((android.widget.RadioGroup)view.findViewById(R.id.checkoutMethods)).getCheckedRadioButtonId();
            checkoutMethod = selected == R.id.payBank ? "Chuyển khoản ngân hàng" : selected == R.id.payCard ? "Thẻ thanh toán" : "Ví điện tử";
            new AlertDialog.Builder(this).setTitle(checkoutMethod)
                .setMessage(String.format(Locale.getDefault(), "Xác nhận thanh toán thử nghiệm %,d đ?\nKhông trừ tiền thật.", totalPrice))
                .setPositiveButton("Xác nhận thanh toán", (d,w) -> {
                    if (!version.equals(catalogSignature)) {checkoutDialog.dismiss(); return;}
                    checkoutOrder();
                })
                .setNeutralButton("Thử giao dịch lỗi", (d,w) -> Toast.makeText(this, "Giao dịch không thành công. Chưa tạo đơn, bạn có thể chọn lại phương thức.", Toast.LENGTH_LONG).show())
                .setNegativeButton("Quay lại", null).show();
        });
        checkoutDialog.show();
        checkoutDialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void checkoutOrder() {
        if (checkoutPending) return;
        if (totalItemCount == 0) {
            Toast.makeText(this, "Giỏ hàng đang trống! Hãy chọn món ăn.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = dbHelper.currentUserId();
        String userEmail = prefs.getString("user_email", "user@app.com");

        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        Order newOrder = new Order(0, userId, userEmail, totalPrice, currentDate, "Chờ xác nhận");

        for (Map.Entry<Food, Integer> entry : cartMap.entrySet()) {
            Food food = entry.getKey();
            int qty = entry.getValue();
            OrderItem item = new OrderItem(0, 0, food.getId(), food.getName(), food.getPrice(), qty);
            newOrder.getItems().add(item);
        }

        checkoutPending = true;
        checkoutDialog.setCancelable(false);
        checkoutDialog.findViewById(R.id.checkoutConfirm).setEnabled(false);
        checkoutDialog.findViewById(R.id.checkoutBack).setEnabled(false);
        layoutViewBasket.setEnabled(false);
        dbHelper.createOrder(newOrder).addOnCompleteListener(task -> {
            checkoutPending = false;
            if (isFinishing() || isDestroyed()) return;
            checkoutDialog.setCancelable(true);
            checkoutDialog.findViewById(R.id.checkoutConfirm).setEnabled(true);
            checkoutDialog.findViewById(R.id.checkoutBack).setEnabled(true);
            layoutViewBasket.setEnabled(true);
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Chưa tạo được đơn: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
            CheckoutDraft.clear();
            cartMap.clear(); totalItemCount = 0; totalPrice = 0;
            updateBasketUI(); checkoutDialog.dismiss();
            new AlertDialog.Builder(this).setTitle("Đặt hàng thành công")
                .setMessage("Đơn đã được lưu trên Firebase.\nPhương thức vừa chọn: " + checkoutMethod + " (mô phỏng).\nBạn có thể theo dõi đơn trong mục Đơn hàng.")
                .setPositiveButton("Hoàn tất", (d,w) -> finish()).setOnCancelListener(d -> finish()).show();
        });
    }
}
