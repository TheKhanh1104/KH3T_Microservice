-- =========================================================================
-- SCRIPT CHUYỂN DỮ LIỆU TỪ MONOLITH (kh3t_shop) SANG MICROSERVICES
-- Viết dựa trên cấu trúc bảng thực tế (đã kiểm tra bằng SHOW COLUMNS)
-- Chạy trên HeidiSQL (root@localhost)
-- =========================================================================

-- =============================================
-- PHẦN 1: CATALOG SERVICE (kh3t_shop → kh3t_catalog)
-- =============================================

-- 1.1 Category (giống nhau hoàn toàn 8 cột)
INSERT IGNORE INTO kh3t_catalog.category (category_id, created_at, description, display_order, image_url, is_active, category_name, updated_at)
SELECT category_id, created_at, description, display_order, image_url, is_active, category_name, updated_at
FROM kh3t_shop.category;

-- 1.2 Product (giống nhau hoàn toàn 18 cột)
INSERT IGNORE INTO kh3t_catalog.product (product_id, brand, cost_price, created_at, description, discount_amount, form, image_url_back, image_url_front, material, product_name, price, quantity, rating, status, unit, updated_at, category)
SELECT product_id, brand, cost_price, created_at, description, discount_amount, form, image_url_back, image_url_front, material, product_name, price, quantity, rating, status, unit, updated_at, category
FROM kh3t_shop.product;

-- 1.3 Size (giống nhau hoàn toàn 2 cột)
INSERT IGNORE INTO kh3t_catalog.size (id, name_size)
SELECT id, name_size
FROM kh3t_shop.size;

-- 1.4 SizeDetail (giống nhau hoàn toàn 4 cột)
INSERT IGNORE INTO kh3t_catalog.size_detail (id, quantity, product_id, size_id)
SELECT id, quantity, product_id, size_id
FROM kh3t_shop.size_detail;


-- =============================================
-- PHẦN 2: IDENTITY SERVICE (kh3t_shop → kh3t_identity)
-- LƯU Ý: kh3t_shop.customer có thêm cột 'avatar' → bỏ qua
--         kh3t_shop.account có thêm 'otp','otp_expiry','session_token' → bỏ qua
-- =============================================

-- 2.1 Customer (source 10 cột → target 9 cột, bỏ 'avatar')
INSERT IGNORE INTO kh3t_identity.customer (customer_id, create_at, date_of_birth, email, full_name, gender, phone_number, status, update_at)
SELECT customer_id, create_at, date_of_birth, email, full_name, gender, phone_number, status, update_at
FROM kh3t_shop.customer;

-- 2.2 Account (source 11 cột → target 8 cột, bỏ 'otp','otp_expiry','session_token')
INSERT IGNORE INTO kh3t_identity.account (login_id, create_at, password, role, status_login, update_at, username, customer_id)
SELECT login_id, create_at, password, role, status_login, update_at, username, customer_id
FROM kh3t_shop.account;

-- 2.3 Address (giống nhau hoàn toàn 5 cột)
INSERT IGNORE INTO kh3t_identity.address (id, delivery_address, delivery_note, province, account_id)
SELECT id, delivery_address, delivery_note, province, account_id
FROM kh3t_shop.address;


-- =============================================
-- PHẦN 3: ORDER SERVICE (kh3t_shop → kh3t_order)
-- =============================================

-- 3.1 CustomerTrading (giống nhau hoàn toàn 9 cột)
INSERT IGNORE INTO kh3t_order.customer_trading (trading_id, created_at, receiver_address, receiver_email, receiver_name, receiver_phone, total_amount, trading_date, updated_at)
SELECT trading_id, created_at, receiver_address, receiver_email, receiver_name, receiver_phone, total_amount, trading_date, updated_at
FROM kh3t_shop.customer_trading;

-- 3.2 Cart (giống nhau 6 cột, thứ tự khác)
INSERT IGNORE INTO kh3t_order.cart (cart_id, customer_login, created_at, total_amount, total_quantity, updated_at)
SELECT cart_id, customer_login, created_at, total_amount, total_quantity, updated_at
FROM kh3t_shop.cart;

-- 3.3 CartDetail (giống nhau 10 cột, thứ tự khác)
INSERT IGNORE INTO kh3t_order.cart_detail (cart_detail_id, create_at, is_selected, price_at_time, product_id, quantity, size_detail_id, subtotal, update_at, cart_id)
SELECT cart_detail_id, create_at, is_selected, price_at_time, product_id, quantity, size_detail_id, subtotal, update_at, cart_id
FROM kh3t_shop.cart_detail;

-- 3.4 Orders (giống nhau 8 cột, thứ tự khác)
INSERT IGNORE INTO kh3t_order.orders (order_id, account_id, note, order_code, order_date, payment_method, status_ordering, customer_trading_id)
SELECT order_id, account_id, note, order_code, order_date, payment_method, status_ordering, customer_trading_id
FROM kh3t_shop.orders;

-- 3.5 OrderDetail (source 9 cột → target 9 cột, thứ tự khác)
INSERT IGNORE INTO kh3t_order.order_detail (order_detail_id, created_at, product_id, product_name, quantity, total_price, unit_price, updated_at, order_id)
SELECT order_detail_id, created_at, product_id, product_name, quantity, total_price, unit_price, updated_at, order_id
FROM kh3t_shop.order_detail;

-- 3.6 Invoice (giống nhau hoàn toàn 10 cột)
INSERT IGNORE INTO kh3t_order.invoice (invoice_id, created_at, invoice_code, payment_method, payment_status, subtotal_amount, tax_amount, total_amount, updated_at, order_id)
SELECT invoice_id, created_at, invoice_code, payment_method, payment_status, subtotal_amount, tax_amount, total_amount, updated_at, order_id
FROM kh3t_shop.invoice;

-- 3.7 WishList (giống nhau 6 cột, thứ tự khác)
INSERT IGNORE INTO kh3t_order.wishlist (wishlist_id, customer_login, created_at, description, name, updated_at)
SELECT wishlist_id, customer_login, created_at, description, name, updated_at
FROM kh3t_shop.wishlist;

-- 3.8 WishListDetail (giống nhau 5 cột)
INSERT IGNORE INTO kh3t_order.wishlist_detail (wishlist_detail_id, created_at, note, product_id, wishlist_id)
SELECT wishlist_detail_id, created_at, note, product_id, wishlist_id
FROM kh3t_shop.wishlist_detail;


-- =============================================
-- HOÀN TẤT - Kiểm tra kết quả
-- =============================================
SELECT 'CATALOG' AS service, 'category' AS tbl, COUNT(*) AS so_dong FROM kh3t_catalog.category
UNION ALL SELECT 'CATALOG', 'product', COUNT(*) FROM kh3t_catalog.product
UNION ALL SELECT 'CATALOG', 'size', COUNT(*) FROM kh3t_catalog.size
UNION ALL SELECT 'CATALOG', 'size_detail', COUNT(*) FROM kh3t_catalog.size_detail
UNION ALL SELECT 'IDENTITY', 'customer', COUNT(*) FROM kh3t_identity.customer
UNION ALL SELECT 'IDENTITY', 'account', COUNT(*) FROM kh3t_identity.account
UNION ALL SELECT 'IDENTITY', 'address', COUNT(*) FROM kh3t_identity.address
UNION ALL SELECT 'ORDER', 'cart', COUNT(*) FROM kh3t_order.cart
UNION ALL SELECT 'ORDER', 'orders', COUNT(*) FROM kh3t_order.orders
UNION ALL SELECT 'ORDER', 'invoice', COUNT(*) FROM kh3t_order.invoice
UNION ALL SELECT 'ORDER', 'wishlist', COUNT(*) FROM kh3t_order.wishlist;
