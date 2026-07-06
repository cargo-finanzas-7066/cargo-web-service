-- La hoja de referencia usa 3.48% anual (0.29% mensual) para el seguro vehicular BBVA.
UPDATE financial_products fp
SET vehicle_insurance_annual_percent = 3.4800000
FROM financial_entities fe
WHERE fp.financial_institution_id = fe.id
  AND fe.code = 'BBVA'
  AND fp.version = 1;

UPDATE financial_entities
SET insurance_vehicle = 3.4800000
WHERE code = 'BBVA';
