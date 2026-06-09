# 🛒 KH3T Shop - Microservices E-Commerce Platform

A production-ready, highly scalable **E-Commerce Platform** built with **Microservices Architecture** using Spring Boot, Spring Cloud, and React. The system utilizes modern design patterns (CQRS, SAGA, Pipeline) to handle complex workflows and high-traffic shopping scenarios.

---

## 🏗️ System Architecture

The system consists of independent microservices communicating via REST (Synchronous) and Message Brokers (Asynchronous):

```
                      +-------------------+
                      |   React Frontend  |
                      +---------+---------+
                                |
                                v
                      +-------------------+
                      |    API Gateway    |
                      +---------+---------+
                                |
        +-----------------------+-----------------------+
        |                       |                       |
        v                       v                       v
+---------------+       +---------------+       +---------------+
|   Identity    |       |    Catalog    |       |     Order     |
|    Service    |       |    Service    |       |    Service    |
+-------+-------+       +-------+-------+       +-------+-------+
        |                       |                       |
        v                       v                       v
    PostgreSQL               MariaDB                PostgreSQL
        +                       +                       +
   Redis Cache             Redis Cache             RabbitMQ/Kafka
        |                       |                       |
        +-----------------------+-----------------------+
                                |
                                v
                        +---------------+
                        |   AI Service  | (Gemini AI Assistant)
                        +---------------+
```

---

## 🛠️ Technology Stack

| Layer | Technology | Description |
|---|---|---|
| **Core Backend** | Java 17, Spring Boot 3.x | Base framework for all microservices |
| **Microservices** | Spring Cloud Netflix Eureka | Service Registry & Discovery |
| **Databases** | MariaDB, PostgreSQL | Relational databases for products & orders |
| **Caching** | Redis | Session management & high-performance product query cache |
| **Message Broker** | RabbitMQ / Apache Kafka | Event-driven communication & SAGA pattern |
| **AI Assistant** | Google Gemini API | Integrated for shopping chatbots and post-processing |
| **Security** | Spring Security + OAuth2 Resource Server | Stateless authentication with JWT validation |
| **Containerization** | Docker, Docker Compose | Infrastructure orchestration |
| **Frontend** | ReactJS + Tailwind CSS | Interactive user interface |

---

## 📐 Applied Design Patterns & Best Practices

### 1. CQRS (Command Query Responsibility Segregation)
Applied in `kh3t-catalog-service` to separate the write side (Insert/Update/Delete product actions) from the read side (Query/Search actions) to optimize database throughput and load times.

### 2. Pipeline Pattern (Pipe & Filter)
Decoupled complex product creation and update logic into a linear execution chain:
$$\text{Request} \rightarrow \text{ValidationFilter} \rightarrow \text{EnrichmentFilter} \rightarrow \text{PersistenceFilter}$$
This pattern avoids massive monolithic service methods and makes filters highly testable.

### 3. Observer Pattern (Event-Driven Caching)
Using Spring `ApplicationEventPublisher` to publish domain events (e.g., `ProductCreatedEvent`). A detached listener (`ProductEventListener`) handles cache eviction on Redis, eliminating tight coupling between database logic and caching code.

### 4. SAGA Pattern
Orchestrates distributed transactions across order placements, inventory updates, and payment services using a message broker (RabbitMQ/Kafka) to guarantee eventual consistency.

---

## 📂 Project Structure

```
kh3tshop-microservices/
├── kh3t-identity-service/    # Authentication, JWT tokens, and user credentials
├── kh3t-catalog-service/     # Product, category, inventory management (CQRS/Pipeline)
├── kh3t-order-service/       # Shopping cart, checkout, SAGA transactions
├── kh3t-ai-service/          # Gemini AI Chatbot assistant integration
├── kh3t-common/              # Shared exception handlers, DTOs, and utility codes
└── kh3tshop-fe/              # ReactJS + Tailwind CSS frontend interface
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+** & **Maven 3.8+**
- **Node.js** (for Frontend)
- **Docker** & **Docker Compose**

### 1. Start Infrastructure (Docker)
Start the shared components (databases, cache, message broker):
```bash
docker-compose up -d
# Starts: PostgreSQL, MariaDB, Redis, RabbitMQ
```

### 2. Run Services (Backend)
Run the Discovery Server first, then boot up individual services:
```bash
# In each service directory
mvn spring-boot:run
```

### 3. Run Frontend
```bash
cd kh3tshop-fe
npm install
npm run dev
```

---

## 👥 Members (Team Size: 3)
- **Pham Ngoc Thanh** (Backend Developer) - Catalog & Product Management Service Lead.
- **Phạm Văn Hinh** (Role) - Order, Inventory & SAGA Integration.
- **Dương Thế Khánh** (Role) - Identity, API Gateway & React Frontend.
