-- =========================================================
-- PayTrack — SEED DATA (ENHANCED VERSION)
-- =========================================================

USE paytrack_db;

-- =========================================================
-- USERS
-- =========================================================
INSERT INTO users (id, email, name, password, enabled) VALUES
(1, 'admin@paytrack.com',     'Admin',        '$2b$10$sIbhccc6z/08lUK9JPOwheOf56dRZqFiEgYKfK9X.po5RSVgyl592', TRUE),
(2, 'sarah.chen@paytrack.com',  'Sarah Chen',   '$2b$10$sIbhccc6z/08lUK9JPOwheOf56dRZqFiEgYKfK9X.po5RSVgyl592', TRUE),
(3, 'raj.kumar@paytrack.com',   'Raj Kumar',    '$2b$10$sIbhccc6z/08lUK9JPOwheOf56dRZqFiEgYKfK9X.po5RSVgyl592', TRUE),
(4, 'aida.hassan@paytrack.com', 'Aida Hassan',  '$2b$10$sIbhccc6z/08lUK9JPOwheOf56dRZqFiEgYKfK9X.po5RSVgyl592', TRUE),
(5, 'james.lim@paytrack.com',   'James Lim',    '$2b$10$sIbhccc6z/08lUK9JPOwheOf56dRZqFiEgYKfK9X.po5RSVgyl592', TRUE),
(6, 'priya.nair@paytrack.com',  'Priya Nair',   '$2b$10$sIbhccc6z/08lUK9JPOwheOf56dRZqFiEgYKfK9X.po5RSVgyl592', TRUE),
(7, 'fariz.ali@paytrack.com',   'Fariz Ali',    '$2b$10$sIbhccc6z/08lUK9JPOwheOf56dRZqFiEgYKfK9X.po5RSVgyl592', TRUE);

INSERT INTO authorities (user_id, authority) VALUES
(1, 'ROLE_USER'),
(1, 'ROLE_ADMIN'),
(2, 'ROLE_USER'),
(3, 'ROLE_USER'),
(4, 'ROLE_USER'),
(5, 'ROLE_USER'),
(6, 'ROLE_USER'),
(7, 'ROLE_USER');

-- =========================================================
-- COMPANIES
-- =========================================================
INSERT INTO companies (
  id, name, registration_number, phone, email,
  billing_address_line1, billing_city, billing_state, billing_country
)
VALUES
(1, 'Microsoft Malaysia Sdn Bhd', '199201002345', '+60322618888', 'enterprise@microsoft.com',
 'Menara 3 PETRONAS', 'Kuala Lumpur', 'WP Kuala Lumpur', 'Malaysia'),

(2, 'IBM Malaysia Sdn Bhd', '196901000123', '+60320550000', 'enterprise@ibm.com',
 'IBM Tower', 'Petaling Jaya', 'Selangor', 'Malaysia');

-- =========================================================
-- MEMBERSHIPS
-- =========================================================
-- user 1 = OWNER of both companies
-- users 2-4 = ADMIN/MEMBER of Microsoft Malaysia
-- users 5-7 = ADMIN/MEMBER of IBM Malaysia
INSERT INTO memberships (user_id, company_id, role_id) VALUES
(1, 1, 1),
(1, 2, 1),
(2, 1, 2),
(3, 1, 3),
(4, 1, 3),
(5, 2, 2),
(6, 2, 3),
(7, 2, 3);

-- =========================================================
-- CUSTOMERS
-- =========================================================
INSERT INTO customers (
  company_id, customer_number, name, email, phone,
  company_name, registration_number,
  billing_address_line1, billing_city, billing_state, billing_country,
  status
)
VALUES
-- Microsoft
(1, 'CUS-00001', 'Cloud Procurement Team', 'cloud@petronas.com', '+60323310000',
 'PETRONAS Digital', '197401002911',
 'KLCC Tower', 'Kuala Lumpur', 'WP Kuala Lumpur', 'Malaysia', 'ACTIVE'),

(1, 'CUS-00002', 'Platform Engineering', 'platform@grab.com', '+60327280000',
 'Grab Holdings', '201401045678',
 'Mid Valley City', 'Kuala Lumpur', 'WP Kuala Lumpur', 'Malaysia', 'ACTIVE'),

(1, 'CUS-00003', 'Enterprise IT', 'it@shopee.com', '+60327770000',
 'Shopee Malaysia', '201501234567',
 'Southpoint Tower', 'Kuala Lumpur', 'WP Kuala Lumpur', 'Malaysia', 'ACTIVE'),

-- IBM
(2, 'CUS-00001', 'Data & AI Division', 'ai@maybank.com', '+60320708833',
 'Malayan Banking Berhad', '196001000142',
 'Menara Maybank', 'Kuala Lumpur', 'WP Kuala Lumpur', 'Malaysia', 'ACTIVE'),

(2, 'CUS-00002', 'Infrastructure Ops', 'infra@tm.com.my', '+60322401299',
 'Telekom Malaysia', '198401010032',
 'Menara TM', 'Kuala Lumpur', 'WP Kuala Lumpur', 'Malaysia', 'ACTIVE'),

(2, 'CUS-00003', 'Security Operations', 'security@axiata.com', '+60322638888',
 'Axiata Group', '199201234567',
 'Axiata Tower', 'Kuala Lumpur', 'WP Kuala Lumpur', 'Malaysia', 'ACTIVE'),

-- Microsoft — additional customers (IDs 7–9)
(1, 'CUS-00004', 'Enterprise IT', 'enterprise@maxis.com.my', '+60323303000',
 'Maxis Communications', '199401010932',
 'Menara Maxis, KLCC', 'Kuala Lumpur', 'WP Kuala Lumpur', 'Malaysia', 'ACTIVE'),

(1, 'CUS-00005', 'Technology Division', 'tech@airasia.com', '+60387751000',
 'AirAsia Digital', '201301001234',
 'RedQ, Sepang', 'Sepang', 'Selangor', 'Malaysia', 'ACTIVE'),

(1, 'CUS-00006', 'Analytics Team', 'analytics@ctos.com.my', '+60322990000',
 'CTOS Data Systems', '200301008765',
 'Menara CTOS, Bangsar South', 'Kuala Lumpur', 'WP Kuala Lumpur', 'Malaysia', 'ACTIVE'),

-- IBM — additional customers (IDs 10–12)
(2, 'CUS-00004', 'Group Technology', 'tech@cimb.com', '+60320848888',
 'CIMB Group Holdings', '198401008250',
 'Menara CIMB, KL Sentral', 'Kuala Lumpur', 'WP Kuala Lumpur', 'Malaysia', 'ACTIVE'),

(2, 'CUS-00005', 'Infrastructure Division', 'infra@petronas-ict.com.my', '+60320515000',
 'Petronas ICT Sdn Bhd', '199701005432',
 'Petronas Twin Towers', 'Kuala Lumpur', 'WP Kuala Lumpur', 'Malaysia', 'ACTIVE'),

(2, 'CUS-00006', 'Digital Innovation', 'digital@simedarby.com', '+60356396000',
 'Sime Darby Technology Centre', '200101003456',
 'Ara Damansara', 'Petaling Jaya', 'Selangor', 'Malaysia', 'ACTIVE');

-- =========================================================
-- CATALOG ITEMS (WITH ARCHIVED)
-- =========================================================
INSERT INTO catalog_items (company_id, item_code, name, description, price, archived_at) VALUES
-- Microsoft
(1, 'ITM-00001', 'Azure Cloud Migration',
 'Migration of legacy systems to Azure including compute, database, and networking setup',
 80000, NULL),

(1, 'ITM-00002', 'Microsoft 365 Licensing',
 'Annual enterprise license per user including Outlook, Teams, OneDrive, and SharePoint',
 459, NULL),

(1, 'ITM-00003', 'AI Copilot Integration',
 'Integration of AI copilots into workflows for automation and productivity enhancement',
 60000, NULL),

(1, 'ITM-00004', 'Legacy System Support (Deprecated)',
 'Support for legacy systems (no longer offered)',
 15000, NOW()), -- archived

(1, 'ITM-00005', 'Azure DevOps & CI/CD Pipeline',
 'End-to-end Azure DevOps setup including pipelines, repos, and artifact management',
 25000, NULL),

(1, 'ITM-00006', 'Power BI Enterprise License',
 'Annual Power BI Premium per-user license with capacity for dashboards and reports',
 1200, NULL),

(1, 'ITM-00007', 'Microsoft Dynamics 365 CRM',
 'Implementation and configuration of Dynamics 365 Sales and Customer Service modules',
 45000, NULL),

-- IBM
(2, 'ITM-00001', 'Watson AI Deployment',
 'Deployment of IBM Watson AI solutions for analytics and automation',
 70000, NULL),

(2, 'ITM-00002', 'Hybrid Cloud Infrastructure',
 'Design and implementation of hybrid cloud across environments',
 50000, NULL),

(2, 'ITM-00003', 'Cybersecurity Intelligence',
 'Threat detection and incident response services',
 30000, NULL),

(2, 'ITM-00004', 'Data Lake Platform',
 'Enterprise data lake setup with ingestion and analytics',
 55000, NULL),

(2, 'ITM-00005', 'IBM Cloud Pak for Data',
 'Unified platform for data collection, organization, analysis, and infusion of AI',
 65000, NULL),

(2, 'ITM-00006', 'IBM Security QRadar',
 'SIEM platform deployment for threat detection, compliance, and security analytics',
 40000, NULL),

(2, 'ITM-00007', 'Mainframe Modernization',
 'Full modernization of legacy mainframe systems to cloud-native architecture',
 90000, NULL);

-- =========================================================
-- INVOICES (INCLUDING CANCELLED)
-- =========================================================
INSERT INTO invoices (
  id, company_id, customer_id, invoice_number, status,
  issue_date, due_date, currency,
  customer_name, customer_company, customer_email, billing_address,
  subtotal, grand_total, paid_amount, remaining_amount
)
VALUES
(1, 1, 1, 'INV-2026-00001', 'PAID',
 '2026-05-01', '2026-05-15', 'MYR',
 'Cloud Procurement Team', 'PETRONAS Digital', 'cloud@petronas.com',
 'KLCC Tower, Kuala Lumpur, Malaysia',
 55080, 55080, 55080, 0),

(2, 1, 2, 'INV-2026-00002', 'PARTIALLY_PAID',
 '2026-05-02', '2026-05-16', 'MYR',
 'Platform Engineering', 'Grab Holdings', 'platform@grab.com',
 'Mid Valley, Kuala Lumpur, Malaysia',
 85000, 85000, 20000, 65000),

(3, 1, 3, 'INV-2026-00003', 'ISSUED',
 '2026-05-03', '2026-05-17', 'MYR',
 'Enterprise IT', 'Shopee Malaysia', 'it@shopee.com',
 'Southpoint Tower, Kuala Lumpur, Malaysia',
 4590, 4590, 0, 4590),

(4, 2, 4, 'INV-2026-00001', 'PAID',
 '2026-05-01', '2026-05-15', 'MYR',
 'Data & AI Division', 'Malayan Banking Berhad', 'ai@maybank.com',
 'Menara Maybank, Kuala Lumpur, Malaysia',
 70000, 70000, 70000, 0),

(5, 2, 5, 'INV-2026-00002', 'ISSUED',
 '2026-05-02', '2026-05-16', 'MYR',
 'Infrastructure Ops', 'Telekom Malaysia', 'infra@tm.com.my',
 'Menara TM, Kuala Lumpur, Malaysia',
 50000, 50000, 0, 50000),

(6, 2, 6, 'INV-2026-00003', 'PARTIALLY_PAID',
 '2026-05-03', '2026-05-17', 'MYR',
 'Security Operations', 'Axiata Group', 'security@axiata.com',
 'Axiata Tower, Kuala Lumpur, Malaysia',
 30000, 30000, 10000, 20000),

(7, 1, 1, 'INV-2026-00004', 'CANCELLED',
 '2026-05-05', '2026-05-20', 'MYR',
 'Cloud Procurement Team', 'PETRONAS Digital', 'cloud@petronas.com',
 'KLCC Tower, Kuala Lumpur, Malaysia',
 20000, 20000, 0, 0),

-- Microsoft — additional invoices (IDs 8–13)
(8,  1, 7, 'INV-2026-00005', 'DRAFT',
 '2026-05-06', '2026-05-21', 'MYR',
 'Enterprise IT', 'Maxis Communications', 'enterprise@maxis.com.my',
 'Menara Maxis, KLCC, Kuala Lumpur, Malaysia',
 25000, 25000, 0, 25000),

(9,  1, 8, 'INV-2026-00006', 'ISSUED',
 '2026-04-10', '2026-04-25', 'MYR',
 'Technology Division', 'AirAsia Digital', 'tech@airasia.com',
 'RedQ, Sepang, Selangor, Malaysia',
 24000, 24000, 0, 24000),

(10, 1, 9, 'INV-2026-00007', 'PAID',
 '2026-04-01', '2026-04-15', 'MYR',
 'Analytics Team', 'CTOS Data Systems', 'analytics@ctos.com.my',
 'Menara CTOS, Bangsar South, Kuala Lumpur, Malaysia',
 22950, 22950, 22950, 0),

(11, 1, 7, 'INV-2026-00008', 'PARTIALLY_PAID',
 '2026-04-15', '2026-04-30', 'MYR',
 'Enterprise IT', 'Maxis Communications', 'enterprise@maxis.com.my',
 'Menara Maxis, KLCC, Kuala Lumpur, Malaysia',
 45000, 45000, 15000, 30000),

(12, 1, 1, 'INV-2026-00009', 'ISSUED',
 '2026-05-04', '2026-05-18', 'MYR',
 'Cloud Procurement Team', 'PETRONAS Digital', 'cloud@petronas.com',
 'KLCC Tower, Kuala Lumpur, Malaysia',
 80000, 80000, 0, 80000),

(13, 1, 8, 'INV-2026-00010', 'CANCELLED',
 '2026-03-01', '2026-03-15', 'MYR',
 'Technology Division', 'AirAsia Digital', 'tech@airasia.com',
 'RedQ, Sepang, Selangor, Malaysia',
 60000, 60000, 0, 0),

-- IBM — additional invoices (IDs 14–19)
(14, 2, 10, 'INV-2026-00004', 'DRAFT',
 '2026-05-06', '2026-05-21', 'MYR',
 'Group Technology', 'CIMB Group Holdings', 'tech@cimb.com',
 'Menara CIMB, KL Sentral, Kuala Lumpur, Malaysia',
 65000, 65000, 0, 65000),

(15, 2, 11, 'INV-2026-00005', 'PAID',
 '2026-04-01', '2026-04-15', 'MYR',
 'Infrastructure Division', 'Petronas ICT Sdn Bhd', 'infra@petronas-ict.com.my',
 'Petronas Twin Towers, Kuala Lumpur, Malaysia',
 50000, 50000, 50000, 0),

(16, 2, 12, 'INV-2026-00006', 'ISSUED',
 '2026-04-20', '2026-05-05', 'MYR',
 'Digital Innovation', 'Sime Darby Technology Centre', 'digital@simedarby.com',
 'Ara Damansara, Petaling Jaya, Selangor, Malaysia',
 40000, 40000, 0, 40000),

(17, 2, 10, 'INV-2026-00007', 'PARTIALLY_PAID',
 '2026-04-10', '2026-04-25', 'MYR',
 'Group Technology', 'CIMB Group Holdings', 'tech@cimb.com',
 'Menara CIMB, KL Sentral, Kuala Lumpur, Malaysia',
 70000, 70000, 30000, 40000),

(18, 2, 4, 'INV-2026-00008', 'ISSUED',
 '2026-05-05', '2026-05-19', 'MYR',
 'Data & AI Division', 'Malayan Banking Berhad', 'ai@maybank.com',
 'Menara Maybank, Kuala Lumpur, Malaysia',
 90000, 90000, 0, 90000),

(19, 2, 12, 'INV-2026-00009', 'CANCELLED',
 '2026-03-10', '2026-03-25', 'MYR',
 'Digital Innovation', 'Sime Darby Technology Centre', 'digital@simedarby.com',
 'Ara Damansara, Petaling Jaya, Selangor, Malaysia',
 55000, 55000, 0, 0);

-- =========================================================
-- INVOICE ITEMS (MULTI-QUANTITY)
-- =========================================================
INSERT INTO invoice_items (invoice_id, name, description, unit_price, quantity, subtotal) VALUES
(1, 'Microsoft 365 Business Standard', 'Annual license, per user', 459, 120, 55080),

(2, 'AI Copilot Integration', 'Enterprise workflow automation', 60000, 1, 60000),
(2, 'Microsoft 365 Licenses', 'Annual license, per user', 500, 50, 25000),

(3, 'Microsoft 365 Licenses', 'Annual license, per user', 459, 10, 4590),

(4, 'IBM Watson AI Deployment', 'Analytics and automation', 70000, 1, 70000),

(5, 'Hybrid Cloud Infrastructure Setup', NULL, 50000, 1, 50000),

(6, 'Cybersecurity Monitoring', 'Monitoring and response service', 30000, 1, 30000),

(7,  'API Integration Project', 'Cancelled', 20000, 1, 20000),

-- Microsoft additional invoice items
(8,  'Azure DevOps Setup', 'CI/CD pipeline configuration', 25000, 1, 25000),

(9,  'Power BI Premium License', 'Per-user, annual', 1200, 20, 24000),

(10, 'Microsoft 365 Business Standard', 'Annual license, per user', 459, 50, 22950),

(11, 'Dynamics 365 CRM Implementation', 'Implementation and configuration', 45000, 1, 45000),

(12, 'Azure Cloud Migration', 'Compute, database, and networking', 80000, 1, 80000),

(13, 'AI Copilot Integration', 'Enterprise workflow automation (cancelled)', 60000, 1, 60000),

-- IBM additional invoice items
(14, 'IBM Cloud Pak for Data', 'Unified AI and analytics platform', 65000, 1, 65000),

(15, 'Hybrid Cloud Infrastructure', 'Design and implementation', 50000, 1, 50000),

(16, 'IBM Security QRadar SIEM', 'Deployment and configuration', 40000, 1, 40000),

(17, 'IBM Watson AI Deployment', 'Enterprise analytics', 70000, 1, 70000),

(18, 'Mainframe Modernization', 'Cloud-native architecture migration', 90000, 1, 90000),

(19, 'Data Lake Platform Setup', 'Analytics integration (cancelled)', 55000, 1, 55000);

-- =========================================================
-- PAYMENTS
-- =========================================================
INSERT INTO payments (
  company_id, invoice_id, receipt_number, amount,
  payment_date, method, reference
)
VALUES
(1, 1,  'REC-2026-00001', 55080, '2026-05-02', 'BANK_TRANSFER', 'MS-TXN-001'),
(1, 2,  'REC-2026-00002', 20000, '2026-05-03', 'BANK_TRANSFER', 'MS-TXN-002'),
(2, 4,  'REC-2026-00001', 70000, '2026-05-02', 'BANK_TRANSFER', 'IBM-TXN-001'),
(2, 6,  'REC-2026-00002', 10000, '2026-05-04', 'CARD',          'IBM-TXN-002'),
-- payments for new invoices
(1, 10, 'REC-2026-00003', 22950, '2026-04-05', 'BANK_TRANSFER', 'MS-TXN-003'),
(1, 11, 'REC-2026-00004', 15000, '2026-04-20', 'ONLINE_TRANSFER', 'MS-TXN-004'),
(2, 15, 'REC-2026-00003', 50000, '2026-04-07', 'BANK_TRANSFER', 'IBM-TXN-003'),
(2, 17, 'REC-2026-00004', 30000, '2026-04-15', 'BANK_TRANSFER', 'IBM-TXN-004');

-- Set paid_at on PAID invoices so dashboard revenue queries match
UPDATE invoices SET paid_at = '2026-05-02' WHERE id = 1;
UPDATE invoices SET paid_at = '2026-05-02' WHERE id = 4;
UPDATE invoices SET paid_at = '2026-04-05' WHERE id = 10;
UPDATE invoices SET paid_at = '2026-04-07' WHERE id = 15;

-- =========================================================
-- QUOTATIONS
-- =========================================================
INSERT INTO quotations (
  id, company_id, customer_id, quotation_number, status,
  issue_date, valid_until,
  customer_name, customer_company, customer_email, billing_address,
  subtotal, grand_total
)
VALUES
(1, 1, 1, 'QUO-2026-00001', 'ISSUED',
 '2026-05-01', '2026-05-31',
 'Cloud Procurement Team', 'PETRONAS Digital', 'cloud@petronas.com',
 'KLCC Tower, Kuala Lumpur, Malaysia',
 40000, 40000),

(2, 2, 5, 'QUO-2026-00001', 'ACCEPTED',
 '2026-05-01', '2026-05-31',
 'Infrastructure Ops', 'Telekom Malaysia', 'infra@tm.com.my',
 'Menara TM, Kuala Lumpur, Malaysia',
 55000, 55000),

(3, 1, 2, 'QUO-2026-00002', 'DRAFT',
 '2026-05-05', '2026-06-05',
 'Platform Engineering', 'Grab Holdings', 'platform@grab.com',
 'Mid Valley, Kuala Lumpur, Malaysia',
 20000, 20000),

-- Microsoft — additional quotations (IDs 4–7)
(4, 1, 7, 'QUO-2026-00003', 'DRAFT',
 '2026-05-06', '2026-06-06',
 'Enterprise IT', 'Maxis Communications', 'enterprise@maxis.com.my',
 'Menara Maxis, KLCC, Kuala Lumpur, Malaysia',
 25000, 25000),

(5, 1, 9, 'QUO-2026-00004', 'ACCEPTED',
 '2026-04-05', '2026-05-05',
 'Analytics Team', 'CTOS Data Systems', 'analytics@ctos.com.my',
 'Menara CTOS, Bangsar South, Kuala Lumpur, Malaysia',
 80000, 80000),

(6, 1, 8, 'QUO-2026-00005', 'EXPIRED',
 '2026-03-01', '2026-03-31',
 'Technology Division', 'AirAsia Digital', 'tech@airasia.com',
 'RedQ, Sepang, Selangor, Malaysia',
 45000, 45000),

(7, 1, 2, 'QUO-2026-00006', 'REJECTED',
 '2026-04-10', '2026-05-10',
 'Platform Engineering', 'Grab Holdings', 'platform@grab.com',
 'Mid Valley, Kuala Lumpur, Malaysia',
 36000, 36000),

-- IBM — additional quotations (IDs 8–11)
(8, 2, 10, 'QUO-2026-00002', 'DRAFT',
 '2026-05-06', '2026-06-06',
 'Group Technology', 'CIMB Group Holdings', 'tech@cimb.com',
 'Menara CIMB, KL Sentral, Kuala Lumpur, Malaysia',
 65000, 65000),

(9, 2, 12, 'QUO-2026-00003', 'ISSUED',
 '2026-05-01', '2026-05-31',
 'Digital Innovation', 'Sime Darby Technology Centre', 'digital@simedarby.com',
 'Ara Damansara, Petaling Jaya, Selangor, Malaysia',
 90000, 90000),

(10, 2, 11, 'QUO-2026-00004', 'EXPIRED',
 '2026-03-01', '2026-03-31',
 'Infrastructure Division', 'Petronas ICT Sdn Bhd', 'infra@petronas-ict.com.my',
 'Petronas Twin Towers, Kuala Lumpur, Malaysia',
 40000, 40000),

(11, 2, 6, 'QUO-2026-00005', 'ACCEPTED',
 '2026-04-01', '2026-04-30',
 'Security Operations', 'Axiata Group', 'security@axiata.com',
 'Axiata Tower, Kuala Lumpur, Malaysia',
 85000, 85000);

-- =========================================================
-- QUOTATION ITEMS
-- =========================================================
INSERT INTO quotation_items (
  quotation_id, name, description, unit_price, quantity, subtotal
)
VALUES
(1,  'Security Compliance Setup', 'Enterprise security compliance and monitoring', 40000, 1, 40000),
(2,  'Data Lake Platform Setup', 'Analytics integration', 55000, 1, 55000),
(3,  'API Integration Services', NULL, 20000, 1, 20000),
-- Microsoft additional quotation items
(4,  'Azure DevOps Setup', 'CI/CD pipeline configuration', 25000, 1, 25000),
(5,  'Azure Cloud Migration', 'Compute, database, and networking', 80000, 1, 80000),
(6,  'Dynamics 365 CRM Implementation', NULL, 45000, 1, 45000),
(7,  'Power BI Premium License', 'Per-user, annual', 1200, 30, 36000),
-- IBM additional quotation items
(8,  'IBM Cloud Pak for Data', 'Unified AI and analytics platform', 65000, 1, 65000),
(9,  'Mainframe Modernization', 'Cloud-native architecture migration', 90000, 1, 90000),
(10, 'IBM Security QRadar SIEM', 'Deployment and configuration', 40000, 1, 40000),
(11, 'Cybersecurity Intelligence', 'Threat response service', 30000, 1, 30000),
(11, 'Data Lake Platform Setup', 'Analytics integration', 55000, 1, 55000);