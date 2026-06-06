-- Increase serialized_event length to accommodate larger event payloads
ALTER TABLE public.event_publication 
ALTER COLUMN serialized_event TYPE TEXT;

-- Also increase listener_id length just in case, as fully qualified names can be long
ALTER TABLE public.event_publication 
ALTER COLUMN listener_id TYPE character varying(512);

-- Also increase event_type length
ALTER TABLE public.event_publication 
ALTER COLUMN event_type TYPE character varying(512);
