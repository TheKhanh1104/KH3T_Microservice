-- identity.sql
-- Data for identity service database (users, accounts, addresses)
-- Single account: phamvanhinhstt8@gmail.com / hinh123

INSERT INTO customer (customer_id, full_name, phone_number, email, gender, date_of_birth, create_at, update_at, status)
VALUES
    (1, 'Pham Van Hinh', '0900000000', 'phamvanhinhstt8@gmail.com', 'MALE', '2004-01-01', '2025-01-01', '2025-01-01', 'ACTIVE');


INSERT INTO account (login_id, create_at, password, role, status_login, update_at, username, customer_id)
VALUES
    (1, '2025-01-01', '$2b$10$hrgG.lh8Ym7ssRME.qAYUOSK9cWIME3Dc1Lx8a1O1NDyeAc/CfEQS', 'USER', 'ACTIVE', '2025-01-01', 'hinh123', 1);


INSERT INTO address (province, delivery_address, delivery_note, account_id) VALUES
    ('TP. Hồ Chí Minh', '01 Đường Nguyễn Văn Linh, Quận 7', 'Giao giờ hành chính', 1);
