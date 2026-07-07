-- BCP seed: desgravamen individual 0.050% mensual y vehicular riesgo 1 4.72% anual.
UPDATE financial_products fp
SET credit_life_insurance_monthly_percent = 0.0500000,
    vehicle_insurance_annual_percent = 4.7200000
FROM financial_entities fe
WHERE fp.financial_institution_id = fe.id
  AND fe.code = 'BCP'
  AND fp.active = true;

UPDATE financial_entities
SET insurance_disbursement = 0.0500000,
    insurance_vehicle = 4.7200000,
    insurances_json = '[{"type":"DEGRAVAMEN","label":"Seguro de desgravamen individual","required":true,"ratePercentMonthly":0.050,"displayValue":"0.050% mensual (minimo referencial; varia segun perfil de riesgo del cliente)","base":"SALDO_DEUDOR"},{"type":"VEHICULAR","label":"Seguro vehicular (riesgo 1)","required":true,"ratePercentAnnual":4.72,"displayValue":"4.72% anual (riesgo 1); varia segun categoria de riesgo del vehiculo","base":"VALOR_VEHICULO"}]'
WHERE code = 'BCP';
