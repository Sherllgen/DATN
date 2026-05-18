-- B1: Add OCPP Basic Authentication password column to chargers.
-- All existing chargers are seeded with the shared password 'evgo123' (BCrypt encoded).
-- The same password is used by the simulator's basicAuthSettings for all ChargePointIDs.

ALTER TABLE chargers
    ADD COLUMN IF NOT EXISTS ocpp_password VARCHAR(255);

-- BCrypt hash of 'evgo123' (cost factor 10)
UPDATE chargers
SET ocpp_password = '$2a$10$Jg.o52009GpaYCMBdvfiTeTEeu/fGTQZMCCeYXceN/S7bWWZZlViu'
WHERE ocpp_password IS NULL;
