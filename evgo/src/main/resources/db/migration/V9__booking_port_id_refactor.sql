-- Migration: Replace port_number with port_id in bookings table
-- Rationale: portNumber is an OCPP hardware concept (connector index on a charger).
-- The booking module should reference the globally-unique port PK (port_id) instead.

-- 1. Add port_id column (nullable initially for data population)
ALTER TABLE public.bookings ADD COLUMN port_id bigint;

-- 2. Populate port_id from existing charger_id + port_number
UPDATE public.bookings b
SET port_id = (
    SELECT p.id FROM public.ports p
    WHERE p.charger_id = b.charger_id AND p.port_number = b.port_number
);

-- 3. Make port_id NOT NULL after population
ALTER TABLE public.bookings ALTER COLUMN port_id SET NOT NULL;

-- 4. Add foreign key constraint
ALTER TABLE public.bookings
    ADD CONSTRAINT fk_bookings_port FOREIGN KEY (port_id) REFERENCES public.ports(id);

-- 5. Drop the old port_number column
ALTER TABLE public.bookings DROP COLUMN port_number;

-- 6. Add index for port_id lookups (overlap checks, availability)
CREATE INDEX IF NOT EXISTS idx_bookings_port_id ON public.bookings(port_id);
