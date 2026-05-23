CREATE TABLE roles (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    phone VARCHAR(30),
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
    user_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB;

CREATE TABLE hotels (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    slug VARCHAR(220) NOT NULL UNIQUE,
    description TEXT,
    star_rating DECIMAL(3,2),
    gst_number VARCHAR(40),
    address_line_1 VARCHAR(255),
    address_line_2 VARCHAR(255),
    city VARCHAR(120) NOT NULL,
    state VARCHAR(120),
    country VARCHAR(120) DEFAULT 'India',
    pincode VARCHAR(20),
    phone VARCHAR(30),
    email VARCHAR(180),
    website VARCHAR(180),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hotels_city (city),
    INDEX idx_hotels_active (is_active)
) ENGINE=InnoDB;

CREATE TABLE room_types (
    id CHAR(36) PRIMARY KEY,
    hotel_id CHAR(36) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    base_price DECIMAL(12,2) NOT NULL,
    max_adults INT NOT NULL DEFAULT 2,
    max_children INT NOT NULL DEFAULT 0,
    max_occupancy INT NOT NULL DEFAULT 2,
    bed_type VARCHAR(80),
    size_sqft INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_type_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    INDEX idx_room_types_hotel (hotel_id)
) ENGINE=InnoDB;

CREATE TABLE rooms (
    id CHAR(36) PRIMARY KEY,
    hotel_id CHAR(36) NOT NULL,
    room_type_id CHAR(36) NOT NULL,
    room_number VARCHAR(40) NOT NULL,
    floor_number VARCHAR(40),
    status VARCHAR(40) NOT NULL DEFAULT 'AVAILABLE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    CONSTRAINT fk_room_type FOREIGN KEY (room_type_id) REFERENCES room_types(id),
    UNIQUE KEY uk_hotel_room_number (hotel_id, room_number),
    INDEX idx_rooms_status (status)
) ENGINE=InnoDB;

CREATE TABLE customers (
    id CHAR(36) PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    email VARCHAR(180),
    phone VARCHAR(30) NOT NULL,
    gender VARCHAR(30),
    address TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customers_phone (phone)
) ENGINE=InnoDB;

CREATE TABLE bookings (
    id CHAR(36) PRIMARY KEY,
    booking_number VARCHAR(40) NOT NULL UNIQUE,
    customer_id CHAR(36) NOT NULL,
    hotel_id CHAR(36) NOT NULL,
    source VARCHAR(40) NOT NULL DEFAULT 'CMS',
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    adults INT NOT NULL DEFAULT 1,
    children INT NOT NULL DEFAULT 0,
    booking_status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    payment_status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    grand_total DECIMAL(12,2) NOT NULL DEFAULT 0,
    special_request TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_booking_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    INDEX idx_booking_dates (check_in_date, check_out_date),
    INDEX idx_booking_status (booking_status)
) ENGINE=InnoDB;

CREATE TABLE booking_rooms (
    id CHAR(36) PRIMARY KEY,
    booking_id CHAR(36) NOT NULL,
    room_id CHAR(36) NOT NULL,
    room_type_id CHAR(36) NOT NULL,
    price_per_night DECIMAL(12,2) NOT NULL,
    total_nights INT NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_br_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_br_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_br_room_type FOREIGN KEY (room_type_id) REFERENCES room_types(id)
) ENGINE=InnoDB;

CREATE TABLE payments (
    id CHAR(36) PRIMARY KEY,
    booking_id CHAR(36) NOT NULL,
    payment_number VARCHAR(40) NOT NULL UNIQUE,
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(40) NOT NULL,
    transaction_id VARCHAR(120),
    payment_status VARCHAR(40) NOT NULL DEFAULT 'PAID',
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    INDEX idx_payments_booking (booking_id)
) ENGINE=InnoDB;

CREATE TABLE promo_codes (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    description TEXT,
    discount_type VARCHAR(40) NOT NULL,
    discount_value DECIMAL(12,2) NOT NULL,
    min_booking_amount DECIMAL(12,2) DEFAULT 0,
    max_discount_amount DECIMAL(12,2),
    usage_limit INT,
    used_count INT NOT NULL DEFAULT 0,
    start_date DATE,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE cms_pages (
    id CHAR(36) PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    slug VARCHAR(220) NOT NULL UNIQUE,
    content MEDIUMTEXT,
    meta_title VARCHAR(180),
    meta_description TEXT,
    status VARCHAR(40) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_pages_slug_status (slug, status)
) ENGINE=InnoDB;

CREATE TABLE banners (
    id CHAR(36) PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    subtitle VARCHAR(255),
    image_url VARCHAR(500),
    button_text VARCHAR(80),
    button_link VARCHAR(255),
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36),
    module VARCHAR(80) NOT NULL,
    action VARCHAR(80) NOT NULL,
    entity_id CHAR(36),
    old_value JSON,
    new_value JSON,
    ip_address VARCHAR(80),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_audit_module_action (module, action)
) ENGINE=InnoDB;
