# RUNNING KH3T Microservices (local)

Short guide to start the backend services locally.

## Prerequisites
- Docker & Docker Compose
- Java 17+ (for local `mvnw` runs)
- Windows: PowerShell or CMD

## Recommended: start everything with Docker Compose
Build and run infrastructure + microservices (may take a while on first run):

```bash
docker compose -f docker-compose.yml -f docker-compose.services.yml up -d --build --remove-orphans
```

Notes:
- This uses the root `docker-compose.yml` (infra) plus `docker-compose.services.yml` (service images).
- Ensure Docker has enough memory (>= 4GB) for Kafka/MySQL.

## Quick dev: run all services using the bundled script (Windows)
The repository includes a helper that opens each service in its own CMD window.

PowerShell/CMD (from repo root):

```powershell
.\start-services.bat
```

Files:
- `start-services.bat` — runs each service via the Maven wrapper (`mvnw.cmd`).

## Run a single service (dev)
Replace `<module>` with one of: `kh3t-discovery`, `kh3t-identity-service`, `kh3t-catalog-service`, `kh3t-order-service`, `kh3t-gateway`.

```powershell
.\kh3tshop-be\mvnw.cmd -f .\kh3tshop-microservices\<module>\pom.xml spring-boot:run
```

Example (discovery):

```powershell
.\kh3tshop-be\mvnw.cmd -f .\kh3tshop-microservices\kh3t-discovery\pom.xml spring-boot:run
```

## Recommended start order
1. Infrastructure (Docker): MySQL, Redis, Zookeeper, Kafka (if using Docker Compose)
2. `kh3t-discovery` (Eureka)
3. Backends: `kh3t-identity-service`, `kh3t-catalog-service`, `kh3t-order-service`
4. `kh3t-gateway` (last)

## Ports (default)
- Eureka: http://localhost:8761
- Gateway: http://localhost:8080
- Identity: http://localhost:8081
- Catalog: http://localhost:8082
- Order: http://localhost:8083
- Kafka: 9092
- Redis: 6379
- MySQL: 3306

## Create required databases (if not present)
If MySQL is running in Docker, create DBs once:

```powershell
docker exec -i kh3t-mysql mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS kh3t_catalog; CREATE DATABASE IF NOT EXISTS kh3t_identity; CREATE DATABASE IF NOT EXISTS kh3t_order;"
```

## Verify services and health
- Eureka registry (raw XML):

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8761/eureka/apps
```

- Actuator health endpoints (example):

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
```

If a call returns 400/404, verify `management.endpoints.web.exposure.include` in the service `application.yml`/`application.properties`.

## Quick smoke test (login + create order)
1. Obtain token (Identity) — example POST (adjust payload to your user):

```bash
curl -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" -d '{"username":"user","password":"pass"}'
```

2. Use token to call order API via Gateway (example):

```bash
curl -X POST http://localhost:8080/api/orders -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" -d '{"customerId":1, "items": [...] }'
```

Adjust endpoints/payloads to match your project APIs.

## Logs & troubleshooting
- Docker logs:

```bash
docker compose logs -f kh3t-mysql kh3t-kafka kh3t-zookeeper kh3t-redis
```

- If using `start-services.bat`, each service runs in its own CMD window; inspect those windows for startup traces and stacktraces.
- Common issues:
  - Missing `kh3t-common` during Docker image build: ensure Dockerfile copies `kh3t-common` and runs `mvn -f kh3t-common/pom.xml install` (already patched in repo).
  - Eureka connection refused: start `kh3t-discovery` first and confirm `http://localhost:8761` is reachable.
  - Kafka not available: confirm Docker container `kh3t-kafka` is running and listening on 9092.

## Stop
- Docker Compose:

```bash
docker compose -f docker-compose.yml -f docker-compose.services.yml down
```

- Kill local runs: close CMD windows or terminate the `mvnw` Java processes.

---
If you want, I can also add a short `README` section with example payloads for the auth and order endpoints — tell me which endpoints/payload fields you prefer.
