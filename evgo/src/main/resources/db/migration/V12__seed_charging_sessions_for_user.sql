-- Seed charging sessions, invoices, and transactions for user@gmail.com (user_id = 4)

-- 1. Insert Invoices (status = 'PAID', purpose = 'CHARGING_SESSION')
-- We use ids starting from 201
INSERT INTO invoices (id, user_id, charging_session_id, number, total_cost, purpose, payment_method, status, created_at, updated_at) VALUES
(201, 4, 201, 'INV-CS-201', 39375.00, 'CHARGING_SESSION', 'MOMO', 'PAID', '2026-05-10 10:00:00', '2026-05-10 11:00:00'),
(202, 4, 202, 'INV-CS-202', 57330.00, 'CHARGING_SESSION', 'ZALOPAY', 'PAID', '2026-05-12 14:00:00', '2026-05-12 15:30:00'),
(203, 4, 203, 'INV-CS-203', 70560.00, 'CHARGING_SESSION', 'BANK_TRANSFER', 'PAID', '2026-05-15 08:30:00', '2026-05-15 10:00:00')
ON CONFLICT (id) DO NOTHING;

-- 2. Insert Charging Sessions (status = 'COMPLETED')
-- port_id = 1 belongs to charger 1, station 1
-- total_kwh calculations:
-- 201: 12.5 kwh * 3150.00 (rate) = 39375.00
-- 202: 18.2 kwh * 3150.00 (rate) = 57330.00
-- 203: 22.4 kwh * 3150.00 (rate) = 70560.00
INSERT INTO charging_sessions (id, user_id, port_id, invoice_id, total_kwh, start_time, end_time, status, created_at, updated_at, meter_start, transaction_id) VALUES
(201, 4, 1, 201, 12.5000, '2026-05-10 10:00:00', '2026-05-10 11:00:00', 'COMPLETED', '2026-05-10 10:00:00', '2026-05-10 11:00:00', 1000, 201),
(202, 4, 1, 202, 18.2000, '2026-05-12 14:00:00', '2026-05-12 15:30:00', 'COMPLETED', '2026-05-12 14:00:00', '2026-05-12 15:30:00', 1150, 202),
(203, 4, 1, 203, 22.4000, '2026-05-15 08:30:00', '2026-05-15 10:00:00', 'COMPLETED', '2026-05-15 08:30:00', '2026-05-15 10:00:00', 1300, 203)
ON CONFLICT (id) DO NOTHING;

-- 3. Insert Transactions (status = 'SUCCESS')
INSERT INTO transactions (id, invoice_id, amount, payment_method, status, app_trans_id, created_at, updated_at) VALUES
(201, 201, 39375.00, 'MOMO', 'SUCCESS', 'trans-cs-201', '2026-05-10 11:00:00', '2026-05-10 11:00:00'),
(202, 202, 57330.00, 'ZALOPAY', 'SUCCESS', 'trans-cs-202', '2026-05-12 15:30:00', '2026-05-12 15:30:00'),
(203, 203, 70560.00, 'BANK_TRANSFER', 'SUCCESS', 'trans-cs-203', '2026-05-15 10:00:00', '2026-05-15 10:00:00')
ON CONFLICT (id) DO NOTHING;

-- Update sequences to avoid future auto-increment ID collisions
SELECT setval('invoices_id_seq', GREATEST((SELECT MAX(id) FROM invoices), 203));
SELECT setval('charging_sessions_id_seq', GREATEST((SELECT MAX(id) FROM charging_sessions), 203));
SELECT setval('transactions_id_seq', GREATEST((SELECT MAX(id) FROM transactions), 203));
