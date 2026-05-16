# Central Dev Monitoring Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Keep dev continuously monitored without running a second full monitoring stack.

**Architecture:** Production keeps the only Prometheus, Grafana, Loki, Alloy, node-exporter, and cAdvisor stack. Dev runs only the app and MySQL by default; production Prometheus scrapes dev app metrics through a loopback-published management port, production cAdvisor/node-exporter cover dev containers and host resources, production Alloy tails dev log volumes, and production runs a lightweight MySQL exporter for the dev database.

**Tech Stack:** Docker Compose, Prometheus, Grafana Alloy, Loki, Spring Boot Actuator, mysqld-exporter.

---

## Chunk 1: Compose Wiring

### Task 1: Expose Dev App Metrics Internally

**Files:**
- Modify: `docker-compose.yml`

- [x] Add `MANAGEMENT_SERVER_PORT=${MANAGEMENT_SERVER_PORT:-8081}` to the dev app environment.
- [x] Publish the dev management port on loopback only, using host port `8082`.
- [x] Validate with `docker compose -f docker-compose.yml config`.

### Task 2: Let Production Monitoring Reach Host-Published Dev Targets

**Files:**
- Modify: `docker-compose.prod.yml`

- [x] Add `extra_hosts: ["host.docker.internal:host-gateway"]` to production Prometheus.
- [x] Add a lightweight `mysqld-exporter-dev` service to production compose.
- [x] Mount dev app and MySQL log volumes into production Alloy as read-only external volumes.
- [x] Declare the dev log volumes as external named volumes.
- [x] Validate with `docker compose -f docker-compose.prod.yml config`.

## Chunk 2: Prometheus and Alloy Labels

### Task 3: Add Dev Metrics Scrape Targets

**Files:**
- Modify: `monitoring/prometheus/prometheus.yml`

- [x] Add `env="prod"` to existing production scrape targets.
- [x] Add a dev app scrape target for `host.docker.internal:8082`.
- [x] Add a dev MySQL scrape target for `mysqld-exporter-dev:9104`.
- [x] Keep host/container metrics central and label them as `env="shared"`.
- [x] Validate Prometheus YAML parsing.

### Task 4: Tail Prod and Dev Logs Separately

**Files:**
- Modify: `monitoring/alloy/config.alloy`

- [x] Split app file targets into production and dev paths with distinct `env` and `container` labels.
- [x] Split MySQL log targets into production and dev paths with distinct `env` labels.
- [x] Validate Alloy config rendering enough for compose config to mount it.

## Chunk 3: Docs and Verification

### Task 5: Update Operations Documentation

**Files:**
- Modify: `monitoring/README.md`

- [x] Document that dev remains monitored centrally.
- [x] Document the dev management port and dev MySQL exporter location.
- [x] Document prod/dev log labels.

### Task 6: Verify Configuration

**Commands:**
- [x] `docker compose -f docker-compose.yml config`
- [x] `docker compose -f docker-compose.yml config --services` should return only `mysql` and `app` by default.
- [x] `docker compose -f docker-compose.prod.yml config`
- [x] `ruby -e 'require "yaml"; YAML.load_file("monitoring/prometheus/prometheus.yml"); YAML.load_file("monitoring/prometheus/alerts/eod-alerts.yml"); puts "yaml ok"'`
- [x] `git diff --check`
