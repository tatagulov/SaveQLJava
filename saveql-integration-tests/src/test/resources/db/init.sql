CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    display_name VARCHAR(120) NOT NULL,
    email TEXT NOT NULL,
    active BOOLEAN NOT NULL,
    age INTEGER,
    score BIGINT NOT NULL,
    balance NUMERIC(14, 2) NOT NULL,
    rating REAL,
    reputation DOUBLE PRECISION,
    birth_date DATE,
    preferred_contact_time TIME WITH TIME ZONE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    payload BYTEA
);

CREATE TABLE purchase_order (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    amount NUMERIC(14, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE membership (
    tenant_id BIGINT NOT NULL,
    user_id UUID NOT NULL REFERENCES app_user(id),
    role_name VARCHAR(40) NOT NULL,
    active BOOLEAN NOT NULL,
    PRIMARY KEY (tenant_id, user_id)
);

CREATE TABLE all_type_samples (
    id BIGSERIAL PRIMARY KEY,
    uuid_value UUID NOT NULL,
    short_text VARCHAR(50) NOT NULL,
    long_text TEXT,
    bool_value BOOLEAN NOT NULL,
    int_value INTEGER,
    long_value BIGINT,
    decimal_value NUMERIC(18, 4),
    float_value REAL,
    double_value DOUBLE PRECISION,
    local_date_value DATE,
    offset_time_value TIME WITH TIME ZONE,
    local_timestamp_value TIMESTAMP,
    offset_timestamp_value TIMESTAMP WITH TIME ZONE,
    bytes_value BYTEA
);
