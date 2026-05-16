# Monitoring Operations

## Production access model

- The application serves business traffic on port `8080`, which is published by `docker-compose.prod.yml` as `8020`.
- Actuator metrics are served on the dedicated management port `MANAGEMENT_SERVER_PORT` and default to `8081`.
- `docker-compose.prod.yml` does not publish the management port, so `/actuator/prometheus` remains reachable only from containers on the internal `eod-network`.
- Prometheus is bound to `127.0.0.1` in production. Grafana is published on port `3002` for external access and must use a strong `GRAFANA_ADMIN_PASSWORD`.
- Alertmanager stays on the internal Docker network and receives alerts directly from Prometheus.

## Development monitoring policy

The development compose file keeps the application and MySQL running by default. The full dev monitoring stack remains opt-in through the `monitoring` profile for temporary local troubleshooting, but continuous dev monitoring is handled by the production monitoring stack.

In the normal same-LXC deployment:

- Production runs the only Prometheus, Grafana, Loki, Alloy, Node Exporter, and cAdvisor stack.
- Dev runs `app` and `mysql` by default.
- Dev app metrics stay inside Docker and are scraped over the shared `eod-network-dev` network from `eod-app-dev:8081`.
- Production Prometheus joins `eod-network-dev` and scrapes dev app metrics from `eod-app-dev:8081`.
- Production runs `mysqld-exporter-dev`, which joins `eod-network-dev` and connects to dev MySQL at `eod-mysql-dev:3306`.
- Production Alloy tails both prod and dev log volumes and labels entries with `env="prod"` or `env="dev"`.

This keeps dev visible while avoiding a second Prometheus, Grafana, Loki, Alloy, Node Exporter, and cAdvisor stack during normal deployments.

Default dev deployment:

```bash
docker compose -f docker-compose.yml up -d
```

Temporary dev monitoring:

```bash
docker compose -f docker-compose.yml --profile monitoring up -d
```

The dev CD workflow runs `docker compose -f docker-compose.yml --profile monitoring down --remove-orphans` before starting the default services, so older dev monitoring containers are removed during the next deployment.

Production deployment requires the dev MySQL credentials in:

- `DEV_MYSQL_USER`
- `DEV_MYSQL_PASSWORD`

The GitHub production and dev CD workflows ensure the shared external Docker network `eod-network-dev` exists before Compose runs. The production CD workflow fills these from the existing dev database secrets.

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
- MySQL Exporter: prod and dev MySQL health, connections, and server metrics
- Blackbox Exporter: internal and public HTTP availability checks
- Loki, Alloy, Grafana, and Prometheus self metrics

The application exposes `GET /healthz` as a lightweight unauthenticated liveness endpoint for HTTP availability checks. It intentionally does not check MySQL; database health is monitored separately through MySQL Exporter and HikariCP metrics.

Dashboard files are provisioned from folder structure:

- `monitoring/grafana/dashboards/prod`: production dashboards
- `monitoring/grafana/dashboards/dev`: development dashboards, including the restored dev monitoring views
- `monitoring/grafana/dashboards/logs`: Loki log dashboards

Grafana creates matching `prod`, `dev`, and `logs` folders from these directories.

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

Application logs are written by Logback to `/logs/application.log` inside each app container. In production, `docker-compose.prod.yml` mounts the named volume `app-logs-prod` to that path. In dev, `docker-compose.yml` mounts `app-logs-dev`.

Production Grafana Alloy tails both `/var/log/eod/prod/application.log` and `/var/log/eod/dev/application.log` through read-only mounts, parses the current Logback text format, attaches labels, and sends entries to Loki. Loki stays on the internal Docker network; Grafana is the user-facing access point for Explore and Drilldown > Logs.

Important Loki labels:

- `service_name="eod-backend-prod"` and `service_name="eod-backend-dev"`: application services shown separately in Logs Drilldown
- `service_name="mysql-prod"` and `service_name="mysql-dev"`: MySQL log services shown separately in Logs Drilldown
- `service="eod-backend"` and `service="mysql"`: common service family labels for cross-environment LogQL
- `server="eod-prod-01"`: production server label configured in `docker-compose.prod.yml`
- `env="prod"` or `env="dev"`: source environment label
- `container`: Docker container role
- `level`: parsed log severity

Direct LogQL check:

```logql
{service_name="eod-backend-prod"}
{service_name="eod-backend-dev"}
```

Grafana Logs Drilldown is available from `Drilldown > Logs` in Grafana 12. Logs are exposed as separate Drilldown services through the `service_name` labels `eod-backend-prod`, `eod-backend-dev`, `mysql-prod`, and `mysql-dev`. If Drilldown opens with no logs, expand the time range first; the app default is shorter than the dashboard default.

Operational checks:

```bash
docker compose -f docker-compose.prod.yml ps loki alloy grafana
docker compose -f docker-compose.prod.yml logs --tail=100 alloy
docker compose -f docker-compose.prod.yml exec loki wget -qO- http://localhost:3100/ready
```
