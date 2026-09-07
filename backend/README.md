# How to Run — Form Generator System

This guide covers every step needed to get the full stack running locally:
Docker infrastructure → Spring Boot backend → Angular frontend.

---

## Prerequisites

Install the following before you start:

| Tool | Version | Download |
|---|---|---|
| **Docker Desktop** | latest | https://www.docker.com/products/docker-desktop |
| **Java JDK** | 17 or later | https://adoptium.net |
| **Maven** | 3.9+ | https://maven.apache.org/download.cgi |
| **Node.js** | 18+ | https://nodejs.org |
| **Angular CLI** | 22 | `npm install -g @angular/cli` |

Verify everything is installed:

```bash
java -version        # should print 17 or higher
mvn -version         # should print 3.9 or higher
node -version        # should print 18 or higher
ng version           # should print Angular CLI 22
docker -version      # should print a recent Docker version
```

---

## Step 1 — Start Docker Infrastructure

The `docker-compose.yml` is inside the `backend/` folder.

```bash
cd backend
docker-compose up -d
```

This starts three containers:

| Container | What it is | Port(s) |
|---|---|---|
| `formgenerator-postgres` | PostgreSQL 15 database | `5432` |
| `formgenerator-rabbitmq` | RabbitMQ 3.12 message broker | `5672` (AMQP) · `15672` (management UI) |
| `formgenerator-localstack` | LocalStack — simulates Amazon S3 | `4566` |

The LocalStack init script automatically creates the S3 bucket `form-generator-bucket` on first startup.

**Verify containers are healthy:**

```bash
docker-compose ps
```

All three containers should show `Up` or `healthy`. Allow 20–30 seconds for LocalStack to finish initialising.

**Useful management URLs (once running):**

| Service | URL | Credentials |
|---|---|---|
| RabbitMQ Management UI | http://localhost:15672 | guest / guest |
| LocalStack health check | http://localhost:4566/_localstack/health | — |

---

## Step 2 — Configure the Database Connection

The backend `application.properties` is pre-configured to connect to a PostgreSQL instance. Check which setup matches your environment:

### Option A — Use the Docker PostgreSQL container (recommended)

Open `backend/src/main/resources/application.properties` and ensure these lines are set:

```properties
server.port=8081

spring.datasource.url=jdbc:postgresql://localhost:5432/formgenerator
spring.datasource.username=formuser
spring.datasource.password=formpass
```

### Option B — Use your own local PostgreSQL installation

If you have PostgreSQL running locally on a different port or with different credentials, update the file to match. For example, the current file is configured as:

```properties
server.port=8081

spring.datasource.url=jdbc:postgresql://localhost:5433/formgenerator
spring.datasource.username=postgres
spring.datasource.password=M@goda
```

Adjust `url`, `username`, and `password` to whatever your local instance uses. Make sure the `formgenerator` database exists — create it if needed:

```sql
CREATE DATABASE formgenerator;
```

> **Note:** The app uses `spring.jpa.hibernate.ddl-auto=update`, so Hibernate will create the `audit_logs` table automatically on first startup. You do not need to run any SQL scripts.

---

## Step 3 — Run the Backend

From the `backend/` directory:

```bash
mvn spring-boot:run
```

Maven will download all dependencies on first run (this may take a few minutes). Once you see a line like:

```
Started FormGeneratorApplication in 4.2 seconds
```

the backend is ready. The API is available at:

```
http://localhost:8081
```

**To stop the backend:** press `Ctrl + C` in the terminal.

---

## Step 4 — Run the Frontend

Open a **new terminal window**, then:

```bash
cd frontend
npm install
npm start
```

`npm install` only needs to run once (or after `package.json` changes). `npm start` runs `ng serve`.

Once you see:

```
Application bundle generation complete.
Local: http://localhost:4200/
```

open your browser at:

```
http://localhost:4200
```

**To stop the frontend:** press `Ctrl + C` in the terminal.

---

## Step 5 — Log In

The app uses HTTP Basic authentication with two built-in users:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin + User |
| `user` | `user123` | User |

Enter either set of credentials on the login page and click **Log In**.

---

## Step 6 — Generate a PDF

Once logged in you will land on the dashboard. Click the **Generate PDF** button.

The backend will:
1. Read `sample_data.csv` from `backend/src/main/resources/csv-input/`
2. Generate a PDF table from the CSV records
3. Save the PDF to `backend/src/main/resources/pdf-output/` (local store — timed)
4. Upload the same PDF to the `form-generator-bucket` S3 bucket in LocalStack (timed)
5. Log a transfer speed comparison to the backend console
6. Publish an audit message to RabbitMQ → consumed and persisted to PostgreSQL

The dashboard will show:
- The generated file name
- Local and S3 destinations
- Transfer speed comparison (local ms vs S3 ms)
- An updated list of all generated PDFs
- An updated audit log table

---

## Running the Tests

Tests run fully without Docker — no database or broker needs to be running.

```bash
cd backend
mvn test
```

The test profile uses:
- **H2 in-memory database** — no PostgreSQL required
- **RabbitMQ auto-configuration excluded** — no broker required
- **Mockito mocks** for all external dependencies

---

## Stopping Everything

```bash
# Stop the frontend (in its terminal)
Ctrl + C

# Stop the backend (in its terminal)
Ctrl + C

# Stop and remove Docker containers
cd backend
docker-compose down
```

To also remove the persisted volumes (database data, S3 data):

```bash
docker-compose down -v
```

---

## Quick Reference

| Thing | Value |
|---|---|
| Backend API | http://localhost:8081 |
| Frontend UI | http://localhost:4200 |
| RabbitMQ UI | http://localhost:15672 (guest / guest) |
| LocalStack S3 | http://localhost:4566 |
| PostgreSQL port | `5432` (Docker) or `5433` (local install — see Step 2) |
| S3 bucket name | `form-generator-bucket` |
| CSV input file | `backend/src/main/resources/csv-input/sample_data.csv` |
| PDF output folder | `backend/src/main/resources/pdf-output/` |
| Login credentials | admin / admin123 · user / user123 |

---

## Common Issues

**`Could not connect to PostgreSQL`**
Check your `application.properties` datasource URL, username, and password match your running PostgreSQL instance (see Step 2).

**`Connection refused` on RabbitMQ**
The Docker container may still be starting. Run `docker-compose ps` and wait until `formgenerator-rabbitmq` shows `healthy`, then restart the backend.

**`BucketAlreadyExists` or S3 errors**
LocalStack persists state between restarts. Run `docker-compose down -v` to reset everything, then `docker-compose up -d` again.

**`ng: command not found`**
Angular CLI is not installed globally. Run `npm install -g @angular/cli` first.

**`Port 8081 already in use`**
Another process is using port 8081. Either stop that process or change `server.port` in `application.properties` and update `frontend/src/environments/environment.ts` to match.

**`Port 4200 already in use`**
Run `ng serve --port 4201` and update your browser URL accordingly.
