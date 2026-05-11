-- =========================================================
-- PayTrack — SQL Schema
-- Multi-tenant billing system (Company-scoped)
-- MySQL 8+
-- =========================================================

DROP DATABASE IF EXISTS paytrack_db;
CREATE DATABASE paytrack_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE paytrack_db;

-- =========================================================
-- USERS & AUTH
-- =========================================================
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  password VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE authorities (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  authority VARCHAR(50) NOT NULL,
  UNIQUE(user_id, authority),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =========================================================
-- COMPANY & MEMBERSHIP (MULTI-TENANT)
-- =========================================================
CREATE TABLE companies (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  registration_number VARCHAR(100) NOT NULL,
  phone VARCHAR(64),
  email VARCHAR(255),
  billing_address_line1 VARCHAR(255),
  billing_address_line2 VARCHAR(255),
  billing_postcode VARCHAR(32),
  billing_city VARCHAR(128),
  billing_state VARCHAR(128),
  billing_country VARCHAR(128) DEFAULT 'Malaysia',
  default_currency VARCHAR(10) DEFAULT 'MYR',
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted BOOLEAN DEFAULT FALSE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(registration_number)
);

CREATE TABLE membership_roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO membership_roles (name) VALUES
('OWNER'), ('ADMIN'), ('MEMBER');

CREATE TABLE memberships (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  company_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, company_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (company_id) REFERENCES companies(id),
  FOREIGN KEY (role_id) REFERENCES membership_roles(id)
);

-- =========================================================
-- CUSTOMERS
-- =========================================================
CREATE TABLE customers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,

  customer_number VARCHAR(50),

  name VARCHAR(255) NOT NULL,
  email VARCHAR(255),
  phone VARCHAR(64),

  company_name VARCHAR(255),
  registration_number VARCHAR(100),

  billing_address_line1 VARCHAR(255),
  billing_address_line2 VARCHAR(255),
  billing_postcode VARCHAR(32),
  billing_city VARCHAR(128),
  billing_state VARCHAR(128),
  billing_country VARCHAR(128),

  notes TEXT,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  archived_at DATETIME NULL,

  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (company_id) REFERENCES companies(id),
  UNIQUE(company_id, customer_number),
  INDEX(company_id, archived_at),
  INDEX(company_id, email)
);

-- =========================================================
-- CATALOG (PRODUCTS / SERVICES)
-- =========================================================
CREATE TABLE catalog_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,

  item_code VARCHAR(50),

  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(12,2) NOT NULL,

  archived_at DATETIME NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (company_id) REFERENCES companies(id),
  UNIQUE(company_id, item_code),
  INDEX(company_id, archived_at)
);

-- =========================================================
-- QUOTATIONS (START OF BUSINESS FLOW)
-- =========================================================
CREATE TABLE quotations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,

  quotation_number VARCHAR(50) NOT NULL,
  status VARCHAR(32) NOT NULL,

  issue_date DATE,
  valid_until DATE,
  currency VARCHAR(10) DEFAULT 'MYR',

  customer_name VARCHAR(255),
  customer_company VARCHAR(255),
  customer_email VARCHAR(255),
  billing_address TEXT,

  subtotal DECIMAL(12,2),
  discount DECIMAL(12,2),
  tax DECIMAL(12,2),
  grand_total DECIMAL(12,2),

  notes TEXT,

  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

  UNIQUE(company_id, quotation_number),

  FOREIGN KEY (company_id) REFERENCES companies(id),
  FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE quotation_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  quotation_id BIGINT NOT NULL,

  name VARCHAR(255),
  description TEXT,
  unit_price DECIMAL(12,2),
  quantity INT,
  subtotal DECIMAL(12,2),

  FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE CASCADE,
  INDEX(quotation_id)
);

-- =========================================================
-- INVOICES
-- =========================================================
CREATE TABLE invoices (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,

  invoice_number VARCHAR(50) NOT NULL,
  status VARCHAR(32) NOT NULL,

  issue_date DATE,
  due_date DATE,
  currency VARCHAR(10) DEFAULT 'MYR',

  customer_name VARCHAR(255),
  customer_company VARCHAR(255),
  customer_email VARCHAR(255),
  billing_address TEXT,

  notes TEXT,

  subtotal DECIMAL(12,2) DEFAULT 0,
  discount DECIMAL(12,2) DEFAULT 0,
  tax DECIMAL(12,2) DEFAULT 0,
  grand_total DECIMAL(12,2) DEFAULT 0,

  paid_amount DECIMAL(12,2) DEFAULT 0,
  remaining_amount DECIMAL(12,2) DEFAULT 0,

  issued_at DATETIME,
  paid_at DATETIME,

  source_quotation_id BIGINT,

  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

  UNIQUE(company_id, invoice_number),

  FOREIGN KEY (company_id) REFERENCES companies(id),
  FOREIGN KEY (customer_id) REFERENCES customers(id),
  FOREIGN KEY (source_quotation_id) REFERENCES quotations(id),

  INDEX(company_id, status),
  INDEX(company_id, issue_date)
);

CREATE TABLE invoice_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  invoice_id BIGINT NOT NULL,

  name VARCHAR(255),
  description TEXT,
  unit_price DECIMAL(12,2),
  quantity INT,
  subtotal DECIMAL(12,2),

  FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
  INDEX(invoice_id)
);

-- =========================================================
-- PAYMENTS (END OF BUSINESS FLOW)
-- =========================================================
CREATE TABLE payments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  invoice_id BIGINT NOT NULL,

  receipt_number VARCHAR(50),

  amount DECIMAL(12,2) NOT NULL,
  payment_date DATETIME,

  method VARCHAR(50),
  reference VARCHAR(255),
  notes TEXT,
  receipt_url VARCHAR(512),

  voided BOOLEAN NOT NULL DEFAULT FALSE,
  voided_at DATETIME NULL,
  voided_by VARCHAR(255),

  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (company_id) REFERENCES companies(id),
  FOREIGN KEY (invoice_id) REFERENCES invoices(id),

  UNIQUE(company_id, receipt_number),
  INDEX(invoice_id),
  INDEX(company_id)
);

-- =========================================================
-- AUDIT LOGS
-- =========================================================
CREATE TABLE invoice_status_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  invoice_id BIGINT NOT NULL,

  from_status VARCHAR(32),
  to_status VARCHAR(32),

  changed_by VARCHAR(255),
  changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (invoice_id) REFERENCES invoices(id),

  INDEX(invoice_id)
);

-- =========================================================
-- INVITATIONS
-- =========================================================
CREATE TABLE invitations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  invitee_email VARCHAR(255) NOT NULL,
  token VARCHAR(128) NOT NULL UNIQUE,
  accepted BOOLEAN NOT NULL DEFAULT FALSE,
  expires_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (company_id) REFERENCES companies(id),
  FOREIGN KEY (role_id) REFERENCES membership_roles(id),
  INDEX(company_id),
  INDEX(token)
);

-- =========================================================
-- MIGRATIONS (run manually on existing databases)
-- =========================================================
-- ALTER TABLE companies ADD COLUMN default_currency VARCHAR(10) DEFAULT 'MYR';
-- ALTER TABLE quotation_items ADD COLUMN name VARCHAR(255) AFTER quotation_id, MODIFY COLUMN description TEXT;
-- ALTER TABLE invoice_items ADD COLUMN name VARCHAR(255) AFTER invoice_id, MODIFY COLUMN description TEXT;

-- =========================================================
-- END OF SCHEMA
-- =========================================================