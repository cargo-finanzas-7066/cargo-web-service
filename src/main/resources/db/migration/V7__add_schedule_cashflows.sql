ALTER TABLE payment_schedules
    ADD COLUMN IF NOT EXISTS final_flow NUMERIC(19, 2),
    ADD COLUMN IF NOT EXISTS base_flow NUMERIC(19, 2);
