CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(120) NOT NULL,
    role VARCHAR(40) NOT NULL DEFAULT 'ADVISOR'
);

CREATE TABLE IF NOT EXISTS clients (
    id SERIAL PRIMARY KEY,
    doc_type VARCHAR(20), doc_number VARCHAR(30), names VARCHAR(120), surnames VARCHAR(120),
    email VARCHAR(254), phone VARCHAR(40), address VARCHAR(250), monthly_income DOUBLE PRECISION,
    occupation VARCHAR(120), status VARCHAR(30) DEFAULT 'Activo'
);

CREATE TABLE IF NOT EXISTS vehicles (
    id SERIAL PRIMARY KEY, code VARCHAR(80) UNIQUE, brand VARCHAR(100), model VARCHAR(120), year INTEGER,
    category VARCHAR(80), price DOUBLE PRECISION, currency VARCHAR(3), dealer VARCHAR(160),
    description TEXT, image_url TEXT, status VARCHAR(30) DEFAULT 'Disponible'
);

CREATE TABLE IF NOT EXISTS financial_entities (
    id SERIAL PRIMARY KEY, code VARCHAR(80) UNIQUE, display_order INTEGER, name VARCHAR(160), short_name VARCHAR(100),
    type VARCHAR(100), logo_text VARCHAR(100), currency VARCHAR(3), credit_type VARCHAR(100), product VARCHAR(160),
    tea_published_label VARCHAR(160), minimum_initial_label VARCHAR(160), maximum_financing_label VARCHAR(160),
    term_label VARCHAR(160), grace_label VARCHAR(160), insurance_summary_label VARCHAR(250), charges_summary_label VARCHAR(250),
    tea DOUBLE PRECISION, min_term INTEGER, max_term INTEGER, min_down_payment DOUBLE PRECISION, max_financing DOUBLE PRECISION,
    insurance_disbursement DOUBLE PRECISION, insurance_vehicle DOUBLE PRECISION, monthly_fee DOUBLE PRECISION, admin_cost DOUBLE PRECISION,
    rates_json TEXT, insurances_json TEXT, charges_json TEXT, source_name VARCHAR(200), source_date VARCHAR(40),
    verification_status VARCHAR(80), can_use_in_simulation BOOLEAN DEFAULT TRUE, status VARCHAR(30) DEFAULT 'Activo'
);

CREATE TABLE IF NOT EXISTS simulations (
    id SERIAL PRIMARY KEY, code VARCHAR(40), client_id INTEGER, vehicle_id INTEGER, entity_id INTEGER, currency VARCHAR(3),
    vehicle_price DOUBLE PRECISION, down_payment DOUBLE PRECISION, down_payment_percent DOUBLE PRECISION,
    financed_amount DOUBLE PRECISION, term INTEGER, tea DOUBLE PRECISION, tem DOUBLE PRECISION, payment_day INTEGER,
    disbursement_date DATE, grace_type VARCHAR(20), grace_months INTEGER, balloon_enabled BOOLEAN,
    balloon_amount DOUBLE PRECISION, insurance_disbursement DOUBLE PRECISION, insurance_vehicle DOUBLE PRECISION,
    monthly_fee DOUBLE PRECISION, admin_cost DOUBLE PRECISION, notary_cost DOUBLE PRECISION, other_charges DOUBLE PRECISION,
    monthly_payment DOUBLE PRECISION, van DOUBLE PRECISION, tir DOUBLE PRECISION, tcea DOUBLE PRECISION,
    status VARCHAR(30), created_at DATE
);

CREATE TABLE IF NOT EXISTS payment_schedules (
    id BIGSERIAL PRIMARY KEY, simulation_id INTEGER, period INTEGER, date DATE, initial_balance DOUBLE PRECISION,
    payment DOUBLE PRECISION, balloon_payment DOUBLE PRECISION, interest DOUBLE PRECISION, amortization DOUBLE PRECISION,
    insurance DOUBLE PRECISION, commission DOUBLE PRECISION, total_payment DOUBLE PRECISION, final_balance DOUBLE PRECISION
);
