KH3T Shop — Architecture Overview

This folder contains architecture artifacts generated from the repository structure (frontend, monolith, microservices).

Contents
- `diagrams/architecture.mmd` — Mermaid diagrams (high-level, component, deployment, sequence)
- `diagrams/architecture.puml` — PlantUML sources
- `openapi/` — OpenAPI v3 sample YAML for `auth`, `product`, `order`
- `migration_checklist.md` — Steps to migrate monolith → microservices

Quick summary
- Frontend: `kh3tshop-fe` (React + Vite)
- Services: `kh3tshop-microservices` (Spring Boot microservices: discovery, gateway, identity, catalog, order, common)
- DB: MariaDB/MySQL (docker/mariadb present)
- Broker: Kafka (service code includes `KafkaTopicConfig`)
- Deployment target: Kubernetes (suggested) with Terraform provisioning and CI via Jenkins

How to use
1. Review diagrams in `diagrams/` and adapt service names/ports.
2. Use OpenAPI files as starting contracts for Gateway → services.
3. Apply the sample K8s manifests in `k8s/` as templates, then convert to Helm charts as needed.

See `migration_checklist.md` for recommended migration plan and cutover steps.
