# SmartDoc - AI-Powered Document Management

## Project Overview
SmartDoc is a microservices-based application designed to streamline document management and analysis using Artificial Intelligence. It allows users to upload documents, automatically tag and categorize them, and perform Q&A against the document content. This milestone establishes the technical foundation with a working Spring Boot backend and a React frontend, demonstrating end-to-end integration.

## Repository Structure
- `client/`: React-based frontend application for user interaction (Initial scaffolding).
- `server/services/document-service/`: Spring Boot microservice handling document logic and REST API.
- `infra/`: Docker Compose configuration for local environment setup.
- `.github/workflows/`: CI/CD pipelines for automated testing and building.

## Team Members & Roles
| Name | GitHub Username | Primary Subsystem |
|------|-----------------|-------------------|
| Hasan | [username1] | Backend: Spring Boot REST API & Document Service |
| Egor | [username2] | Frontend: React Client & API Integration |
| Gledis | [username3] | DevOps: Docker, CI/CD & Project Structure |

## Local Setup Instructions

### Prerequisites
- Java 17 or higher
- Node.js 20 or higher
- Maven 3.9+
- Docker & Docker Compose (optional, for future persistence)

### Running the Server
1. Navigate to the document service directory:
   ```bash
   cd server/services/document-service
   ```
2. Build and run with Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
3. The server will be available at `http://localhost:8080`

### Running the Client
1. Navigate to the client directory:
   ```bash
   cd client
   ```
2. (Pending implementation) Install dependencies and start:
   ```bash
   npm install
   npm run dev
   ```
3. The client will be available at `http://localhost:3000`

### Running with Docker (Alternative)
1. Navigate to the infra directory:
   ```bash
   cd infra
   ```
2. Run:
   ```bash
   docker-compose up --build
   ```

## REST Endpoints (Document Service)
All endpoints are prefixed with `/api/v1/documents`.

- `GET /`: Returns a paginated list of documents.
- `GET /all`: Returns a list of all documents (Mock data).
- `GET /{id}`: Returns a specific document by its ID.

---
**Milestone Tasks:**
- Task 3.3: List View / B
- Task 6.1: Workflow
