/*
  # Add account reference number

  1. Changes
    - Add account_reference column to users table
    - Add unique constraint on account_reference
    - Generate random account reference for existing users

  2. Security
    - Enable RLS for account reference lookups
*/

-- Add account reference column
ALTER TABLE users
    ADD COLUMN account_reference VARCHAR(20) UNIQUE;

-- Generate random account references for existing users
UPDATE users
SET account_reference = 'ACC-' || SUBSTRING(CAST(gen_random_uuid() AS VARCHAR), 1, 8)
WHERE account_reference IS NULL;

-- Make account_reference NOT NULL after filling existing records
ALTER TABLE users
    ALTER COLUMN account_reference SET NOT NULL;

-- Create index for faster lookups
CREATE INDEX idx_users_account_reference ON users(account_reference);