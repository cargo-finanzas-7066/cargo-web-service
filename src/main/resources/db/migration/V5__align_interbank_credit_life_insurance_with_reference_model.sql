-- La segunda hoja de referencia usa 0.038% mensual para Interbank.
UPDATE financial_products fp
SET credit_life_insurance_monthly_percent = 0.0380000
FROM financial_entities fe
WHERE fp.financial_institution_id = fe.id
  AND fe.code = 'INTERBANK'
  AND fp.version = 1;

UPDATE financial_entities
SET insurance_disbursement = 0.0380000
WHERE code = 'INTERBANK';
