/*
  # Add user profile fields

  1. Changes
    - Add full_name column to users table
    - Add bank_account column to users table
    - Add indexes for improved query performance

  2. Security
    - Both fields are nullable initially
    - Will be required for new users via application logic
*/

-- Add new columns
ALTER TABLE users
    ADD COLUMN full_name VARCHAR(255),
    ADD COLUMN bank_account VARCHAR(255);

-- Add indexes for performance
CREATE INDEX idx_users_full_name ON users(full_name);
CREATE INDEX idx_users_bank_account ON users(bank_account);

-- Add comments for documentation
COMMENT ON COLUMN users.full_name IS 'User''s full name';
COMMENT ON COLUMN users.bank_account IS 'User''s bank account number';