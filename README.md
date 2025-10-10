# 💳 Payment & Billing Service

[![Build and Test Payment Service](https://github.com/TechTorque-2025/Payment_Service/actions/workflows/buildtest.yaml/badge.svg)](https://github.com/TechTorque-2025/Payment_Service/actions/workflows/buildtest.yaml)

This microservice handles all financial transactions, including invoice generation and payment processing via the external PayHere gateway.

**Assigned Team:** Rothila, Dinith

### 🎯 Key Responsibilities

-   **Invoice Management:** Create and manage invoices for completed work.
-   **Payment Processing:** Securely initiate payment requests for the PayHere gateway.
-   **Webhook Handling:** Handle the server-to-server notification from PayHere to securely confirm payment status and verify the `md5sig` hash.

### ⚙️ Tech Stack

-   **Framework:** Java / Spring Boot
-   **Database:** PostgreSQL
-   **Security:** Spring Security (consumes JWTs)

### ℹ️ API Information

-   **Local Port:** `8086`
-   **Swagger UI:** [http://localhost:8086/swagger-ui.html](http://localhost:8086/swagger-ui.html)

### 🚀 Running Locally

This service is designed to be run as part of the main `docker-compose` setup from the project's root directory.

```bash
# From the root of the TechTorque-2025 project
docker-compose up --build payment-service