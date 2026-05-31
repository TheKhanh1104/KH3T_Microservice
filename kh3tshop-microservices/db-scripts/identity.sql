-- identity.sql
-- Data for identity service database (users, accounts, addresses)
-- Seed accounts:
-- phamvanhinhstt8@gmail.com / hinh123 (USER)
-- staff.kh3tshop@gmail.com / staff123 (STAFF)

-- Ensure schema is compatible with current Account entity mapping.
SET @db_name = DATABASE();

SET @stmt = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db_name
              AND TABLE_NAME = 'account'
              AND COLUMN_NAME = 'otp_code'
        ),
        'SELECT 1',
        'ALTER TABLE account ADD COLUMN otp_code VARCHAR(255) NULL'
    )
);
PREPARE s1 FROM @stmt;
EXECUTE s1;
DEALLOCATE PREPARE s1;

SET @stmt = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db_name
              AND TABLE_NAME = 'account'
              AND COLUMN_NAME = 'otp_expiry'
        ),
        'SELECT 1',
        'ALTER TABLE account ADD COLUMN otp_expiry DATETIME NULL'
    )
);
PREPARE s2 FROM @stmt;
EXECUTE s2;
DEALLOCATE PREPARE s2;

INSERT INTO customer (customer_id, full_name, phone_number, email, gender, date_of_birth, create_at, update_at, status)
VALUES
    (1, 'Pham Van Hinh', '0900000000', 'phamvanhinhstt8@gmail.com', 'MALE', '2004-01-01', '2025-01-01', '2025-01-01', 'ACTIVE');


INSERT INTO account (login_id, create_at, password, role, status_login, update_at, username, customer_id)
VALUES
    (1, '2025-01-01', '$2b$10$hrgG.lh8Ym7ssRME.qAYUOSK9cWIME3Dc1Lx8a1O1NDyeAc/CfEQS', 'USER', 'ACTIVE', '2025-01-01', 'hinh123', 1);


INSERT INTO address (province, delivery_address, delivery_note, account_id) VALUES
    ('TP. Hồ Chí Minh', '01 Đường Nguyễn Văn Linh, Quận 7', 'Giao giờ hành chính', 1);


INSERT INTO customer (customer_id, full_name, phone_number, email, gender, date_of_birth, create_at, update_at, status)
VALUES
    (2, 'Pham Van Staff', '0911111111', 'staff.kh3tshop@gmail.com', 'MALE', '2000-01-01', '2025-01-01', '2025-01-01', 'ACTIVE');


INSERT INTO account (login_id, create_at, password, role, status_login, update_at, username, customer_id)
VALUES
    (2, '2025-01-01', '$2b$10$hrgG.lh8Ym7ssRME.qAYUOSK9cWIME3Dc1Lx8a1O1NDyeAc/CfEQS', 'STAFF', 'ACTIVE', '2025-01-01', 'staff123', 2);


INSERT INTO address (province, delivery_address, delivery_note, account_id) VALUES
    ('TP. Hồ Chí Minh', '01 Đường Nguyễn Văn Linh, Quận 7', 'Tài khoản nhân viên', 2);
