-- ============================================================
-- SQL SCHEMA & DATASEED FOR FOOD DELIVERY APPLICATION
-- Database: app_database.db
-- Target: SQLite 3.x
-- ============================================================

-- 1. TABLE USERS
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'user'
);

-- 2. TABLE RESTAURANTS
DROP TABLE IF EXISTS restaurants;
CREATE TABLE restaurants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    logo INTEGER NOT NULL,
    cover INTEGER NOT NULL,
    address TEXT NOT NULL,
    open_hours TEXT NOT NULL
);

-- 3. TABLE FOODS
DROP TABLE IF EXISTS foods;
CREATE TABLE foods (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    restaurant_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    price INTEGER NOT NULL,
    image INTEGER NOT NULL,
    description TEXT,
    is_available INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY(restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

-- 4. TABLE ORDERS
DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    user_email TEXT NOT NULL,
    total_price INTEGER NOT NULL,
    date TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'Pending',
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. TABLE ORDER ITEMS
DROP TABLE IF EXISTS order_items;
CREATE TABLE order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    food_id INTEGER NOT NULL,
    food_name TEXT NOT NULL,
    price INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- ============================================================
-- INITIAL SEED DATA
-- ============================================================

-- Seed Users (1 Admin, 1 Main User, 4 Fake Customers)
INSERT INTO users (id, email, password, full_name, role) VALUES
(1, 'admin@app.com', 'admin123', 'System Administrator', 'admin'),
(2, 'user@app.com', 'user123', 'John Doe', 'user'),
(3, 'alice@app.com', 'alice123', 'Alice Nguyễn', 'user'),
(4, 'bob@app.com', 'bob123', 'Bob Trần', 'user'),
(5, 'charlie@app.com', 'charlie123', 'Charlie Lê', 'user'),
(6, 'david@app.com', 'david123', 'David Phạm', 'user');

-- Seed Restaurants
INSERT INTO restaurants (id, name, logo, cover, address, open_hours) VALUES
(1, '7 Eleven', 2131165318, 2131165272, '82 Nguyễn Thị Minh Khai, P.6, Q.3', '06:00 - 23:00'),
(2, 'KFC Chicken', 2131165318, 2131165272, '123 Lê Lợi, P. Bến Thành, Q.1', '08:00 - 22:00'),
(3, 'Circle K', 2131165306, 2131165272, '45 Nguyễn Huệ, P. Bến Nghé, Q.1', '24/7 Mở cửa'),
(4, 'Highland Coffee', 2131165313, 2131165272, '200 Trần Hưng Đạo, Q.5', '07:00 - 23:00');

-- Seed Foods
INSERT INTO foods (id, restaurant_id, name, price, image, description, is_available) VALUES
-- 7 Eleven (id = 1)
(1, 1, 'Uranium Pizza', 120000, 2131165318, 'Bánh Pizza phô mai xốt đặc biệt thơm ngon, nướng giòn rụm.', 1),
(2, 1, 'Abyssal Burger', 55000, 2131165318, 'Burger bò nướng đậm vị kết hợp phô mai xát lát hảo hạng.', 1),
(3, 1, 'Nuclear Coke', 35000, 2131165318, 'Nước ngọt có ga mát lạnh sảng khoái đập tan cơn khát.', 1),
(4, 1, 'Noddle of the Damned', 65000, 2131165318, 'Mì xào hải sản cay nồng đậm đà chuẩn vị Á Đông.', 1),
(5, 1, 'Bánh Mì Trứng', 25000, 2131165303, 'Bánh mì giòn kẹp trứng ốp la nóng hổi kèm dưa leo rau thơm.', 1),
(6, 1, 'Trà Sữa Thái', 30000, 2131165316, 'Trà sữa Thái xanh đậm vị thơm béo kèm trân châu giòn.', 1),

-- KFC Chicken (id = 2)
(7, 2, 'Fried Chicken Combo', 89000, 2131165288, 'Combo 2 miếng gà rán giòn rụm + 1 khoai tây chiên + 1 nước ngọt.', 1),
(8, 2, 'Zinger Burger', 59000, 2131165289, 'Burger gà giòn cay đặc trưng xốt mayonnaise đậm vị.', 1),
(9, 2, 'French Fries Large', 39000, 2131165288, 'Khoai tây chiên giòn tan cỡ lớn châm xốt cà chua.', 1),
(10, 2, 'Pepsi Can', 20000, 2131165304, 'Lon Pepsi mát lạnh sảng khoái.', 1),

-- Circle K (id = 3)
(11, 3, 'Hot Dog Supreme', 29000, 2131165306, 'Bánh hot dog kẹp xúc xích Đức nướng xốt mù tạt.', 1),
(12, 3, 'Milk Tea Boba', 32000, 2131165316, 'Trà sữa truyền thống ngọt thanh trân châu đen dai dẻo.', 1),
(13, 3, 'Instant Ramen Egg', 22000, 2131165289, 'Mì ly nóng hổi chèn trứng lòng đào thơm ngon.', 0),

-- Highland Coffee (id = 4)
(14, 4, 'Phin Sữa Đá', 39000, 2131165304, 'Cà phê phin truyền thống đậm đà hòa quyện sữa đặc ngọt ngào.', 1),
(15, 4, 'Trà Sen Vàng', 49000, 2131165313, 'Trà oolong thanh mát kết hợp hạt sen thơm bùi và củ năng.', 1),
(16, 4, 'Freeze Trà Xanh', 55000, 2131165313, 'Trà xanh đá xay mát lạnh phủ kem tươi ngọt béo.', 1);

-- Seed Sample Orders (for revenue & statistics demonstration)
INSERT INTO orders (id, user_id, user_email, total_price, date, status) VALUES
(1001, 3, 'alice@app.com', 210000, '2026-10-01 11:30', 'Completed'),
(1002, 4, 'bob@app.com', 128000, '2026-10-02 12:15', 'Completed'),
(1003, 5, 'charlie@app.com', 88000, '2026-10-03 18:45', 'Completed'),
(1004, 6, 'david@app.com', 155000, '2026-10-04 19:20', 'Processing'),
(1005, 2, 'user@app.com', 95000, '2026-10-05 09:10', 'Pending');

-- Seed Sample Order Items
INSERT INTO order_items (id, order_id, food_id, food_name, price, quantity) VALUES
(1, 1001, 1, 'Uranium Pizza', 120000, 1),
(2, 1001, 3, 'Nuclear Coke', 35000, 2),
(3, 1001, 5, 'Bánh Mì Trứng', 20000, 1),
(4, 1002, 7, 'Fried Chicken Combo', 89000, 1),
(5, 1002, 9, 'French Fries Large', 39000, 1),
(6, 1003, 15, 'Trà Sen Vàng', 49000, 1),
(7, 1003, 14, 'Phin Sữa Đá', 39000, 1),
(8, 1004, 2, 'Abyssal Burger', 55000, 2),
(9, 1004, 16, 'Freeze Trà Xanh', 45000, 1),
(10, 1005, 6, 'Trà Sữa Thái', 30000, 2),
(11, 1005, 11, 'Hot Dog Supreme', 35000, 1);
