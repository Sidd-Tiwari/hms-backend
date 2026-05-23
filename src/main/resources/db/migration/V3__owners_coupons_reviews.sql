ALTER TABLE promo_codes
    ADD COLUMN IF NOT EXISTS hotel_id CHAR(36) NULL;

CREATE INDEX IF NOT EXISTS idx_promo_codes_hotel_id ON promo_codes(hotel_id);

ALTER TABLE promo_codes
    ADD CONSTRAINT fk_promo_code_hotel
    FOREIGN KEY (hotel_id) REFERENCES hotels(id);

CREATE TABLE IF NOT EXISTS reviews (
    id CHAR(36) PRIMARY KEY,
    booking_id CHAR(36) NOT NULL,
    customer_id CHAR(36) NOT NULL,
    hotel_id CHAR(36) NOT NULL,
    rating INT NOT NULL,
    feedback TEXT,
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_review_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    UNIQUE KEY uk_review_booking_customer (booking_id, customer_id),
    INDEX idx_review_hotel_status (hotel_id, status)
) ENGINE=InnoDB;

UPDATE hotels
SET owner_user_id = (SELECT id FROM users WHERE email = 'owner@hotel.com' LIMIT 1)
WHERE owner_user_id IS NULL;

INSERT INTO customers (id, full_name, email, phone, user_account_id, is_active, created_at, updated_at)
SELECT UUID(), u.full_name, u.email, COALESCE(u.phone, 'N/A'), u.id, TRUE, NOW(), NOW()
FROM users u
WHERE u.email = 'user@hotel.com'
AND NOT EXISTS (SELECT 1 FROM customers c WHERE c.user_account_id = u.id);
