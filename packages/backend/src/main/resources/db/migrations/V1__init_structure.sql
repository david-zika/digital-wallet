-- Create users table
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create wallet_balances table
CREATE TABLE wallet_balances (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id UUID NOT NULL REFERENCES users(id),
                                 currency VARCHAR(3) NOT NULL CHECK (currency IN ('EUR', 'CZK')),
                                 balance DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
                                 last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 UNIQUE (user_id, currency)
);

-- Create transactions table
CREATE TABLE transactions (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              user_id UUID NOT NULL REFERENCES users(id),
                              amount DECIMAL(20, 2) NOT NULL CHECK (amount > 0),
                              currency VARCHAR(3) NOT NULL CHECK (currency IN ('EUR', 'CZK')),
                              type VARCHAR(10) NOT NULL CHECK (type IN ('DEPOSIT', 'WITHDRAWAL')),
                              status VARCHAR(10) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
                              recipient_account VARCHAR(255),
                              recipient_name VARCHAR(255),
                              payment_reference VARCHAR(255),
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Enable btree_gist extension for timestamp range indexing
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Indexes for users table
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Indexes for wallet_balances table
CREATE INDEX idx_wallet_balances_user_id ON wallet_balances(user_id);
CREATE INDEX idx_wallet_balances_currency ON wallet_balances(currency);
CREATE INDEX idx_wallet_balances_last_updated ON wallet_balances(last_updated);

-- Indexes for transactions table
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_currency ON transactions(currency);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- Add composite indexes for common query patterns
CREATE INDEX idx_wallet_balances_user_currency
    ON wallet_balances(user_id, currency);

CREATE INDEX idx_transactions_user_date
    ON transactions(user_id, created_at DESC);

-- Add range index for timestamp range queries
CREATE INDEX idx_transactions_created_at_range
    ON transactions USING GIST (created_at);

-- Add indexes for full text search on payment references
CREATE INDEX idx_transactions_payment_reference_gin
    ON transactions USING GIN (to_tsvector('simple', coalesce(payment_reference, '')));
