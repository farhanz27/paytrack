# Paytrack

A multi-tenant invoicing and payment tracking app. Manage customers, invoices, quotations, and payments across multiple workspaces — with PDF exports, a product catalog, a revenue dashboard, and Pax, an AI assistant powered by Google Gemini.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| Frontend | Vue 3, Vite, PrimeVue (Aura), Vue Router, Axios |
| Database | MySQL 8 |
| PDF | AWS Lambda (Playwright + Chromium), S3, API Gateway |
| AI | Google Gemini API, Bucket4j |
| Containerization | Docker, Docker Compose |

---

## Features

- 🏢 **Multi-tenant Workspaces** — manage multiple companies, invite members, assign Owner / Admin / Member roles
- 👥 **Customer Management** — directory with contact details, billing info, and status tracking
- 🗂️ **Product & Service Catalog** — reusable price list for populating invoice and quotation line items
- 🧾 **Invoicing** — dynamic line items, live totals, and full status lifecycle (Draft → Issued → Paid)
- 📋 **Quotations** — send quotes with validity dates and track acceptance or rejection
- 💳 **Payment Tracking** — record payments by method and reference; invoices mark themselves paid automatically
- 📄 **PDF Export** — download invoices and quotations as formatted PDFs with status watermark, rendered by an AWS Lambda (Playwright + Chromium) and stored in S3
- 📊 **Dashboard** — revenue summary, outstanding invoices, and customer count per workspace
- 🔐 **Authentication** — session-based login with CSRF protection, profile and password management
- 🤖 **Pax AI Assistant** — persistent floating chat widget powered by Gemini; answers workflow questions and extracts structured invoice drafts from natural language, with per-user rate limiting and context-aware responses per page

---

## Quick start (Docker)

```bash
docker compose up --build
```

Open **http://localhost**. Demo account — email: `admin@paytrack.com` · password: `admin123`

---

## Local development

**Prerequisites:** Java 21+, Node.js 18+, MySQL 8

**1. Install Chromium (once)**
```bash
cd backend
./mvnw exec:java -e \
-Dexec.mainClass=com.microsoft.playwright.CLI \
-Dexec.args="install --with-deps chromium"
```

**2. Set up the database**
```bash
bash scripts/setup_db.sh
```

**3. Start the API (port 8080)**
```bash
cd backend
./mvnw spring-boot:run
```

**4. Start the frontend (port 5173)**
```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**.

---

## Project Structure

```
paytrack/
├── backend/                  # Spring Boot API
│   └── src/main/java/com/avantdream/paytrack/
│       ├── auth/             # Login, register, session
│       ├── company/          # Workspaces, members, invitations
│       ├── customer/         # Customer management
│       ├── catalog/          # Product & service catalog
│       ├── invoice/          # Invoices and line items
│       ├── quotation/        # Quotations
│       ├── payment/          # Payment recording
│       ├── dashboard/        # Revenue and stats
│       ├── pdf/              # PDF generation
│       ├── upload/           # File upload (logos, attachments)
│       ├── pax/              # AI assistant (Gemini integration)
│       └── shared/           # Config, exceptions, utilities
├── frontend/                 # Vue 3 SPA
│   └── src/
│       ├── api/              # Axios API modules
│       ├── views/            # Page components
│       ├── components/       # Shared UI components
│       ├── router/           # Vue Router config
│       └── auth/             # Auth composable
├── functions/
│   └── renderpdf/            # AWS Lambda — HTML→PDF via Playwright + Chromium
│       ├── infra/deploy.sh   # Deploy script (ECR, Lambda, API Gateway, S3)
│       ├── Makefile          # make deploy / make smoke-test
│       ├── .env              # AWS config and secrets (git-ignored)
│       └── docs/deploy.md    # Full deploy guide
└── scripts/                  # Database setup script
```
