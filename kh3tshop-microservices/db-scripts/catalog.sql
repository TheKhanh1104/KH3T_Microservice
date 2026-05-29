-- catalog.sql
-- Data for catalog service database (categories, products, sizes)
-- No account-specific data; unchanged from original.

-- categories
INSERT INTO category
(category_name, description, image_url, display_order, is_active, created_at, updated_at)
VALUES
    ('Top', 'Các loại áo như áo thun, sơ mi, hoodie...', 'https://i.postimg.cc/LXQSc1jQ/Tops-Size-Chart.png', 1, true, '2025-11-10', '2025-11-10'),
    ('Bottom', 'Các loại quần như jeans, trousers, shorts...', 'https://i.postimg.cc/HsDMRc35/Bottoms-Size-Chart.png', 2, true, '2025-11-10', '2025-11-10'),
    ('Accessories', 'Các loại phụ kiện như ví, mũ, thắt lưng...', 'https://i.postimg.cc/T3QWKkx7/accessires-Sizechart.png', 3, true, '2025-11-10', '2025-11-10');


-- products
INSERT INTO product
(product_name, description, price, cost_price, unit, quantity, image_url_front, image_url_back, created_at, updated_at, brand, rating, category, discount_amount, form, material, status)
VALUES
-- 01/11/2025 - Out of stock + 15% off
('Triple Star Small Wallet', 'Compact leather wallet featuring the signature Triple Star logo, perfect for everyday essentials with multiple card slots and a sleek design.',
 350000, 297500, 'piece', 0,
 'https://content.pancake.vn/1/s2360x2950/35/d5/16/04/8a7e15b89251d0132ab9ba5025dbd2f35afaf47ccc47a1bd14541f02-w:2400-h:3000-l:871502-t:image/jpeg.jpeg',
 'https://content.pancake.vn/1/s2360x2950/06/9c/80/1c/fbbe27ea27340bd2cb3467e91d49a6e0e37f33b6651c1a1422ecf38a-w:2400-h:3000-l:703943-t:image/jpeg.jpeg',
 '2025-11-01', '2025-11-01', 'KH3T', 4.5, 3, 15, NULL, 'Leather', 'ACTIVE'),

-- 02/11/2025 - không giảm giá
('Raw Denim Stitch Baggy Jeans', 'Baggy-fit jeans crafted from premium raw denim with bold contrast stitching along the seams, offering a rugged streetwear vibe and lasting durability.',
 850000, 850000, 'piece', 150,
 'https://content.pancake.vn/1/s2360x2950/88/d3/98/05/f32daa82a82f8cf47c9256f5303cc907852f6f2ad97b0d84cc1e7464-w:2400-h:3000-l:875966-t:image/jpeg.jpeg',
 'https://content.pancake.vn/1/s2360x2950/21/08/9e/78/4ead1df425b3f0caa19594fc6fd40a9418d15d6aefbf95f7f5e85633-w:2400-h:3000-l:906251-t:image/jpeg.jpeg',
 '2025-11-02', '2025-11-02', 'KH3T', 4.2, 2, 0, 'Baggy Fit', 'Denim Fabric', 'ACTIVE'),

-- 08/11/2025 - không giảm giá
('Washed Jorts', 'Relaxed wide-leg denim shorts with a stonewashed finish, delivering a worn-in feel and laid-back style for summer adventures.',
 580000, 580000, 'piece', 130,
 'https://content.pancake.vn/1/s2360x2950/7d/cb/77/12/3658a18f95a81bbeae63064b0a133ec1055bc1ad173e84f5e9c984a1-w:2400-h:3000-l:942162-t:image/jpeg.jpeg',
 'https://content.pancake.vn/1/s2360x2950/68/db/26/51/ffaeccd1e4548ea3f0f1f830d0f213d0e3c86bcd18aac3cc9cc347b4-w:2400-h:3000-l:949630-t:image/jpeg.jpeg',
 '2025-11-08', '2025-11-08', 'KH3T', 4.2, 2, 0, 'Wide Leg', 'Washed Denim Fabric', 'ACTIVE'),

-- 09/11/2025 - 30% off
('Hello Kitty | Monogram Laser Baggy Jeans/ Blue', 'Playful baggy jeans featuring laser-etched Hello Kitty monogram patterns on blue denim, blending cute nostalgia with modern street fashion.',
 890000, 623000, 'piece', 160,
 'https://content.pancake.vn/1/s2360x2950/2d/0d/4f/5b/b7f983e37623a32d63d7fe5cbd507f6d829dc81600d6915d71e80215-w:2400-h:3000-l:866495-t:image/jpeg.jpeg',
 'https://content.pancake.vn/1/s2360x2950/96/a7/4f/07/f07f02a110fb8870cf5fda26b57eacb2be37f28895c25ba31ec28ebc-w:2400-h:3000-l:871816-t:image/jpeg.jpeg',
 '2025-11-09', '2025-11-09', 'KH3T', 4.6, 2, 30, 'Baggy Fit', 'Denim Fabric', 'ACTIVE');

-- (remaining product rows should be appended here from original source)


INSERT INTO size (name_size) VALUES
    ('S'),
    ('M'),
    ('L'),
    ('XL');


-- size_detail
-- Triple Star Small Wallet (id=1)
INSERT INTO size_detail (product_id, size_id, quantity) VALUES
    (1, 1, 0),
    (1, 2, 0),
    (1, 3, 0),
    (1, 4, 0);

-- Raw Denim Stitch Baggy Jeans (id=2)
INSERT INTO size_detail (product_id, size_id, quantity) VALUES
    (2, 1, 37),
    (2, 2, 37),
    (2, 3, 38),
    (2, 4, 38);

-- (remaining size_detail rows should be appended here from original source)
