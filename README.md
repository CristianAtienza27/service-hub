# Service Hub

Multi-tenant SaaS platform for small service businesses to receive and manage customer requests.

## Project status

Early validation and MVP development.

## Initial target market

Tattoo studios.

## Main MVP flow

1. A business registers.
2. The platform generates a public business page.
3. A customer submits a service request.
4. The business reviews the request from its private dashboard.
5. The business updates the request status.

## Technology stack

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Docker

### Frontend

- Angular
- Angular Material

### Architecture

- Modular monolith
- Multi-tenant data model
- REST API

## Repository structure

```text
service-hub/
├── backend/
├── frontend/
├── infrastructure/
└── docs/