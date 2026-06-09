# SmartDoc — AI-Powered Document Management

SmartDoc is a microservices-based application for document management and analysis powered by Artificial Intelligence. Users can upload documents, have them automatically tagged and categorized, and run Q&A against their content.

This repository contains the full source code, infrastructure as code, and configuration management required to run SmartDoc locally, on Microsoft Azure, and on the AET Kubernetes cluster.

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
├── helm/
│   └── smartdoc/                        # Helm chart for the AET Kubernetes cluster
│       ├── Chart.yaml
│       ├── values.yaml                  # All environment-specific values
│       └── templates/                   # Deployments, Services, ConfigMaps, Secret, Ingress
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
| `db`      | 5432           | *(not exposed)* |

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

### Azure VM (Terraform + Ansible)

After a successful Terraform + Ansible run, the application is publicly accessible at:

- **Frontend:** [http://20.123.168.66:3000](http://20.123.168.66:3000)
- **Backend API:** [http://20.123.168.66:8080/api/v1/documents](http://20.123.168.66:8080/api/v1/documents)

### AET Kubernetes cluster (Helm)

After a successful `helm install`, the application is publicly accessible at:

- **Frontend:** [https://go54niq-devops26.stud.k8s.aet.cit.tum.de/](https://go54niq-devops26.stud.k8s.aet.cit.tum.de/)
- **Backend API (via client proxy):** [https://go54niq-devops26.stud.k8s.aet.cit.tum.de/api/v1/documents](https://go54niq-devops26.stud.k8s.aet.cit.tum.de/api/v1/documents)

The hostname matches `ingress.host` in [`helm/smartdoc/values.yaml`](helm/smartdoc/values.yaml). TLS is provisioned automatically via cert-manager (`letsencrypt-prod`).

---

## Kubernetes Deployment on the AET Cluster (Helm)
The same containerised system also runs on the **AET Kubernetes cluster**, packaged
as a Helm chart in [`helm/smartdoc`](helm/smartdoc). The chart deploys all three
components as Kubernetes workloads:

| Component | Workload | Service (ClusterIP) | Exposed publicly? |
|-----------|----------|---------------------|-------------------|
| `client`  | Deployment | `<release>-client:80`   | **Yes**, via the Ingress |
| `server`  | Deployment | `<release>-server:8080` | No — reached through the client's `/api` proxy |
| `db`      | Deployment + PVC | `<release>-db:5432` | No — internal only |

Only the **client** is reachable from the internet through the Ingress. The
client's nginx (configured from a templated `ConfigMap`) proxies `/api` to the
in-cluster `server` Service, and the `server` talks to the `db` Service. The
database is never exposed externally.

> The chart is fully parameterised through `values.yaml` — image repositories and
> tags, namespace, hostname, ports, replica counts and database settings. Nothing
> environment-specific is hardcoded in the templates, and **no credentials are
> committed** (see *Secrets & configuration* below).

### Prerequisites
- `kubectl` and `helm` installed locally.
- A kubeconfig / token for the AET Cluster, supplied to your environment as
  announced in Artemis. **Never commit the kubeconfig or any token** — point
  `kubectl`/`helm` at it via the `KUBECONFIG` environment variable:

  ```bash
  export KUBECONFIG=/path/to/stud.yaml
  kubectl config current-context   # should print: stud
  ```

  On Windows (PowerShell):

  ```powershell
  $env:KUBECONFIG = "C:\path\to\stud.yaml"
  kubectl config current-context
  ```

- The `server` and `client` images built and pushed to a registry the cluster can
  pull from (CI does not push images yet), for example:

  ```bash
  docker login ghcr.io -u <github-username>   # password = GitHub PAT (write:packages)

  docker build -t ghcr.io/devops26hn/smartdoc-server:0.1.0 server/services/document-service
  docker build -t ghcr.io/devops26hn/smartdoc-client:0.1.0 client
  docker push ghcr.io/devops26hn/smartdoc-server:0.1.0
  docker push ghcr.io/devops26hn/smartdoc-client:0.1.0
  ```

### 1. Create the team namespace
All workloads live in the team namespace `<tum-id>-devops26`, where `<tum-id>` is
the TUM ID of the team member running the deployment:

```bash
kubectl create namespace <tum-id>-devops26
# Example: kubectl create namespace go54niq-devops26
```

Only one team member performs the final `helm install` against the shared namespace; chart authoring is a shared effort via pull requests.

### 2. Provide secrets out-of-band (not committed)
Create the image-pull secret (required when GHCR images are private) and,
optionally, the database password secret directly in the cluster so neither ever
lands in git:

```bash
# Registry credentials for pulling the server/client images (referenced by name)
kubectl create secret docker-registry ghcr-secret \
  --namespace <tum-id>-devops26 \
  --docker-server=ghcr.io \
  --docker-username=<github-username> \
  --docker-password=<github-pat-with-write-packages> \
  --docker-email=<github-email>

# (Preferred) database password kept fully out-of-band
kubectl create secret generic smartdoc-db \
  --namespace <tum-id>-devops26 \
  --from-literal=POSTGRES_PASSWORD='<strong-password>'
```

### 3. Edit the values that change per team / environment
Before installing, edit [`helm/smartdoc/values.yaml`](helm/smartdoc/values.yaml) (or pass `--set`):

| Value | What to set |
|-------|-------------|
| `ingress.host` | Public hostname on the AET Cluster (wildcard: `*.stud.k8s.aet.cit.tum.de`) |
| `server.image.repository` / `server.image.tag` | Your pushed server image |
| `client.image.repository` / `client.image.tag` | Your pushed client image |
| `imagePullSecrets` | `[{ name: ghcr-secret }]` when the registry is private |
| `db.existingSecret` | `smartdoc-db` to use the out-of-band password secret (recommended), **or** pass `--set db.password=...` at install time |
| `db.resources` / `server.resources` / `client.resources` | **Required on the AET cluster** — each container must define `limits.cpu` and `limits.memory` (namespace quota rejects pods without limits) |

The committed `values.yaml` already targets `ghcr.io/devops26hn/smartdoc-{server,client}:0.1.0`, `ghcr-secret`, `go54niq-devops26.stud.k8s.aet.cit.tum.de`, and resource limits for all three components — adjust if your team uses different images or a different `<tum-id>`.

### 4. Install the chart
From the repository root (with `ingress.host`, images, and `imagePullSecrets` already in `values.yaml`):

```bash
helm install smartdoc helm/smartdoc \
  --namespace <tum-id>-devops26 \
  --set db.existingSecret=smartdoc-db
```

Alternatively, let the chart create the database Secret at install time (do **not** commit the real password):

```bash
helm install smartdoc helm/smartdoc \
  --namespace <tum-id>-devops26 \
  --set db.password='<strong-password>'
```

Or from the chart directory:

```bash
cd helm/smartdoc
helm install smartdoc . --namespace <tum-id>-devops26 --set db.existingSecret=smartdoc-db
```

### 5. Upgrade an existing release
After changing the chart or values, apply the change in place:

```bash
helm upgrade smartdoc helm/smartdoc --namespace <tum-id>-devops26
```

### 6. Verify the rollout
```bash
kubectl --namespace <tum-id>-devops26 get pods,svc,ingress \
  -l app.kubernetes.io/instance=smartdoc
```
All pods should reach `Running`/`Ready`, and the Ingress should list the public
host. Then open the frontend URL in a browser — the client should show documents
served by the backend (same behaviour as the Docker Compose / Azure deployment).

```bash
curl https://go54niq-devops26.stud.k8s.aet.cit.tum.de/api/v1/documents
```

### Secrets & configuration
- **Cluster access** (kubeconfig, tokens) is supplied via `KUBECONFIG` in the
  operator's environment — never committed.
- **Image-pull / registry credentials** are created out-of-band
  (`ghcr-secret`) and only referenced **by name** in `values.yaml`.
- **Application config** that differs between local and cluster is externalised:
  the JDBC URL, username and database name are rendered into a `ConfigMap`; the
  nginx upstream is rendered into a templated `ConfigMap`; the database password
  comes from a Kubernetes `Secret` (chart-managed from `--set db.password=...` or,
  preferably, an out-of-band `db.existingSecret`). No real password is stored in
  the repository.

### Tear the release down
Once grading is confirmed, free the shared cluster resources:

```bash
helm uninstall smartdoc --namespace <tum-id>-devops26
# Optionally remove the namespace entirely:
kubectl delete namespace <tum-id>-devops26
```

`helm uninstall` removes every resource the chart created — including the database
`PersistentVolumeClaim` — so a fresh `helm install` afterwards recreates an
equivalent, clean release. Secrets you created out-of-band (`ghcr-secret`,
`smartdoc-db`) are **not** owned by the chart and remain until you delete them or
the namespace.

---

## Verification

### Azure VM

```bash
ssh azureuser@20.123.168.66
sudo docker ps
curl http://localhost:8080/api/v1/documents
```

`docker ps` should list the SmartDoc containers (client, server, database) in the `Up` state, and the `curl` call should return a valid JSON response from the document service.

### AET Kubernetes cluster

```bash
kubectl --namespace <tum-id>-devops26 get pods,svc,ingress -l app.kubernetes.io/instance=smartdoc
curl https://go54niq-devops26.stud.k8s.aet.cit.tum.de/api/v1/documents
```

All SmartDoc pods should be `Running`/`Ready`, and the browser should show the client with backend data at the Ingress URL.

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
