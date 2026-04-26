# Monitoring Operations

## Production access model

- The application serves business traffic on port `8080`, which is published by `docker-compose.prod.yml` as `8020`.
- Actuator metrics are served on the dedicated management port `MANAGEMENT_SERVER_PORT` and default to `8081`.
- `docker-compose.prod.yml` does not publish the management port, so `/actuator/prometheus` remains reachable only from containers on the internal `eod-network`.
- Prometheus and Grafana are bound to `127.0.0.1` in production. If remote access is required, place them behind a reverse proxy with authentication or use an SSH tunnel instead of widening the bind address.
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

These rules are defined in `monitoring/prometheus/alerts/eod-alerts.yml` and routed through Alertmanager using the Discord webhook from `DISCORD_WEBHOOK_URL`. During production deployment, the workflow writes that secret to `monitoring/alertmanager/secrets/discord_webhook_url`, which is mounted into Alertmanager and read through `webhook_url_file`.
