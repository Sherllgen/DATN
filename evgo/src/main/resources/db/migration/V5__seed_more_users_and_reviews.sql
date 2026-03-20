-- 1. Create 5 more User accounts
INSERT INTO "users" (id, email, password, full_name, status, email_verified, phone_verified, auth_provider, created_at, updated_at, avatar_url) VALUES 
(11, 'user1@example.com', '$2a$10$iZAxrdMs0Z1998XEddFEi.l0ghFUAn.mRZC9eUyYwb8u4.nY0Ik.q', 'Nguyen Van A', 'ACTIVE', true, true, 'LOCAL', NOW(), NOW(), 'https://ui-avatars.com/api/?name=Nguyen+Van+A'),
(12, 'user2@example.com', '$2a$10$iZAxrdMs0Z1998XEddFEi.l0ghFUAn.mRZC9eUyYwb8u4.nY0Ik.q', 'Tran Thi B', 'ACTIVE', true, true, 'LOCAL', NOW(), NOW(), 'https://ui-avatars.com/api/?name=Tran+Thi+B'),
(13, 'user3@example.com', '$2a$10$iZAxrdMs0Z1998XEddFEi.l0ghFUAn.mRZC9eUyYwb8u4.nY0Ik.q', 'Le Van C', 'ACTIVE', true, true, 'LOCAL', NOW(), NOW(), 'https://ui-avatars.com/api/?name=Le+Van+C'),
(14, 'user4@example.com', '$2a$10$iZAxrdMs0Z1998XEddFEi.l0ghFUAn.mRZC9eUyYwb8u4.nY0Ik.q', 'Pham Thi D', 'ACTIVE', true, true, 'LOCAL', NOW(), NOW(), 'https://ui-avatars.com/api/?name=Pham+Thi+D'),
(15, 'user5@example.com', '$2a$10$iZAxrdMs0Z1998XEddFEi.l0ghFUAn.mRZC9eUyYwb8u4.nY0Ik.q', 'Hoang Van E', 'ACTIVE', true, true, 'LOCAL', NOW(), NOW(), 'https://ui-avatars.com/api/?name=Hoang+Van+E')
ON CONFLICT (email) DO NOTHING;

-- 2. Assign USER role (role_id = 1)
INSERT INTO user_roles (user_id, role_id) VALUES
(11, 1), (12, 1), (13, 1), (14, 1), (15, 1)
ON CONFLICT DO NOTHING;

-- Update users sequence
SELECT setval('users_id_seq', (SELECT MAX(id) FROM "users"));

-- 3. Seed reviews for stations (0-5 per station)
INSERT INTO "reviews" (station_id, user_id, rating, comment, created_at) VALUES
-- Station 1: EV-Go Bitexco Central
(1, 11, 5, 'Great charging station, right in the center.', NOW()),
(1, 12, 4, 'A bit crowded during peak hours but charges fast.', NOW()),
(1, 13, 5, 'Excellent service.', NOW()),
(1, 14, 4, 'Parking space is a bit tight.', NOW()),
(1, 15, 5, 'Excellent service!', NOW()),

-- Station 2: Independence Palace
(2, 4, 4, 'Beautiful view, stable charging.', NOW()),
(2, 11, 5, 'Very convenient when visiting the palace.', NOW()),
(2, 12, 3, 'Price is a bit higher than other places.', NOW()),

-- Station 4: KTX Khu B
(4, 13, 4, 'Convenient for students.', NOW()),
(4, 14, 2, 'Frequent connection issues with the app.', NOW()),

-- Station 5: Suoi Tien
(5, 15, 5, 'Very fast and powerful.', NOW()),
(5, 11, 4, 'Spacious area.', NOW()),
(5, 12, 5, 'Will come back.', NOW()),
(5, 4, 3, 'A bit far from the center.', NOW()),

-- Station 6: Landmark 81
(6, 11, 5, 'Landmark class, extremely fast.', NOW()),
(6, 12, 5, '100kW charging is top-notch.', NOW()),
(6, 13, 5, 'Luxurious parking basement.', NOW()),
(6, 14, 4, 'Parking fee is a bit high.', NOW()),
(6, 15, 5, 'Top 1 in HCM City.', NOW()),

-- Station 7: Aeon Mall Tan Phu
(7, 4, 4, 'Very convenient to charge while shopping at the supermarket.', NOW()),
(7, 11, 3, 'Often out of space.', NOW()),

-- Station 8: Giga Mall
(8, 12, 4, 'Stable charging.', NOW()),

-- Station 9: Saigon Centre
(9, 13, 5, 'Prime location in District 1.', NOW()),
(9, 14, 5, 'Parking staff are very helpful.', NOW()),

-- Station 10: Vincom Dong Khoi
(10, 15, 2, 'This area is prone to traffic jams.', NOW()),
(10, 4, 4, 'Normal charging.', NOW()),

-- Station 11: SC VivoCity
(11, 11, 5, 'Large mall, fast charging.', NOW()),

-- Station 13: Pearl Plaza
(13, 12, 4, 'Good.', NOW()),

-- Station 14: Masteri Thao Dien
(14, 13, 5, 'Very convenient for residents.', NOW()),
(14, 14, 4, 'Clean.', NOW()),

-- Station 15: Lotte Mart
(15, 15, 3, 'A bit old but still works well.', NOW()),

-- Station 17: Van Hanh Mall
(17, 4, 5, 'Many charging stations, no long wait.', NOW()),
(17, 11, 4, 'Easy to find.', NOW()),

-- Station 19: Celadon City
(19, 12, 5, 'Fresh and airy.', NOW()),

-- Station 20: Sala City
(20, 13, 5, 'New urban area, very smooth charging.', NOW()),
(20, 14, 5, 'Station design is very modern.', NOW())
ON CONFLICT DO NOTHING;

-- Update reviews sequence
SELECT setval('reviews_id_seq', (SELECT MAX(id) FROM "reviews"));
