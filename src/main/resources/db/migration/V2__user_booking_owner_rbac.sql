ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS user_account_id CHAR(36) NULL,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE hotels
    ADD COLUMN IF NOT EXISTS owner_user_id CHAR(36) NULL;

CREATE INDEX IF NOT EXISTS idx_customers_user_account_id ON customers(user_account_id);
CREATE INDEX IF NOT EXISTS idx_hotels_owner_user_id ON hotels(owner_user_id);

ALTER TABLE customers
    ADD CONSTRAINT fk_customers_user_account
    FOREIGN KEY (user_account_id) REFERENCES users(id);

ALTER TABLE hotels
    ADD CONSTRAINT fk_hotels_owner_user
    FOREIGN KEY (owner_user_id) REFERENCES users(id);
