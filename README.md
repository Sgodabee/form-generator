# Form Generator System
**Intelligent Reporting (IR)** — Full Stack Developer Assessment

---

## Overview

This system reads a CSV file from a local input store, maps the data fields to a generated PDF, and delivers the PDF to two destinations simultaneously:

1. **Local file store** — on-premise output directory
2. **Amazon S3** (via LocalStack Docker container) — cloud storage POC

The transfer time to each destination is compared and logged to the system console after every run. All user activity is audited asynchronously via RabbitMQ and persisted to a PostgreSQL database.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 17 + Spring Boot 3.2 |
| Frontend | Angular 17 (standalone components) |
| Database | PostgreSQL 15 |
| Message Broker | RabbitMQ 3.12 |
| Cloud Storage (POC) | Amazon S3 via LocalStack 3.0 (Docker) |
| PDF Generation | iText 8 |
| Build | Maven 3.9 (backend) · npm + Angular CLI 17 (frontend) |
| Version Control | GitLab |

---

## Project Structure

```
form-generator/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ir/formgenerator/
│   │   │   │   ├── FormGeneratorApplication.java   ← entry point
│   │   │   │   ├── config/                         ← S3Config, RabbitMQConfig, SecurityConfig
│   │   │   │   ├── controller/                     ← REST controllers + GlobalExceptionHandler
│   │   │   │   ├── dto/                            ← AuditLogDto, GenerationResultDto
│   │   │   │   ├── model/                          ← AuditLog, FormRecord, GenerationResult
│   │   │   │   ├── queue/                          ← AuditMessage, AuditProducer, AuditConsumer
│   │   │   │   ├── repository/                     ← AuditLogRepository (Spring Data JPA)
│   │   │   │   └── service/                        ← interfaces + impl/
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── csv-input/                      ← sample_data.csv (input)
│   │   │       └── pdf-output/                     ← generated PDFs (local store)
│   │   └── test/
│   │       ├── java/com/ir/formgenerator/
│   │       │   ├── controller/                     ← FormGenerationControllerTest
│   │       │   ├── queue/                          ← AuditConsumerTest
│   │       │   └── service/                        ← 6 service unit test classes
│   │       └── resources/
│   │           └── application-test.properties     ← H2 in-memory, RabbitMQ excluded
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── app.component.ts                    ← root (router-outlet only)
│   │   │   ├── app.config.ts                       ← providers: router, HttpClient + interceptor
│   │   │   ├── app.routes.ts                       ← lazy-loaded routes + authGuard
│   │   │   ├── core/
│   │   │   │   ├── guards/                         ← auth.guard.ts
│   │   │   │   ├── interceptors/                   ← auth.interceptor.fn.ts
│   │   │   │   ├── models/                         ← audit-log.model.ts, generation-result.model.ts
│   │   │   │   └── services/                       ← auth.service.ts, form-generation.service.ts, audit.service.ts
│   │   │   └── pages/
│   │   │       ├── login/                          ← LoginComponent
│   │   │       └── dashboard/                      ← DashboardComponent
│   │   ├── environments/
│   │   │   ├── environment.ts                      ← dev: http://localhost:8080/api
│   │   │   └── environment.prod.ts                 ← prod: /api
│   │   ├── index.html
│   │   ├── main.ts
│   │   └── styles.scss                             ← global styles
│   ├── angular.json
│   ├── tsconfig.json
│   ├── tsconfig.app.json
│   └── package.json
│
├── localstack-init/
│   └── 01-create-bucket.sh                         ← creates S3 bucket on LocalStack startup
├── docker-compose.yml                              ← PostgreSQL + RabbitMQ + LocalStack
└── README.md
```

---

## Prerequisites

| Tool | Version |
|---|---|
| Docker Desktop | latest |
| Java | 17 or later |
| Maven | 3.9+ |
| Node.js | 18+ |
| npm | bundled with Node.js |
| Angular CLI | 17 — `npm install -g @angular/cli` |

---

## Setup & Running

### 1. Start infrastructure (Docker)

```bash
docker-compose up -d
```

This starts three containers:

| Container | Port | Notes |
|---|---|---|
| PostgreSQL 15 | `5432` | database: `formgenerator`, user: `formuser` |
| RabbitMQ 3.12 | `5672` / `15672` | management UI: http://localhost:15672 (guest / guest) |
| LocalStack S3 | `4566` | bucket `form-generator-bucket` created automatically |

### 2. Run the backend

```bash
cd backend
mvn spring-boot:run
```

API available at `http://localhost:8080`.

### 3. Run the frontend

```bash
cd frontend
npm install
ng serve
```

UI available at `http://localhost:4200`.

### 4. Log in

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN, USER |
| `user` | `user123` | USER |

---

## REST API Endpoints

All endpoints require HTTP Basic authentication.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/auth/me` | Returns the authenticated user's username |
| `POST` | `/api/auth/logout` | Ends the current session |
| `POST` | `/api/forms/generate` | Triggers the CSV → PDF generation pipeline |
| `GET` | `/api/files` | Lists all generated PDF file names |
| `GET` | `/api/audit` | Returns all audit log entries (newest first) |

---

## Running Tests

```bash
cd backend
mvn test
```

Tests run fully in-process — no running PostgreSQL, RabbitMQ, or LocalStack is required:

- **JUnit 5** for test structure
- **Mockito** for mocking all collaborators
- **H2 in-memory** database (activated via `application-test.properties`)
- **Spring MockMvc** for controller-layer slice tests
- **RabbitMQ auto-configuration excluded** in the test profile

### Test Coverage Summary

| Test Class | Tests | What it covers |
|---|---|---|
| `CsvReaderServiceImplTest` | 7 | CSV parsing, header mapping, empty file, missing fields, error handling |
| `PdfGeneratorServiceImplTest` | 5 | Byte array output, `%PDF` magic header, empty records, large datasets |
| `LocalFileStoreServiceTest` | 8 | File write, content verify, list, PDF-only filter, sorted order, overwrite |
| `FormGenerationServiceImplTest` | 9 | Full pipeline orchestration, collaborator interactions, error propagation |
| `AuditServiceImplTest` | 5 | Queue publish, correct message fields, no direct DB call on publish |
| `ConsoleTransferSpeedLoggerTest` | 5 | Local faster, S3 faster, equal, zero, large values |
| `FormGenerationControllerTest` | 3 | HTTP 200 (authenticated), 401 (unauthenticated), 500 (service error) |
| `AuditConsumerTest` | 3 | Message consumed, correct fields persisted, timestamp set |
| **Total** | **45** | |

---

## SOLID Design Principles

All five SOLID principles are deliberately applied throughout the codebase:

| Principle | Where applied |
|---|---|
| **S — Single Responsibility** | Each class has exactly one reason to change. `CsvReaderServiceImpl` only reads CSV. `PdfGeneratorServiceImpl` only generates PDFs. `AuditConsumer` only persists queue messages. `ConsoleTransferSpeedLogger` only logs timing. |
| **O — Open / Closed** | `FileStoreService` is the extension point for storage. Adding Azure Blob or GCS requires only a new `@Service` implementation — `FormGenerationServiceImpl` requires zero changes. |
| **L — Liskov Substitution** | `LocalFileStoreService` and `S3FileStoreService` are fully interchangeable implementations of `FileStoreService`. Any caller works correctly with either without knowing which is injected. |
| **I — Interface Segregation** | Service interfaces are small and focused: `CsvReaderService`, `PdfGeneratorService`, `FileStoreService`, `AuditService`, and `TransferSpeedLogger` each declare only the methods relevant to that single concern. |
| **D — Dependency Inversion** | `FormGenerationServiceImpl` (high-level orchestration policy) depends entirely on interfaces. Spring injects the correct concrete beans at runtime via `@Qualifier`. |

---

## Version Control — GitLab

### Why version control?

1. **Source code protection** — the original codebase was lost due to a hard-drive failure. A remote GitLab repository guarantees every committed change is stored off-machine and replicated, making that kind of loss impossible.
2. **Collaboration** — four developers can work in parallel, raise merge requests, and review each other's code before anything reaches the main branch. Every change has a traceable author and timestamp.

### Branching Strategy — GitFlow

With a team of four, **GitFlow** is recommended because it provides clear lane separation between ongoing feature work, release stabilisation, and emergency production fixes.

```
main          ← production-ready code only; tagged on every release
develop       ← integration branch; all features merge here first
feature/*     ← one short-lived branch per feature/ticket
release/*     ← release stabilisation (version bumps, final QA)
hotfix/*      ← urgent production fixes branched directly off main
```

**Why GitFlow for this team?**

| Concern | How GitFlow addresses it |
|---|---|
| 4 developers working in parallel | Each works on an isolated `feature/` branch — no conflicts until merge time |
| Stable production deployments | `main` is never committed to directly — only tested, approved code arrives there |
| Releasing without blocking features | `release/` branches let QA sign off while `develop` continues receiving new work |
| Production hotfixes | `hotfix/` branches off `main` mean a critical bug can ship without pulling in unfinished features |

### Typical workflow

```bash
# Start a new feature
git checkout develop
git pull origin develop
git checkout -b feature/pdf-generator

# Work and commit often with descriptive messages
git add src/...
git commit -m "feat: implement iText8 PDF generation from FormRecord list"

# Push and raise a Merge Request targeting develop
git push origin feature/pdf-generator
# → open MR on GitLab → peer review → squash & merge

# Cut a release
git checkout -b release/1.0.0 develop
# bump version in pom.xml / package.json, final fixes
git checkout main && git merge --no-ff release/1.0.0 && git tag v1.0.0
git checkout develop && git merge --no-ff release/1.0.0
git branch -d release/1.0.0
```

---

## Cloud Storage — Local vs Amazon S3

### Transfer speed comparison

After every generation run the backend logs a side-by-side comparison:

```
=== Transfer Speed Comparison ===
  File            : form_20260904_143012.pdf
  Local store     : 3 ms
  Amazon S3 store : 87 ms
  Result          : Local was faster by 84 ms
=================================
```

The local write is typically faster in a localhost/LAN environment because S3 uploads incur HTTP round-trip overhead to the LocalStack container. In a production deployment where the backend runs inside AWS (e.g. EC2 or ECS), S3 write times drop to single-digit milliseconds and the gap narrows significantly.

### Pros and Cons

| | Local (On-Premise) Store | Amazon S3 (Cloud) |
|---|---|---|
| **Pros** | Very low latency on the same machine or LAN | Virtually unlimited, elastic storage capacity |
| | No network dependency for writes | 99.999999999% durability — built-in replication |
| | Zero per-GB cost | Accessible from any location with credentials |
| | Simple setup — just a filesystem path | Fine-grained access control via IAM and bucket policies |
| **Cons** | Single point of failure — disk failure means data loss | Network latency adds overhead vs a local disk write |
| | Storage capacity capped by hardware | Per-GB egress costs for large data volumes |
| | Manual backup strategy required | Requires AWS account and credential management |
| | Not reachable outside the network without VPN | Cold-start overhead for infrequently accessed objects |

### Recommendation

For IR's form generator the recommended pattern is the one already implemented — **dual write**:

- Use **S3 as the primary long-term archive** — durability, off-site redundancy, and cross-office accessibility.
- Keep the **local store as a hot delivery target** — fast, synchronous access for the client pick-up folder.

This gives the business both speed and resilience without choosing one over the other.

---

## Optional Extra — Async Audit Logging via Queue

### What was implemented

Audit logs are **never written synchronously** inside the REST request cycle. The flow is:

```
POST /api/forms/generate
        │
        ├─ generate PDF
        ├─ save to local store
        ├─ save to S3
        ├─ AuditProducer.publish(message) → RabbitMQ [audit.queue]   ← returns instantly
        └─ HTTP 200 returned to client

                    (background thread)
                    AuditConsumer.consume(message)
                            └─ auditLogRepository.save(auditLog) → PostgreSQL
```

### Why a queue for auditing?

| Concern | Without queue | With queue |
|---|---|---|
| **Response time** | Every request blocks on a synchronous DB insert | Request returns in microseconds regardless of DB load |
| **Throughput under load** | DB write latency directly degrades API response times | RabbitMQ absorbs traffic bursts; DB drains at its own pace |
| **Resilience** | A slow or unavailable DB slows or breaks every user request | Queue buffers messages; the generation operation is unaffected |
| **Scalability** | More concurrent users = more concurrent DB writes = contention | More messages simply queue up; horizontal consumer scaling is straightforward |
| **Decoupling** | An audit failure can crash the core business operation | Audit failure is completely isolated from the generation pipeline |

As user volume grows, this pattern becomes increasingly valuable — the audit feature becomes a true background concern that never competes for DB connections with the operations users actually care about.
