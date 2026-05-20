-- Migrate wallet_balances.id from BIGSERIAL to UUID for consistency with other tables.
-- Step 1: Add a new uuid column
ALTER TABLE wallet_balances ADD COLUMN new_id UUID NOT NULL DEFAULT gen_random_uuid();

-- Step 2: Update refresh_tokens or any FK that might reference wallet_balances.id
--         (there are none in this schema, so we can proceed directly)

-- Step 3: Drop the old primary key constraint and sequence-driven column
ALTER TABLE wallet_balances DROP CONSTRAINT wallet_balances_pkey;
ALTER TABLE wallet_balances DROP COLUMN id;

-- Step 4: Rename new column to id and set it as primary key
ALTER TABLE wallet_balances RENAME COLUMN new_id TO id;
ALTER TABLE wallet_balances ADD PRIMARY KEY (id);

-- Step 5: Recreate indexes (were on the old integer id, but primary key is re-created above)
--         Other indexes (user_id, currency, composite) are unaffected.

