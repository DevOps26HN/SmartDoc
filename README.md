# SmartDoc — AI-Powered Document Management

SmartDoc is a microservices-based application for document management and analysis powered by Artificial Intelligence. Users can upload documents, have them automatically tagged and categorized, and run Q&A against their content.

This repository contains the full source code, infrastructure as code, and configuration management required to run SmartDoc both locally and on Microsoft Azure.

---

## Repository Structure
```
.
├── client/                              # React-based frontend application
├── server/
│   └── services/
│       └── document-service/            # Spring Boot microservice (REST API)
├── infra/
│   ├── terraform/                       # Azure infrastructure as code
│   └── ansible/                         # VM configuration and deployment playbooks
├── docker-compose.yml                   # Local and remote container orchestration
└── .github/
    └── workflows/                       # CI/CD pipelines
```

---

## Local Deployment (Docker Compose)
The entire stack can be started on a developer machine with a single command:

```bash
docker compose up --build
```

Once the containers are healthy, the application is reachable at:

- **Frontend:** [http://localhost:3000](http://localhost:3000)
- **Backend API:** [http://localhost:8080/api/v1/documents](http://localhost:8080/api/v1/documents)

### Prerequisites
- Docker & Docker Compose
- Java 17+ (optional, for local builds)
- Node.js 20+ (optional, for local builds)
- Maven 3.9+

### Component Ports
| Component | Container Port | Host Port |
|-----------|----------------|-----------|
| `client`  | 80             | 3000      |
| `server`  | 8080           | 8080      |
| `db`      | 5432           | 5432      |

---

## Cloud Deployment on Azure
The cloud deployment is fully automated and reproducible from this repository:

1. **Terraform** provisions the Azure infrastructure (resource group, network, public IP, security group, VM).
2. **Ansible** connects to the provisioned VM, installs the required runtime, copies the project files, and starts the SmartDoc containers via Docker Compose.

No manual changes are made to the VM outside of these tools, which keeps the deployment fully reproducible from source control.

### 1. Provision Infrastructure with Terraform
```bash
cd infra/terraform
terraform init
terraform plan
terraform apply
```

Terraform creates the following Azure resources:

- Resource group
- Virtual network
- Subnet
- Public IP address
- Network security group (SSH, HTTP, frontend, backend rules)
- Linux virtual machine (Ubuntu 22.04 LTS)

### 2. Configure the VM and Deploy with Ansible
```bash
cd infra/ansible
ansible-playbook playbook.yml
ansible-playbook deploy.yml
```

The Ansible playbooks perform the following tasks on the provisioned VM:

- Install Docker
- Install Docker Compose
- Copy the SmartDoc project files to the VM
- Start the SmartDoc system using Docker Compose

---

## Public Deployment URLs
After a successful Terraform + Ansible run, the application is publicly accessible at:

- **Frontend:** [http://20.86.114.210:3000](http://20.86.114.210:3000)
- **Backend API:** [http://20.86.114.210:8080/api/v1/documents](http://20.86.114.210:8080/api/v1/documents)

---

## Verification
To confirm that the deployment is healthy on the Azure VM:

```bash
ssh azureuser@20.86.114.210
sudo docker ps
curl http://localhost:8080/api/v1/documents
```

`docker ps` should list the SmartDoc containers (client, server, database) in the `Up` state, and the `curl` call should return a valid JSON response from the document service.

---

## Environment Variables
The system uses sane defaults, which can be overridden via a `.env` file (see `.env.example`).

| Variable            | Default Value                          | Description                |
|---------------------|----------------------------------------|----------------------------|
| `POSTGRES_DB`       | `smartdoc`                             | Database name              |
| `POSTGRES_USER`     | `postgres`                             | Database user              |
| `POSTGRES_PASSWORD` | `postgres`                             | Database password          |
| `DATABASE_URL`      | `jdbc:postgresql://db:5432/smartdoc`   | JDBC connection string     |

---

## Cleanup
To tear down the Azure infrastructure and avoid further resource consumption:

```bash
cd infra/terraform
terraform destroy
```

This removes every resource created by Terraform, leaving no leftover infrastructure in the Azure subscription.

---

## Git Workflow
All changes to this repository follow a peer-reviewed workflow:

- No direct commits to `main`.
- Every change is developed on a dedicated **feature branch**.
- Feature branches are merged into `main` exclusively through a **pull request**.
- Each pull request must receive at least one **peer review** approval before it can be merged.

This ensures that the `main` branch always reflects a reviewed, reproducible state of the project.
