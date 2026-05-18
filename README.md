# SmartDoc - AI-Powered Document Management

## Project Overview
SmartDoc is a microservices-based application designed to streamline document management and analysis using Artificial Intelligence. It allows users to upload documents, automatically tag and categorize them, and perform Q&A against the document content.

## Repository Structure
- `client/`: React-based frontend application for user interaction.
- `server/services/document-service/`: Spring Boot microservice handling document logic and REST API.
- `infra/`: Docker Compose configuration and infrastructure notes.
- `.github/workflows/`: CI/CD pipelines for automated testing and building.

## Quick Start (Docker Compose)
The entire system can be started with a single command:
```bash
docker compose up --build
```
- **Client:** `http://localhost:3000`
- **Document Service API:** `http://localhost:8080/api/v1/documents`

## Local Development Setup

### Prerequisites
- Docker & Docker Compose
- Java 17+ (optional for local builds)
- Node.js 20+ (optional for local builds)
- Maven 3.9+

### Environment Variables
The system uses sane defaults, but you can override them in a `.env` file (see `.env.example`):
| Variable | Default Value | Description |
|----------|---------------|-------------|
| `POSTGRES_DB` | `smartdoc` | Database name |
| `POSTGRES_USER` | `postgres` | Database user |
| `POSTGRES_PASSWORD` | `postgres` | Database password |
| `DATABASE_URL` | `jdbc:postgresql://db:5432/smartdoc` | JDBC connection string |

## Component Ports
| Component | Container Port | Host Port |
|-----------|----------------|-----------|
| `client` | 80 | 3000 |
| `server` | 8080 | 8080 |
| `db` | 5432 | 5432 |
