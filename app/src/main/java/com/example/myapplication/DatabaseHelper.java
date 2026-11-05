package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "app_database.db";
    private static final int DATABASE_VERSION = 7; // Upgraded version for fresh seed user_id and shipper order fixes

    // Table Users
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PASSWORD = "password";
    private static final String COL_USER_FULL_NAME = "full_name";
    private static final String COL_USER_ROLE = "role";
    private static final String COL_USER_AVATAR = "avatar";

    // Table Restaurants
    private static final String TABLE_RESTAURANTS = "restaurants";
    private static final String COL_REST_ID = "id";
    private static final String COL_REST_NAME = "name";
    private static final String COL_REST_LOGO = "logo";
    private static final String COL_REST_COVER = "cover";
    private static final String COL_REST_ADDRESS = "address";
    private static final String COL_REST_OPEN_HOURS = "open_hours";

    // Table Foods
    private static final String TABLE_FOODS = "foods";
    private static final String COL_FOOD_ID = "id";
    private static final String COL_FOOD_REST_ID = "restaurant_id";
    private static final String COL_FOOD_NAME = "name";
    private static final String COL_FOOD_PRICE = "price";
    private static final String COL_FOOD_IMAGE = "image";
    private static final String COL_FOOD_DESC = "description";
    private static final String COL_FOOD_AVAILABLE = "is_available";

    // Table Food Options / Toppings
    private static final String TABLE_FOOD_OPTIONS = "food_options";
    private static final String COL_OPT_ID = "id";
    private static final String COL_OPT_FOOD_ID = "food_id";
    private static final String COL_OPT_NAME = "option_name";
    private static final String COL_OPT_EXTRA_PRICE = "extra_price";

    // Table Orders
    private static final String TABLE_ORDERS = "orders";
    private static final String COL_ORDER_ID = "id";
    private static final String COL_ORDER_USER_ID = "user_id";
    private static final String COL_ORDER_USER_EMAIL = "user_email";
    private static final String COL_ORDER_TOTAL_PRICE = "total_price";
    private static final String COL_ORDER_DATE = "date";
    private static final String COL_ORDER_STATUS = "status";
    private static final String COL_ORDER_SHIPPER_ID = "shipper_id";
    private static final String COL_ORDER_SHIPPER_NAME = "shipper_name";

    // Table Order Items
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String COL_ITEM_ID = "id";
    private static final String COL_ITEM_ORDER_ID = "order_id";
    private static final String COL_ITEM_FOOD_ID = "food_id";
    private static final String COL_ITEM_FOOD_NAME = "food_name";
    private static final String COL_ITEM_PRICE = "price";
    private static final String COL_ITEM_QUANTITY = "quantity";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_EMAIL + " TEXT UNIQUE, " +
                COL_USER_PASSWORD + " TEXT, " +
                COL_USER_FULL_NAME + " TEXT, " +
                COL_USER_ROLE + " TEXT, " +
                COL_USER_AVATAR + " INTEGER DEFAULT 0)";

        String createRestaurantsTable = "CREATE TABLE " + TABLE_RESTAURANTS + " (" +
                COL_REST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REST_NAME + " TEXT, " +
                COL_REST_LOGO + " INTEGER, " +
                COL_REST_COVER + " INTEGER, " +
                COL_REST_ADDRESS + " TEXT, " +
                COL_REST_OPEN_HOURS + " TEXT)";

        String createFoodsTable = "CREATE TABLE " + TABLE_FOODS + " (" +
                COL_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FOOD_REST_ID + " INTEGER, " +
                COL_FOOD_NAME + " TEXT, " +
                COL_FOOD_PRICE + " INTEGER, " +
                COL_FOOD_IMAGE + " INTEGER, " +
                COL_FOOD_DESC + " TEXT, " +
                COL_FOOD_AVAILABLE + " INTEGER DEFAULT 1)";

        String createFoodOptionsTable = "CREATE TABLE " + TABLE_FOOD_OPTIONS + " (" +
                COL_OPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_OPT_FOOD_ID + " INTEGER, " +
                COL_OPT_NAME + " TEXT, " +
                COL_OPT_EXTRA_PRICE + " INTEGER)";

        String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + " (" +
                COL_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ORDER_USER_ID + " INTEGER, " +
                COL_ORDER_USER_EMAIL + " TEXT, " +
                COL_ORDER_TOTAL_PRICE + " INTEGER, " +
                COL_ORDER_DATE + " TEXT, " +
                COL_ORDER_STATUS + " TEXT, " +
                COL_ORDER_SHIPPER_ID + " INTEGER DEFAULT 0, " +
                COL_ORDER_SHIPPER_NAME + " TEXT)";

        String createOrderItemsTable = "CREATE TABLE " + TABLE_ORDER_ITEMS + " (" +
                COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ITEM_ORDER_ID + " INTEGER, " +
                COL_ITEM_FOOD_ID + " INTEGER, " +
                COL_ITEM_FOOD_NAME + " TEXT, " +
                COL_ITEM_PRICE + " INTEGER, " +
                COL_ITEM_QUANTITY + " INTEGER)";

        db.execSQL(createUsersTable);
        db.execSQL(createRestaurantsTable);
        db.execSQL(createFoodsTable);
        db.execSQL(createFoodOptionsTable);
        db.execSQL(createOrdersTable);
        db.execSQL(createOrderItemsTable);

        seedInitialData(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // Seed Users (1 Admin, 1 Main User, 1 Shipper, 4 Fake Customers)
        db.execSQL("INSERT INTO " + TABLE_USERS + " (email, password, full_name, role, avatar) VALUES ('admin@app.com', ?, 'Quản Trị Viên', 'admin', 0)" , new Object[]{PasswordHasher.hash("admin123")});
        db.execSQL("INSERT INTO " + TABLE_USERS + " (email, password, full_name, role, avatar) VALUES ('user@app.com', ?, 'Nguyễn Văn A', 'user', 0)" , new Object[]{PasswordHasher.hash("user123")});
        db.execSQL("INSERT INTO " + TABLE_USERS + " (email, password, full_name, role, avatar) VALUES ('shipper@app.com', ?, 'Shipper Nguyễn Văn Giao', 'shipper', " + R.drawable.ic_driver + ")" , new Object[]{PasswordHasher.hash("shipper123")});
        db.execSQL("INSERT INTO " + TABLE_USERS + " (email, password, full_name, role, avatar) VALUES ('alice@app.com', ?, 'Alice Nguyễn', 'user', 0)" , new Object[]{PasswordHasher.hash("alice123")});
        db.execSQL("INSERT INTO " + TABLE_USERS + " (email, password, full_name, role) VALUES ('bob@app.com', ?, 'Bob Trần', 'user')" , new Object[]{PasswordHasher.hash("bob123")});
        db.execSQL("INSERT INTO " + TABLE_USERS + " (email, password, full_name, role) VALUES ('charlie@app.com', ?, 'Charlie Lê', 'user')" , new Object[]{PasswordHasher.hash("charlie123")});
        db.execSQL("INSERT INTO " + TABLE_USERS + " (email, password, full_name, role) VALUES ('david@app.com', ?, 'David Phạm', 'user')" , new Object[]{PasswordHasher.hash("david123")});

        // Seed Restaurants
        long r1 = insertRestaurantSeed(db, "7 Eleven", R.drawable.ic_seveneleven, R.drawable.cover_menu_1, "82 Nguyễn Thị Minh Khai, P.6, Q.3", "06:00 - 23:00");
        long r2 = insertRestaurantSeed(db, "KFC Chicken", R.drawable.ic_seveneleven, R.drawable.cover_menu_1, "123 Lê Lợi, P. Bến Thành, Q.1", "08:00 - 22:00");
        long r3 = insertRestaurantSeed(db, "Circle K", R.drawable.ic_circlek, R.drawable.cover_menu_1, "45 Nguyễn Huệ, P. Bến Nghé, Q.1", "24/7 Mở cửa");
        long r4 = insertRestaurantSeed(db, "Highland Coffee", R.drawable.ic_highland, R.drawable.cover_menu_1, "200 Trần Hưng Đạo, Q.5", "07:00 - 23:00");

        // Seed Foods
        insertFoodSeed(db, (int) r1, "Uranium Pizza", 120000, R.drawable.ic_pizza, "Bánh Pizza phô mai xốt đặc biệt thơm ngon giòn rụm.", 1);
        insertFoodSeed(db, (int) r1, "Abyssal Burger", 55000, R.drawable.ic_burger, "Burger bò nướng đậm vị kết hợp phô mai xát lát hảo hạng.", 1);
        insertFoodSeed(db, (int) r1, "Nuclear Coke", 35000, R.drawable.ic_coke, "Nước ngọt có ga mát lạnh sảng khoái đập tan cơn khát.", 1);
        insertFoodSeed(db, (int) r1, "Noddle of the Damned", 65000, R.drawable.ic_noodle, "Mì xào hải sản cay nồng đậm đà chuẩn vị Á Đông.", 1);
        insertFoodSeed(db, (int) r1, "Bánh Mì Trứng", 25000, R.drawable.ic_banh_mi, "Bánh mì giòn kẹp trứng ốp la nóng hổi kèm dưa leo rau thơm.", 1);
        insertFoodSeed(db, (int) r1, "Trà Sữa Thái", 30000, R.drawable.ic_milk_tea, "Trà sữa Thái xanh đậm vị thơm béo kèm trân châu giòn.", 1);

        insertFoodSeed(db, (int) r2, "Fried Chicken Combo", 89000, R.drawable.dish, "Combo 2 miếng gà rán giòn rụm + 1 khoai tây chiên + 1 nước ngọt.", 1);
        insertFoodSeed(db, (int) r2, "Zinger Burger", 59000, R.drawable.dish_2, "Burger gà giòn cay đặc trưng xốt mayonnaise đậm vị.", 1);
        insertFoodSeed(db, (int) r2, "French Fries Large", 39000, R.drawable.dish, "Khoai tây chiên giòn tan cỡ lớn châm xốt cà chua.", 1);
        insertFoodSeed(db, (int) r2, "Pepsi Can", 20000, R.drawable.ic_black_coffee, "Lon Pepsi mát lạnh sảng khoái.", 1);

        insertFoodSeed(db, (int) r3, "Hot Dog Supreme", 29000, R.drawable.ic_circlek, "Bánh hot dog kẹp xúc xích Đức nướng xốt mù tạt.", 1);
        insertFoodSeed(db, (int) r3, "Milk Tea Boba", 32000, R.drawable.ic_milk_tea, "Trà sữa truyền thống ngọt thanh trân châu đen dai dẻo.", 1);
        insertFoodSeed(db, (int) r3, "Instant Ramen Egg", 22000, R.drawable.dish_2, "Mì ly nóng hổi chèn trứng lòng đào thơm ngon.", 0);

        insertFoodSeed(db, (int) r4, "Phin Sữa Đá", 39000, R.drawable.ic_black_coffee, "Cà phê phin truyền thống đậm đà hòa quyện sữa đặc ngọt ngào.", 1);
        insertFoodSeed(db, (int) r4, "Trà Sen Vàng", 49000, R.drawable.ic_highland, "Trà oolong thanh mát kết hợp hạt sen thơm bùi và củ năng.", 1);
        insertFoodSeed(db, (int) r4, "Freeze Trà Xanh", 55000, R.drawable.ic_highland, "Trà xanh đá xay mát lạnh phủ kem tươi ngọt béo.", 1);

        // Seed Food Options / Toppings
        insertOptionSeed(db, 7, "Sốt Mayo", 5000);
        insertOptionSeed(db, 7, "Sốt Cay Extra", 5000);
        insertOptionSeed(db, 7, "Thêm Phô Mai", 10000);

        insertOptionSeed(db, 8, "Bò Miếng Extra", 15000);
        insertOptionSeed(db, 8, "Sốt Mayo", 5000);

        insertOptionSeed(db, 1, "Thêm Phô Mai Mozzarella", 15000);
        insertOptionSeed(db, 1, "Thêm Xúc Xích", 10000);

        insertOptionSeed(db, 6, "Trân Châu Đen", 8000);
        insertOptionSeed(db, 6, "Thạch Trai", 8000);

        seedOrders(db);
    }

    private void seedOrders(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_ORDERS + " (id, user_id, user_email, total_price, date, status) VALUES (1001, 4, 'alice@app.com', 190000, '2026-10-01 11:30', 'Đã hoàn thành')");
        db.execSQL("INSERT INTO " + TABLE_ORDERS + " (id, user_id, user_email, total_price, date, status) VALUES (1002, 5, 'bob@app.com', 128000, '2026-10-02 12:15', 'Đã hoàn thành')");
        db.execSQL("INSERT INTO " + TABLE_ORDERS + " (id, user_id, user_email, total_price, date, status) VALUES (1003, 6, 'charlie@app.com', 88000, '2026-10-03 18:45', 'Đã hoàn thành')");
        db.execSQL("INSERT INTO " + TABLE_ORDERS + " (id, user_id, user_email, total_price, date, status) VALUES (1004, 7, 'david@app.com', 155000, '2026-10-04 19:20', 'Chờ xác nhận')");

        db.execSQL("INSERT INTO " + TABLE_ORDER_ITEMS + " (order_id, food_id, food_name, price, quantity) VALUES (1001, 1, 'Uranium Pizza (+Thêm Phô Mai)', 135000, 1)");
        db.execSQL("INSERT INTO " + TABLE_ORDER_ITEMS + " (order_id, food_id, food_name, price, quantity) VALUES (1001, 3, 'Nuclear Coke', 35000, 2)");
        db.execSQL("INSERT INTO " + TABLE_ORDER_ITEMS + " (order_id, food_id, food_name, price, quantity) VALUES (1002, 7, 'Fried Chicken Combo (+Sốt Mayo)', 94000, 1)");
        db.execSQL("INSERT INTO " + TABLE_ORDER_ITEMS + " (order_id, food_id, food_name, price, quantity) VALUES (1002, 9, 'French Fries Large', 39000, 1)");
        db.execSQL("INSERT INTO " + TABLE_ORDER_ITEMS + " (order_id, food_id, food_name, price, quantity) VALUES (1003, 15, 'Trà Sen Vàng', 49000, 1)");
        db.execSQL("INSERT INTO " + TABLE_ORDER_ITEMS + " (order_id, food_id, food_name, price, quantity) VALUES (1003, 14, 'Phin Sữa Đá', 39000, 1)");
        db.execSQL("INSERT INTO " + TABLE_ORDER_ITEMS + " (order_id, food_id, food_name, price, quantity) VALUES (1004, 2, 'Abyssal Burger', 55000, 2)");
        db.execSQL("INSERT INTO " + TABLE_ORDER_ITEMS + " (order_id, food_id, food_name, price, quantity) VALUES (1004, 16, 'Freeze Trà Xanh', 45000, 1)");
    }

    private long insertRestaurantSeed(SQLiteDatabase db, String name, int logo, int cover, String address, String openHours) {
        ContentValues cv = new ContentValues();
        cv.put(COL_REST_NAME, name);
        cv.put(COL_REST_LOGO, logo);
        cv.put(COL_REST_COVER, cover);
        cv.put(COL_REST_ADDRESS, address);
        cv.put(COL_REST_OPEN_HOURS, openHours);
        return db.insert(TABLE_RESTAURANTS, null, cv);
    }

    private long insertFoodSeed(SQLiteDatabase db, int restaurantId, String name, int price, int image, String desc, int isAvailable) {
        ContentValues cv = new ContentValues();
        cv.put(COL_FOOD_REST_ID, restaurantId);
        cv.put(COL_FOOD_NAME, name);
        cv.put(COL_FOOD_PRICE, price);
        cv.put(COL_FOOD_IMAGE, image);
        cv.put(COL_FOOD_DESC, desc);
        cv.put(COL_FOOD_AVAILABLE, isAvailable);
        return db.insert(TABLE_FOODS, null, cv);
    }

    private long insertOptionSeed(SQLiteDatabase db, int foodId, String optionName, int extraPrice) {
        ContentValues cv = new ContentValues();
        cv.put(COL_OPT_FOOD_ID, foodId);
        cv.put(COL_OPT_NAME, optionName);
        cv.put(COL_OPT_EXTRA_PRICE, extraPrice);
        return db.insert(TABLE_FOOD_OPTIONS, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESTAURANTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOODS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD_OPTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.beginTransaction();
        try (Cursor c = db.query(TABLE_USERS, new String[]{COL_USER_ID, COL_USER_PASSWORD}, null, null, null, null, null)) {
            while (c.moveToNext()) {
                String stored = c.getString(1);
                if (stored != null && !PasswordHasher.isEncoded(stored)) {
                    ContentValues values = new ContentValues();
                    values.put(COL_USER_PASSWORD, PasswordHasher.hash(stored));
                    db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(c.getInt(0))});
                }
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }

    // --- FOOD OPTIONS / TOPPINGS CRUD ---
    public List<FoodOption> getOptionsForFood(int foodId) {
        List<FoodOption> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FOOD_OPTIONS, null,
                COL_OPT_FOOD_ID + "=?",
                new String[]{String.valueOf(foodId)}, null, null, COL_OPT_ID + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_OPT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_OPT_NAME));
                int extra = cursor.getInt(cursor.getColumnIndexOrThrow(COL_OPT_EXTRA_PRICE));
                list.add(new FoodOption(id, foodId, name, extra));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public long addFoodOption(FoodOption option) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_OPT_FOOD_ID, option.getFoodId());
        cv.put(COL_OPT_NAME, option.getOptionName());
        cv.put(COL_OPT_EXTRA_PRICE, option.getExtraPrice());
        return db.insert(TABLE_FOOD_OPTIONS, null, cv);
    }

    public boolean deleteFoodOption(int optionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_FOOD_OPTIONS, COL_OPT_ID + "=?", new String[]{String.valueOf(optionId)});
        return rows > 0;
    }

    // --- USER CRUD ---
    public User cacheFirebaseUser(String email, String name, String role) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_FULL_NAME, name);
        values.put(COL_USER_ROLE, role);
        try (Cursor c = db.query(TABLE_USERS, new String[]{COL_USER_ID}, COL_USER_EMAIL + "=?", new String[]{email}, null, null, null)) {
            if (c.moveToFirst()) {
                int id = c.getInt(0);
                db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(id)});
                return getUserById(id);
            }
        }
        values.put(COL_USER_EMAIL, email);
        values.putNull(COL_USER_PASSWORD);
        long id = db.insertOrThrow(TABLE_USERS, null, values);
        return getUserById((int) id);
    }

    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_USERS, null, COL_USER_EMAIL + "=?", new String[]{email}, null, null, null)) {
            if (!c.moveToFirst()) return null;
            String encoded = c.getString(c.getColumnIndexOrThrow(COL_USER_PASSWORD));
            if (!PasswordHasher.verify(password, encoded)) return null;
            return new User(c.getInt(c.getColumnIndexOrThrow(COL_USER_ID)), email, encoded,
                    c.getString(c.getColumnIndexOrThrow(COL_USER_FULL_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_USER_ROLE)),
                    c.getInt(c.getColumnIndexOrThrow(COL_USER_AVATAR)));
        }
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD));
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_FULL_NAME));
            String role = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE));
            int avatar = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_AVATAR));
            user = new User(userId, email, password, fullName, role, avatar);
            cursor.close();
        }
        return user;
    }

    public boolean registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_EMAIL, user.getEmail());
        cv.put(COL_USER_PASSWORD, PasswordHasher.hash(user.getPassword()));
        cv.put(COL_USER_FULL_NAME, user.getFullName());
        cv.put(COL_USER_ROLE, user.getRole() != null ? user.getRole() : "user");
        cv.put(COL_USER_AVATAR, user.getAvatar());

        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public boolean updateUserProfile(int userId, String newName, int avatarResId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_FULL_NAME, newName);
        cv.put(COL_USER_AVATAR, avatarResId);
        int rows = db.update(TABLE_USERS, cv, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public boolean updateUserPassword(int userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_PASSWORD, PasswordHasher.hash(newPassword));
        int rows = db.update(TABLE_USERS, cv, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, COL_USER_ID + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD));
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_FULL_NAME));
                String role = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE));
                int avatar = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_AVATAR));
                list.add(new User(id, email, password, fullName, role, avatar));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public boolean updateUserRole(int userId, String newRole) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_ROLE, newRole);
        int rows = db.update(TABLE_USERS, cv, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_USERS, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    // --- RESTAURANT CRUD ---
    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RESTAURANTS, null, null, null, null, null, COL_REST_ID + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REST_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_REST_NAME));
                int logo = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REST_LOGO));
                int cover = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REST_COVER));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(COL_REST_ADDRESS));
                String openHours = cursor.getString(cursor.getColumnIndexOrThrow(COL_REST_OPEN_HOURS));
                Restaurant restaurant = new Restaurant(id, name, logo, cover, address, openHours);
                list.add(restaurant);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public long addRestaurant(Restaurant restaurant) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_REST_NAME, restaurant.getName());
        cv.put(COL_REST_LOGO, restaurant.getLogo());
        cv.put(COL_REST_COVER, restaurant.getCover());
        cv.put(COL_REST_ADDRESS, restaurant.getAddress());
        cv.put(COL_REST_OPEN_HOURS, restaurant.getOpenHoursRaw());
        return db.insert(TABLE_RESTAURANTS, null, cv);
    }

    public boolean deleteRestaurant(int restaurantId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FOODS, COL_FOOD_REST_ID + "=?", new String[]{String.valueOf(restaurantId)});
        int rows = db.delete(TABLE_RESTAURANTS, COL_REST_ID + "=?", new String[]{String.valueOf(restaurantId)});
        return rows > 0;
    }

    // --- FOOD CRUD ---
    public ArrayList<Food> getFoodsByRestaurantId(int restaurantId) {
        ArrayList<Food> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FOODS, null,
                COL_FOOD_REST_ID + "=?",
                new String[]{String.valueOf(restaurantId)}, null, null, COL_FOOD_ID + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FOOD_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOOD_NAME));
                int price = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FOOD_PRICE));
                int image = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FOOD_IMAGE));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOOD_DESC));
                boolean available = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FOOD_AVAILABLE)) == 1;

                Food food = new Food(id, restaurantId, name, image, price, desc, available);
                food.setOptions(getOptionsForFood(id));
                list.add(food);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Food> getAllFoods() {
        List<Food> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FOODS, null, null, null, null, null, COL_FOOD_ID + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FOOD_ID));
                int restId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FOOD_REST_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOOD_NAME));
                int price = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FOOD_PRICE));
                int image = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FOOD_IMAGE));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOOD_DESC));
                boolean available = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FOOD_AVAILABLE)) == 1;

                Food food = new Food(id, restId, name, image, price, desc, available);
                food.setOptions(getOptionsForFood(id));
                list.add(food);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public long addFood(Food food) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FOOD_REST_ID, food.getRestaurantId());
        cv.put(COL_FOOD_NAME, food.getName());
        cv.put(COL_FOOD_PRICE, food.getPrice());
        cv.put(COL_FOOD_IMAGE, food.getImage() != 0 ? food.getImage() : R.drawable.ic_seveneleven);
        cv.put(COL_FOOD_DESC, food.getDescription());
        cv.put(COL_FOOD_AVAILABLE, food.isAvailable() ? 1 : 0);
        return db.insert(TABLE_FOODS, null, cv);
    }

    public boolean updateFood(Food food) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FOOD_NAME, food.getName());
        cv.put(COL_FOOD_PRICE, food.getPrice());
        cv.put(COL_FOOD_DESC, food.getDescription());
        cv.put(COL_FOOD_AVAILABLE, food.isAvailable() ? 1 : 0);
        if (food.getRestaurantId() > 0) {
            cv.put(COL_FOOD_REST_ID, food.getRestaurantId());
        }
        int rows = db.update(TABLE_FOODS, cv, COL_FOOD_ID + "=?", new String[]{String.valueOf(food.getId())});
        return rows > 0;
    }

    public boolean toggleFoodAvailability(int foodId, boolean isAvailable) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FOOD_AVAILABLE, isAvailable ? 1 : 0);
        int rows = db.update(TABLE_FOODS, cv, COL_FOOD_ID + "=?", new String[]{String.valueOf(foodId)});
        return rows > 0;
    }

    public boolean deleteFood(int foodId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FOOD_OPTIONS, COL_OPT_FOOD_ID + "=?", new String[]{String.valueOf(foodId)});
        int rows = db.delete(TABLE_FOODS, COL_FOOD_ID + "=?", new String[]{String.valueOf(foodId)});
        return rows > 0;
    }

    // --- ORDER CRUD ---
    public long createOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        long orderId = -1;
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_ORDER_USER_ID, order.getUserId());
            cv.put(COL_ORDER_USER_EMAIL, order.getUserEmail());
            cv.put(COL_ORDER_TOTAL_PRICE, order.getTotalPrice());
            cv.put(COL_ORDER_DATE, order.getDate());
            cv.put(COL_ORDER_STATUS, order.getStatus());

            orderId = db.insert(TABLE_ORDERS, null, cv);

            if (orderId != -1 && order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    ContentValues itemCv = new ContentValues();
                    itemCv.put(COL_ITEM_ORDER_ID, orderId);
                    itemCv.put(COL_ITEM_FOOD_ID, item.getFoodId());
                    itemCv.put(COL_ITEM_FOOD_NAME, item.getFoodName());
                    itemCv.put(COL_ITEM_PRICE, item.getPrice());
                    itemCv.put(COL_ITEM_QUANTITY, item.getQuantity());
                    db.insert(TABLE_ORDER_ITEMS, null, itemCv);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return orderId;
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null,
                COL_ORDER_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, COL_ORDER_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_ID));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_USER_EMAIL));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_TOTAL_PRICE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_DATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_STATUS));
                int sId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_SHIPPER_ID));
                String sName = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_SHIPPER_NAME));

                Order order = new Order(id, userId, email, total, date, status, sId, sName);
                order.setItems(getOrderItems(id));
                list.add(order);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null, null, null, null, null, COL_ORDER_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_ID));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_USER_ID));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_USER_EMAIL));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_TOTAL_PRICE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_DATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_STATUS));
                int sId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_SHIPPER_ID));
                String sName = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_SHIPPER_NAME));

                Order order = new Order(id, userId, email, total, date, status, sId, sName);
                order.setItems(getOrderItems(id));
                list.add(order);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    private List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDER_ITEMS, null,
                COL_ITEM_ORDER_ID + "=?",
                new String[]{String.valueOf(orderId)}, null, null, COL_ITEM_ID + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_ID));
                int foodId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_FOOD_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_FOOD_NAME));
                int price = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_PRICE));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_QUANTITY));
                items.add(new OrderItem(id, orderId, foodId, name, price, qty));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_STATUS, status);
        int rows = db.update(TABLE_ORDERS, cv, COL_ORDER_ID + "=?", new String[]{String.valueOf(orderId)});
        return rows > 0;
    }

    public boolean deleteOrder(int orderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ORDER_ITEMS, COL_ITEM_ORDER_ID + "=?", new String[]{String.valueOf(orderId)});
        int rows = db.delete(TABLE_ORDERS, COL_ORDER_ID + "=?", new String[]{String.valueOf(orderId)});
        return rows > 0;
    }

    // --- SHIPPER ORDER METHODS ---
    public boolean assignOrderToShipper(int orderId, int shipperId, String shipperName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_SHIPPER_ID, shipperId);
        cv.put(COL_ORDER_SHIPPER_NAME, shipperName);
        cv.put(COL_ORDER_STATUS, "Đang giao");
        int rows = db.update(TABLE_ORDERS, cv, COL_ORDER_ID + "=?", new String[]{String.valueOf(orderId)});
        return rows > 0;
    }

    public List<Order> getAvailableOrdersForShipper() {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null,
                COL_ORDER_STATUS + "=? OR " + COL_ORDER_STATUS + "=?",
                new String[]{"Chờ xác nhận", "Pending"}, null, null, COL_ORDER_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_ID));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_USER_ID));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_USER_EMAIL));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_TOTAL_PRICE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_DATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_STATUS));

                Order order = new Order(id, userId, email, total, date, status);
                order.setItems(getOrderItems(id));
                list.add(order);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Order> getShipperActiveOrders(int shipperId) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null,
                COL_ORDER_SHIPPER_ID + "=? AND (" + COL_ORDER_STATUS + "=? OR " + COL_ORDER_STATUS + "=?)",
                new String[]{String.valueOf(shipperId), "Đang giao", "Delivering"}, null, null, COL_ORDER_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_ID));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_USER_ID));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_USER_EMAIL));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_TOTAL_PRICE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_DATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_STATUS));
                String sName = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_SHIPPER_NAME));

                Order order = new Order(id, userId, email, total, date, status, shipperId, sName);
                order.setItems(getOrderItems(id));
                list.add(order);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Order> getShipperHistoryOrders(int shipperId) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null,
                COL_ORDER_SHIPPER_ID + "=?",
                new String[]{String.valueOf(shipperId)}, null, null, COL_ORDER_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_ID));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_USER_ID));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_USER_EMAIL));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_TOTAL_PRICE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_DATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_STATUS));
                String sName = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_SHIPPER_NAME));

                Order order = new Order(id, userId, email, total, date, status, shipperId, sName);
                order.setItems(getOrderItems(id));
                list.add(order);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // --- STATISTICS METHODS FOR ADMIN DASHBOARD ---
    public int getTotalRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_ORDER_TOTAL_PRICE + ") FROM " + TABLE_ORDERS + " WHERE " + COL_ORDER_STATUS + " != 'Cancelled' AND " + COL_ORDER_STATUS + " != 'Đã hủy'", null);
        int total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getInt(0);
            cursor.close();
        }
        return total;
    }

    public int getTotalUsersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getTotalFoodsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FOODS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getTotalOrdersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public List<FoodSaleStat> getFoodSalesStatistics() {
        List<FoodSaleStat> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_ITEM_FOOD_NAME + ", SUM(" + COL_ITEM_QUANTITY + "), SUM(" + COL_ITEM_PRICE + " * " + COL_ITEM_QUANTITY + ") " +
                "FROM " + TABLE_ORDER_ITEMS + " GROUP BY " + COL_ITEM_FOOD_NAME + " ORDER BY SUM(" + COL_ITEM_QUANTITY + ") DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                int qty = cursor.getInt(1);
                int revenue = cursor.getInt(2);
                list.add(new FoodSaleStat(name, qty, revenue));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }
}
