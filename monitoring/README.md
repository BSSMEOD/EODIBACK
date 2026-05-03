# Monitoring Operations

## Production access model

- The application serves business traffic on port `8080`, which is published by `docker-compose.prod.yml` as `8020`.
- Actuator metrics are served on the dedicated management port `MANAGEMENT_SERVER_PORT` and default to `8081`.
- `docker-compose.prod.yml` does not publish the management port, so `/actuator/prometheus` remains reachable only from containers on the internal `eod-network`.
- Prometheus is bound to `127.0.0.1` in production. Grafana is published on port `3002` for external access and must use a strong `GRAFANA_ADMIN_PASSWORD`.
- Alertmanager stays on the internal Docker network and receives alerts directly from Prometheus.

## Required production environment variables

- `GRAFANA_ADMIN_USER`
- `GRAFANA_ADMIN_PASSWORD`
- `DISCORD_WEBHOOK_URL`

The production compose file intentionally has no fallback for these values. Missing variables should fail fast during deployment instead of silently starting with unsafe defaults.

## Alert coverage

The baseline alert rules cover:

- Application scrape failure
- HTTP 5xx ratio above 5% for 10 minutes
- HTTP p95 latency above 1 second for 10 minutes
- JVM heap usage above 90% for 15 minutes
- HikariCP pending connections
- JVM GC pause average above 200ms
- Public and internal `/healthz` availability
- Monitoring exporter availability
- Host CPU, memory, disk, and inode pressure
- MySQL availability and connection usage
- Scheduler failures

These rules are defined in `monitoring/prometheus/alerts/eod-alerts.yml` and routed through Alertmanager using the Discord webhook from `DISCORD_WEBHOOK_URL`. During production deployment, the workflow renders `monitoring/alertmanager/generated/alertmanager.yml` with that secret and mounts the generated file into Alertmanager.

## Metrics

The monitoring stack collects metrics from:

- Spring Boot Actuator: HTTP, JVM, HikariCP, and EOD custom domain metrics
- Node Exporter: host CPU, memory, disk, inode, and network metrics
- cAdvisor: Docker container resource metrics
- MySQL Exporter: MySQL health, connections, and server metrics
- Blackbox Exporter: internal and public HTTP availability checks
- Loki, Alloy, Grafana, and Prometheus self metrics

The application exposes `GET /healthz` as a lightweight unauthenticated liveness endpoint for HTTP availability checks. It intentionally does not check MySQL; database health is monitored separately through MySQL Exporter and HikariCP metrics.

Dashboard files:

- `monitoring/grafana/dashboards/eod-overview.json`: application HTTP/JVM/DB overview
- `monitoring/grafana/dashboards/eod-infrastructure.json`: server, container, MySQL, and availability metrics
- `monitoring/grafana/dashboards/eod-domain-metrics.json`: EOD business/domain metrics

Domain metric names:

- `eod_business_events_total{domain,action,result}`
- `eod_external_call_seconds{provider,operation,result}`
- `eod_image_upload_bytes{result}`
- `eod_image_upload_seconds{result}`
- `eod_scheduler_runs_total{task,result}`
- `eod_scheduler_processed_items{task}`
- `eod_items_current{status}`
- `eod_claims_current{status}`
- `eod_reward_eligible_items`
- `eod_reward_records_current`

## Logs

Application logs are written by Logback to `/logs/application.log` inside the app container. In production, `docker-compose.prod.yml` mounts host `/eod/prod/logs` to that path.

Grafana Alloy tails `/eod/prod/logs/application.log` through a read-only mount, parses the current Logback text format, attaches labels, and sends entries to Loki. Loki stays on the internal Docker network; Grafana is the user-facing access point for Explore and Drilldown > Logs.

Important Loki labels:

- `service_name="eod-backend"`: primary service label for Logs Drilldown
- `server="eod-prod-01"`: production server label configured in `docker-compose.prod.yml`
- `env="prod"`: production environment label configured in `docker-compose.prod.yml`
- `container`: Docker container role
- `level`: parsed log severity

Direct LogQL check:

```logql
{service_name="eod-backend", server="eod-prod-01"}
```

Operational checks:

```bash
docker compose -f docker-compose.prod.yml ps loki alloy grafana
docker compose -f docker-compose.prod.yml logs --tail=100 alloy
docker compose -f docker-compose.prod.yml exec loki wget -qO- http://localhost:3100/ready
```
