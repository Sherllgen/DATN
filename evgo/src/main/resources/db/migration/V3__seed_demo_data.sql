-- =========================================================================
-- Seed Data script replacing StationDataInitializer and InvoiceDataInitializer
-- Note: Replace table names (e.g. `station`, `charger`, `users`, `invoice`) 
-- and column names with the exact generated names from your JPA/Hibernate schema.
-- =========================================================================

-- Stations
INSERT INTO stations (id, owner_id, name, description, address, latitude, longitude, rate, status, is_flagged_low_quality)
VALUES 
(1001, (SELECT id FROM "users" WHERE email='station.owner@evgo.com'), 'EV-Go Bitexco Central', 'Premium charging station near financial district', '2 Hai Trieu, Ben Nghe, District 1, Ho Chi Minh City', 10.771918, 106.704439, 4.8, 'ACTIVE', false),
(1002, (SELECT id FROM "users" WHERE email='station.owner@evgo.com'), 'Independence Palace Station', 'Convenient parking and charging spot', '135 Nam Ky Khoi Nghia, Ben Thanh, District 1, Ho Chi Minh City', 10.776111, 106.695833, 4.5, 'ACTIVE', false),
(1003, (SELECT id FROM "users" WHERE email='station.owner@evgo.com'), 'Crescent Mall EV Hub', 'Spacious charging area in Crescent Mall basement', '101 Ton Dat Tien, Tan Phu, District 7, Ho Chi Minh City', 10.729150, 106.721400, 4.7, 'ACTIVE', false),
(1004, (SELECT id FROM "users" WHERE email='station.owner@evgo.com'), 'KTX Khu B Chargers', 'Open 24/7 charging station in Dormitory Zone B', 'Dong Hoa, Di An, Binh Duong', 10.853000, 106.788000, 4.2, 'ACTIVE', false),
(1005, (SELECT id FROM "users" WHERE email='station.owner@evgo.com'), 'Suoi Tien Theme Park', 'Fast chargers near the main gate', '120 Hanoi Highway, Tan Phu, Thu Duc', 10.865500, 106.801000, 4.6, 'ACTIVE', false);

-- Set location PostGIS points (assuming location column is geometry)
UPDATE stations SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326) WHERE id IN (1001, 1002, 1003, 1004, 1005);

-- Image URLs
INSERT INTO station_images (station_id, image_url) VALUES
(1001, 'https://futureev.net/wp-content/uploads/2023/08/future-ev-chariging-station-1024x576.webp'),
(1002, 'https://www.egat.co.th/home/en/wp-content/uploads/2021/12/02.jpg'),
(1003, 'https://www.egat.co.th/home/en/wp-content/uploads/2023/02/MRT-EV-04-1024x683.jpg'),
(1004, 'https://teklife.com.vn/wp-content/uploads/2025/12/tram-sac-xe-may-dien-04-1.jpg'),
(1005, 'https://5.imimg.com/data5/SELLER/Default/2024/4/412135759/PV/SY/UU/218378982/ev-charger-box-500x500.jpg');

-- Chargers
INSERT INTO chargers (id, station_id, name, max_power, connector_type, status) VALUES
(2001, 1001, 'Bitexco AC-01', 22.0, 'IEC_TYPE_2', 'AVAILABLE'),
(2002, 1001, 'Bitexco DC-Rapid', 150.0, 'VINFAST_STD', 'IN_USE'),
(2003, 1002, 'Palace AC-01', 11.0, 'IEC_TYPE_2', 'AVAILABLE'),
(2004, 1003, 'Crescent AC-01', 7.4, 'IEC_TYPE_2', 'AVAILABLE'),
(2005, 1003, 'Crescent DC-01', 50.0, 'VINFAST_STD', 'AVAILABLE'),
(2006, 1004, 'VNU AC-01', 11.0, 'IEC_TYPE_2', 'AVAILABLE'),
(2007, 1004, 'VNU DC-Rapid', 100.0, 'VINFAST_STD', 'AVAILABLE'),
(2008, 1005, 'KTX AC-01', 7.4, 'IEC_TYPE_2', 'AVAILABLE');

-- Ports
INSERT INTO ports (id, charger_id, port_number, status) VALUES
(4001, 2001, 1, 'AVAILABLE'),
(4002, 2002, 1, 'CHARGING'),
(4003, 2003, 1, 'AVAILABLE'),
(4004, 2004, 1, 'AVAILABLE'),
(4005, 2005, 1, 'AVAILABLE'),
(4006, 2006, 1, 'AVAILABLE'),
(4007, 2007, 1, 'AVAILABLE'),
(4008, 2008, 1, 'AVAILABLE');

-- Invoices
INSERT INTO invoices (id, "number", user_id, booking_id, charging_session_id, total_cost, purpose, status) VALUES
(3001, 'INV-2026-0001', (SELECT id FROM "users" WHERE email='user@gmail.com'), NULL, 1, 85000, 'CHARGING_SESSION', 'PENDING'),
(3002, 'INV-2026-0002', (SELECT id FROM "users" WHERE email='user@gmail.com'), 1, NULL, 150000, 'BOOKING', 'PENDING'),
(3003, 'INV-2026-0003', (SELECT id FROM "users" WHERE email='user@gmail.com'), NULL, 2, 320000, 'CHARGING_SESSION', 'PENDING'),
(3004, 'INV-2026-0004', (SELECT id FROM "users" WHERE email='user@gmail.com'), 2, NULL, 200000, 'BOOKING', 'PAID'),
(3005, 'INV-2026-0005', (SELECT id FROM "users" WHERE email='user@gmail.com'), NULL, 3, 50000, 'CHARGING_SESSION', 'CANCELLED');
