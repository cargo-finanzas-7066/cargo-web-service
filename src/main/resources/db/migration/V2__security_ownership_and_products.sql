ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();
UPDATE users SET role = CASE WHEN role IN ('ADMIN','ADVISOR','CLIENT') THEN role ELSE 'ADVISOR' END;
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_lower ON users(lower(email));

INSERT INTO users(email,password,name,role,active)
SELECT 'migration@cargo.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoOeR6O.JjoIL6O6b3f1x1wX9U7M8QfM0K', 'Usuario de migración', 'ADVISOR', false
WHERE (EXISTS (SELECT 1 FROM clients) OR EXISTS (SELECT 1 FROM simulations)) AND NOT EXISTS (SELECT 1 FROM users);

ALTER TABLE clients ADD COLUMN IF NOT EXISTS owner_user_id INTEGER;
ALTER TABLE clients ADD COLUMN IF NOT EXISTS archived BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE clients ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE clients ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
UPDATE clients SET owner_user_id = (SELECT id FROM users ORDER BY active DESC, id LIMIT 1) WHERE owner_user_id IS NULL;
CREATE INDEX IF NOT EXISTS ix_clients_owner ON clients(owner_user_id, archived);
CREATE UNIQUE INDEX IF NOT EXISTS ux_clients_owner_document ON clients(owner_user_id, doc_type, doc_number) WHERE archived = false;

ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
UPDATE vehicles SET active = COALESCE(status,'Disponible') <> 'No disponible';

ALTER TABLE simulations ADD COLUMN IF NOT EXISTS owner_user_id INTEGER;
ALTER TABLE simulations ADD COLUMN IF NOT EXISTS financial_product_id INTEGER;
ALTER TABLE simulations ADD COLUMN IF NOT EXISTS product_snapshot JSONB;
ALTER TABLE simulations ADD COLUMN IF NOT EXISTS archived BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE simulations ADD COLUMN IF NOT EXISTS created_at_ts TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE simulations ADD COLUMN IF NOT EXISTS balloon_percent NUMERIC(7,4) NOT NULL DEFAULT 0;
ALTER TABLE simulations ADD COLUMN IF NOT EXISTS total_interest NUMERIC(19,2) NOT NULL DEFAULT 0;
ALTER TABLE simulations ADD COLUMN IF NOT EXISTS total_insurance NUMERIC(19,2) NOT NULL DEFAULT 0;
ALTER TABLE simulations ADD COLUMN IF NOT EXISTS total_fees NUMERIC(19,2) NOT NULL DEFAULT 0;
ALTER TABLE simulations ADD COLUMN IF NOT EXISTS total_payment NUMERIC(19,2) NOT NULL DEFAULT 0;
ALTER TABLE simulations ADD COLUMN IF NOT EXISTS first_payment_date DATE;
UPDATE simulations SET first_payment_date = COALESCE((disbursement_date + INTERVAL '1 month')::date, CURRENT_DATE) WHERE first_payment_date IS NULL;
ALTER TABLE payment_schedules ADD COLUMN IF NOT EXISTS grace_type VARCHAR(20) NOT NULL DEFAULT 'NONE';
UPDATE simulations SET owner_user_id = (SELECT id FROM users ORDER BY active DESC, id LIMIT 1) WHERE owner_user_id IS NULL;
CREATE INDEX IF NOT EXISTS ix_simulations_owner ON simulations(owner_user_id, archived, created_at_ts DESC);

CREATE TABLE IF NOT EXISTS financial_products (
    id SERIAL PRIMARY KEY,
    financial_institution_id INTEGER NOT NULL REFERENCES financial_entities(id),
    product_name VARCHAR(160) NOT NULL,
    version INTEGER NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'PEN',
    tea_percent NUMERIC(12,7) NOT NULL,
    min_term_months INTEGER NOT NULL,
    max_term_months INTEGER NOT NULL,
    min_down_payment_percent NUMERIC(7,4) NOT NULL,
    max_down_payment_percent NUMERIC(7,4) NOT NULL DEFAULT 100,
    balloon_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    max_balloon_percent NUMERIC(7,4) NOT NULL DEFAULT 0,
    credit_life_insurance_monthly_percent NUMERIC(12,7) NOT NULL DEFAULT 0,
    vehicle_insurance_annual_percent NUMERIC(12,7) NOT NULL DEFAULT 0,
    monthly_fee NUMERIC(19,2) NOT NULL DEFAULT 0,
    admin_cost NUMERIC(19,2) NOT NULL DEFAULT 0,
    notary_cost NUMERIC(19,2) NOT NULL DEFAULT 0,
    other_upfront_cost NUMERIC(19,2) NOT NULL DEFAULT 0,
    capitalize_interest_total_grace BOOLEAN NOT NULL DEFAULT TRUE,
    capitalize_insurance_total_grace BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from DATE NOT NULL,
    valid_until DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(financial_institution_id, product_name, version)
);

INSERT INTO financial_products(financial_institution_id,product_name,version,currency,tea_percent,min_term_months,max_term_months,
    min_down_payment_percent,max_down_payment_percent,balloon_allowed,max_balloon_percent,
    credit_life_insurance_monthly_percent,vehicle_insurance_annual_percent,monthly_fee,admin_cost,valid_from,active)
SELECT id,COALESCE(product,'Crédito vehicular'),1,COALESCE(currency,'PEN'),COALESCE(tea,0),COALESCE(min_term,1),COALESCE(max_term,60),
       COALESCE(min_down_payment,0),100,true,50,COALESCE(insurance_disbursement,0),COALESCE(insurance_vehicle,0),
       COALESCE(monthly_fee,0),COALESCE(admin_cost,0),CURRENT_DATE,COALESCE(status,'Activo')='Activo'
FROM financial_entities fe
WHERE code IS NOT NULL AND NOT EXISTS (SELECT 1 FROM financial_products fp WHERE fp.financial_institution_id=fe.id);

UPDATE simulations s SET financial_product_id = (
    SELECT fp.id FROM financial_products fp WHERE fp.financial_institution_id=s.entity_id ORDER BY fp.version DESC LIMIT 1
) WHERE financial_product_id IS NULL AND entity_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS migration_issues (
    id BIGSERIAL PRIMARY KEY, migration_version VARCHAR(20) NOT NULL, entity_type VARCHAR(40) NOT NULL,
    entity_id BIGINT, issue VARCHAR(250) NOT NULL, detected_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO migration_issues(migration_version,entity_type,entity_id,issue)
SELECT 'V2','simulation',s.id,'Cliente heredado inexistente' FROM simulations s LEFT JOIN clients c ON c.id=s.client_id WHERE s.client_id IS NOT NULL AND c.id IS NULL;
INSERT INTO migration_issues(migration_version,entity_type,entity_id,issue)
SELECT 'V2','simulation',s.id,'Vehículo heredado inexistente' FROM simulations s LEFT JOIN vehicles v ON v.id=s.vehicle_id WHERE s.vehicle_id IS NOT NULL AND v.id IS NULL;
INSERT INTO migration_issues(migration_version,entity_type,entity_id,issue)
SELECT 'V2','payment_schedule',p.id,'Simulación heredada inexistente' FROM payment_schedules p LEFT JOIN simulations s ON s.id=p.simulation_id WHERE p.simulation_id IS NOT NULL AND s.id IS NULL;
UPDATE simulations SET archived=true, client_id=NULL WHERE client_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM clients c WHERE c.id=simulations.client_id);
UPDATE simulations SET archived=true, vehicle_id=NULL WHERE vehicle_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM vehicles v WHERE v.id=simulations.vehicle_id);
DELETE FROM payment_schedules WHERE simulation_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM simulations s WHERE s.id=payment_schedules.simulation_id);
ALTER TABLE clients ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE simulations ALTER COLUMN owner_user_id SET NOT NULL;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_clients_owner') THEN
        ALTER TABLE clients ADD CONSTRAINT fk_clients_owner FOREIGN KEY(owner_user_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_simulations_owner') THEN
        ALTER TABLE simulations ADD CONSTRAINT fk_simulations_owner FOREIGN KEY(owner_user_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_simulations_client') THEN
        ALTER TABLE simulations ADD CONSTRAINT fk_simulations_client FOREIGN KEY(client_id) REFERENCES clients(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_simulations_vehicle') THEN
        ALTER TABLE simulations ADD CONSTRAINT fk_simulations_vehicle FOREIGN KEY(vehicle_id) REFERENCES vehicles(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_simulations_product') THEN
        ALTER TABLE simulations ADD CONSTRAINT fk_simulations_product FOREIGN KEY(financial_product_id) REFERENCES financial_products(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_schedules_simulation') THEN
        ALTER TABLE payment_schedules ADD CONSTRAINT fk_schedules_simulation FOREIGN KEY(simulation_id) REFERENCES simulations(id) ON DELETE CASCADE;
    END IF;
END $$;
