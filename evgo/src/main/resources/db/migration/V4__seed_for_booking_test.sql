-- Bookings (for E2E Testing)
INSERT INTO bookings (id, user_id, station_id, charger_id, port_number, start_time, end_time, status) VALUES
-- Booking 1: Job 2 (Pre-arrival Lock) sẽ quét ở phút thứ 2 (T-10m trước giờ bắt đầu)
(1, 1, 1, 1, 1, NOW() + INTERVAL '12 minutes', NOW() + INTERVAL '72 minutes', 'CONFIRMED'),
-- Booking 2: Job 3 (Soft Warning) quét ở phút thứ 1 (T-15m), Job 4 (Hard Cut-off) quét ở phút thứ 6 (T-10m)
(2, 1, 1, 1, 2, NOW() - INTERVAL '44 minutes', NOW() + INTERVAL '16 minutes', 'IN_PROGRESS');

-- Set sequence
SELECT setval('bookings_id_seq', (SELECT MAX(id) FROM bookings));