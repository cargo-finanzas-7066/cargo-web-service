ALTER TABLE payment_schedules
    ADD COLUMN IF NOT EXISTS credit_life_insurance NUMERIC(19, 2),
    ADD COLUMN IF NOT EXISTS vehicle_insurance NUMERIC(19, 2);

UPDATE payment_schedules ps
SET credit_life_insurance = ROUND(
        s.financed_amount * COALESCE((s.product_snapshot ->> 'creditLifeInsuranceMonthlyPercent')::NUMERIC, 0) / 100,
        2
    ),
    vehicle_insurance = ROUND(
        s.vehicle_price * ROUND(
            COALESCE((s.product_snapshot ->> 'vehicleInsuranceAnnualPercent')::NUMERIC, 0) / 12,
            4
        ) / 100,
        2
    )
FROM simulations s
WHERE s.id = ps.simulation_id
  AND (ps.credit_life_insurance IS NULL OR ps.vehicle_insurance IS NULL);

UPDATE payment_schedules SET credit_life_insurance = 0 WHERE credit_life_insurance IS NULL;
UPDATE payment_schedules SET vehicle_insurance = 0 WHERE vehicle_insurance IS NULL;

ALTER TABLE payment_schedules ALTER COLUMN credit_life_insurance SET DEFAULT 0;
ALTER TABLE payment_schedules ALTER COLUMN vehicle_insurance SET DEFAULT 0;
