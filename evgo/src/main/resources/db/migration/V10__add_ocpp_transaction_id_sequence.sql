-- Create a PostgreSQL sequence for OCPP transaction IDs.
-- This replaces the in-memory AtomicInteger counter that reset on every server restart,
-- causing duplicate transactionId values across charging sessions.
--
-- Start at 10000 to avoid collisions with any existing data from the old counter.
CREATE SEQUENCE IF NOT EXISTS ocpp_transaction_id_seq START WITH 10000 INCREMENT BY 1;
