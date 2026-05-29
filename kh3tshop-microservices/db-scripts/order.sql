-- order.sql
-- Data for order service database (cart, orders, invoices, wishlists)
-- Single account: hinh123 (login_id=1)

-- cart
INSERT INTO cart (
    cart_id, customer_login, total_quantity, total_amount, created_at, updated_at
) VALUES
    (1, 1, 4, 2295000, '2025-06-05 10:15:22', '2025-11-10 09:12:41');


-- cart_detail
INSERT INTO cart_detail (
    cart_detail_id,
    cart_id,
    product_id,
    size_detail_id,
    quantity,
    price_at_time,
    subtotal,
    is_selected,
    create_at,
    update_at
) VALUES
    (1, 1, 1,  1, 2, 297500,  595000, 1, '2025-06-05', '2025-11-10'),
    (2, 1, 2,  6, 2, 850000, 1700000, 1, '2025-06-12', '2025-11-10');


-- customer_trading
INSERT INTO customer_trading (
    trading_id,
    receiver_name,
    receiver_phone,
    receiver_email,
    receiver_address,
    total_amount,
    trading_date,
    created_at,
    updated_at
) VALUES
    (1, 'Pham Van Hinh', '0900000000', 'phamvanhinhstt8@gmail.com',
     '01 Đường Nguyễn Văn Linh, Quận 7, TP. Hồ Chí Minh',
     2295000, '2025-11-10 09:00:00', '2025-11-10 08:50:00', '2025-11-10 08:50:00');


-- Orders
INSERT INTO orders (
    order_id,
    order_code,
    order_date,
    status_ordering,
    note,
    customer_trading_id,
    account_id,
    payment_method
) VALUES
    (1, 'ORD20251110001', '2025-11-10 09:00:00', 'PENDING', 'Giao giờ hành chính', 1, 1, 'CASH');


-- Order Details
INSERT INTO order_detail (
    order_detail_id,
    order_id,
    product_id,
    product_name,
    quantity,
    unit_price,
    total_price,
    created_at,
    updated_at
) VALUES
    (1, 1, 1, 'Triple Star Small Wallet',       2, 297500,  595000, '2025-06-10 09:15:33', '2025-06-10 09:15:33'),
    (2, 1, 2, 'Raw Denim Stitch Baggy Jeans',   2, 850000, 1700000, '2025-06-10 09:16:10', '2025-06-10 09:16:10');


-- invoice
INSERT INTO invoice (
    invoice_id,
    order_id,
    invoice_code,
    subtotal_amount,
    tax_amount,
    total_amount,
    payment_method,
    payment_status,
    created_at,
    updated_at
) VALUES
    (1, 1, 'INV-20250610-001', 2295000, 0, 2295000, 'CASH', 'PAID', '2025-06-10 09:20:15', '2025-06-10 09:20:15');


-- wishlist
INSERT INTO wishlist (
    wishlist_id,
    name,
    description,
    created_at,
    updated_at,
    customer_login
) VALUES
    (1, 'Wishlist Hinh', 'Các sản phẩm yêu thích của Hinh', '2025-11-10 08:00:00', '2025-11-10 08:00:00', 1);

-- wishlist_detail
INSERT INTO wishlist_detail (
    wishlist_detail_id,
    note,
    created_at,
    wishlist_id,
    product_id
) VALUES
    (1, 'Muốn mua sớm',            '2025-11-10 08:05:00', 1, 1),
    (2, 'Xem xét màu sắc khác',    '2025-11-10 08:06:00', 1, 3);
