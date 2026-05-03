# Grafana Logs Drilldown Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add server-filterable application logs to the existing Docker Compose monitoring stack so Grafana Logs Drilldown can browse EOD backend logs without writing LogQL.

**Architecture:** Keep the current Prometheus/Grafana stack and add Loki as the log store plus Grafana Alloy as the file log collector. Alloy tails the existing Spring Boot file logs from `/logs`, attaches stable labels such as `service_name`, `server`, `env`, and `container`, and pushes entries to Loki over the private Docker network. Grafana provisions Loki as an additional datasource and runs a Grafana version where Logs Drilldown is available.

**Tech Stack:** Docker Compose, Grafana 12.x or Grafana >= 11.6.11, Loki 3.x, Grafana Alloy, Spring Boot Logback.

---

## Current State

- `docker-compose.prod.yml` already runs `app`, `mysql`, `prometheus`, `alertmanager`, and `grafana`.
- `docker-compose.prod.yml` mounts app logs from host `/eod/prod/logs` into the app container as `/logs`.
- `src/main/resources/logback-spring.xml` writes `${LOG_DIR}/application.log` and gzipped rotated files.
- `monitoring/grafana/provisioning/datasources/prometheus.yml` provisions only Prometheus.
- There is no Loki service, Alloy service, Loki datasource, or Logs Drilldown-specific Loki configuration.

## External Requirements

- Grafana Logs Drilldown supports only Loki datasources.
- Grafana Logs Drilldown is installed by default in Grafana `v11.6.11` and later; Grafana `v12` includes Drilldown apps in the navigation.
- Loki must expose log volume data for Drilldown. Set `limits_config.volume_enabled: true`.
- For useful service selection, logs need a stable service label. Use `service_name="eod-backend"` and set Loki `limits_config.discover_service_name`.
- For server-by-server filtering, Alloy must attach `server="${SERVER_NAME}"`.

## File Structure

- Modify: `docker-compose.prod.yml`
  - Upgrade Grafana image.
  - Add `loki` service.
  - Add `alloy` service.
  - Add `loki-data-prod` and `alloy-data-prod` volumes.
  - Pass `SERVER_NAME` and `ENVIRONMENT` to Alloy.

- Modify: `docker-compose.yml`
  - Mirror the local/dev version of Loki and Alloy.
  - Use `SERVER_NAME=${SERVER_NAME:-local-dev}` and `ENVIRONMENT=${ENVIRONMENT:-dev}` defaults.

- Create: `monitoring/loki/loki-config.yml`
  - Single-binary filesystem-backed Loki config for Docker Compose.
  - Enable `volume_enabled`.
  - Set `discover_service_name`.
  - Keep Loki internal-only.

- Create: `monitoring/alloy/config.alloy`
  - Tail `/var/log/eod/application.log`.
  - Attach labels required by Drilldown.
  - Parse the current Logback text format enough to extract `level`, `thread`, and `logger`.
  - Forward logs to `http://loki:3100/loki/api/v1/push`.

- Modify or rename: `monitoring/grafana/provisioning/datasources/prometheus.yml`
  - Either keep the filename and add Loki to the same provisioning file, or rename to `datasources.yml`.
  - Preserve Prometheus as the default datasource.
  - Add Loki with uid `loki`.

- Modify: `monitoring/README.md`
  - Document the logs architecture, required env vars, access path, and smoke-test commands.

- Optional later modify: `src/main/resources/logback-spring.xml`
  - Keep out of the first pass unless parsing proves brittle.
  - Future improvement: switch file log output to JSON for cleaner fields.

---

## Chunk 1: Baseline Verification

### Task 1: Confirm Current Compose and Log Paths

**Files:**
- Read: `docker-compose.prod.yml`
- Read: `docker-compose.yml`
- Read: `src/main/resources/logback-spring.xml`
- Read: `monitoring/grafana/provisioning/datasources/prometheus.yml`

- [ ] **Step 1: Verify current services**

Run:

```bash
docker compose -f docker-compose.prod.yml config --services
```

Expected: output includes `app`, `mysql`, `prometheus`, `alertmanager`, `grafana`; does not include `loki` or `alloy`.

- [ ] **Step 2: Verify app log path contract**

Run:

```bash
rg -n "LOG_DIR|application.log|/logs|/eod/prod/logs" docker-compose.prod.yml src/main/resources/logback-spring.xml
```

Expected: app mounts `/eod/prod/logs:/logs`, and Logback writes `${LOG_DIR}/application.log`.

- [ ] **Step 3: Commit is not required**

No code changes in this task.

---

## Chunk 2: Loki Storage

### Task 2: Add Loki Configuration

**Files:**
- Create: `monitoring/loki/loki-config.yml`

- [ ] **Step 1: Create Loki config**

Create `monitoring/loki/loki-config.yml`:

```yaml
auth_enabled: false

server:
  http_listen_port: 3100
  grpc_listen_port: 9095

common:
  instance_addr: 127.0.0.1
  path_prefix: /loki
  storage:
    filesystem:
      chunks_directory: /loki/chunks
      rules_directory: /loki/rules
  replication_factor: 1
  ring:
    kvstore:
      store: inmemory

query_range:
  results_cache:
    cache:
      embedded_cache:
        enabled: true
        max_size_mb: 100

schema_config:
  configs:
    - from: 2024-04-01
      store: tsdb
      object_store: filesystem
      schema: v13
      index:
        prefix: index_
        period: 24h

limits_config:
  volume_enabled: true
  discover_service_name:
    - service_name
    - app
    - job

analytics:
  reporting_enabled: false
```

- [ ] **Step 2: Validate YAML parses**

Run:

```bash
ruby -e 'require "yaml"; YAML.load_file("monitoring/loki/loki-config.yml"); puts "ok"'
```

Expected: `ok`.

- [ ] **Step 3: Commit**

Run:

```bash
git add monitoring/loki/loki-config.yml
git commit -m "infra: add loki config for logs drilldown"
```

---

## Chunk 3: Alloy Log Collection

### Task 3: Add Alloy File Tailer

**Files:**
- Create: `monitoring/alloy/config.alloy`

- [ ] **Step 1: Create Alloy config**

Create `monitoring/alloy/config.alloy`:

```river
loki.source.file "eod_app" {
  targets = [
    {
      __path__      = "/var/log/eod/application.log",
      service_name  = "eod-backend",
      app           = "eod",
      env           = sys.env("ENVIRONMENT"),
      server        = sys.env("SERVER_NAME"),
      container     = "eod-app",
      log_source    = "spring-file",
    },
  ]

  forward_to = [loki.process.eod_app.receiver]
}

loki.process "eod_app" {
  stage.regex {
    expression = "^(?P<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) (?P<level>[A-Z]+)\\s+\\[(?P<thread>[^\\]]+)\\] (?P<logger>[^ ]+) - (?P<message>.*)$"
  }

  stage.labels {
    values = {
      level = "",
    }
  }

  stage.structured_metadata {
    values = {
      thread = "",
      logger = "",
    }
  }

  forward_to = [loki.write.local.receiver]
}

loki.write "local" {
  endpoint {
    url = "http://loki:3100/loki/api/v1/push"
  }
}
```

- [ ] **Step 2: Validate Alloy config in container**

Run:

```bash
docker run --rm \
  -v "$PWD/monitoring/alloy/config.alloy:/etc/alloy/config.alloy:ro" \
  grafana/alloy:latest \
  run --server.http.listen-addr=127.0.0.1:12345 --storage.path=/tmp/alloy /etc/alloy/config.alloy
```

Expected: Alloy starts without a River parse error. Stop it with `Ctrl+C` after confirming startup.

- [ ] **Step 3: Commit**

Run:

```bash
git add monitoring/alloy/config.alloy
git commit -m "infra: collect app file logs with alloy"
```

---

## Chunk 4: Docker Compose Integration

### Task 4: Wire Loki and Alloy into Production Compose

**Files:**
- Modify: `docker-compose.prod.yml`

- [ ] **Step 1: Upgrade Grafana image**

Change:

```yaml
image: grafana/grafana:11.1.0
```

To a current pinned Grafana 12 image after checking availability during implementation, for example:

```yaml
image: grafana/grafana:12.4.0
```

If `12.4.0` is not available at implementation time, use the latest stable `12.x` image. Do not use `latest`.

- [ ] **Step 2: Add Loki service after Alertmanager**

Add:

```yaml
  loki:
    image: grafana/loki:3.7.0
    container_name: eod-loki
    restart: always
    command:
      - -config.file=/etc/loki/loki-config.yml
    volumes:
      - ./monitoring/loki/loki-config.yml:/etc/loki/loki-config.yml:ro
      - loki-data-prod:/loki
    networks:
      - eod-network
```

Do not publish `3100` externally in production.

- [ ] **Step 3: Add Alloy service after Loki**

Add:

```yaml
  alloy:
    image: grafana/alloy:latest
    container_name: eod-alloy
    restart: always
    command:
      - run
      - --server.http.listen-addr=0.0.0.0:12345
      - --storage.path=/var/lib/alloy
      - /etc/alloy/config.alloy
    environment:
      - SERVER_NAME=${SERVER_NAME:?SERVER_NAME is required}
      - ENVIRONMENT=${ENVIRONMENT:-prod}
    volumes:
      - ./monitoring/alloy/config.alloy:/etc/alloy/config.alloy:ro
      - /eod/prod/logs:/var/log/eod:ro
      - alloy-data-prod:/var/lib/alloy
    depends_on:
      loki:
        condition: service_started
    networks:
      - eod-network
```

Implementation note: replace `grafana/alloy:latest` with a pinned current Alloy version if the team wants fully reproducible deployments.

- [ ] **Step 4: Make Grafana depend on Loki**

Change Grafana `depends_on` to include Loki:

```yaml
    depends_on:
      prometheus:
        condition: service_started
      loki:
        condition: service_started
```

- [ ] **Step 5: Add volumes**

Add under `volumes`:

```yaml
  loki-data-prod:
    name: eod_loki-data-prod
  alloy-data-prod:
    name: eod_alloy-data-prod
```

- [ ] **Step 6: Validate production compose**

Run:

```bash
docker compose -f docker-compose.prod.yml config
```

Expected: config renders successfully when required env vars are supplied. If local shell lacks required prod vars, run with temporary non-secret placeholders:

```bash
GRAFANA_ADMIN_USER=admin \
GRAFANA_ADMIN_PASSWORD=placeholder \
SERVER_NAME=eod-prod-01 \
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/eod \
SPRING_DATASOURCE_USERNAME=eod \
SPRING_DATASOURCE_PASSWORD=placeholder \
JWT_SECRET=placeholder \
BASE_URL=https://example.com \
FRONTEND_BASE_URL=https://example.com \
LOG_DIR=/logs \
BSM_CLIENT_ID=placeholder \
BSM_CLIENT_SECRET=placeholder \
BSM_OAUTH_BASE_URL=https://auth.bssm.app \
BSM_REDIRECT_URI=https://example.com/oauth/bsm \
FILE_UPLOAD_BASE_URL=https://example.com \
MYSQL_ROOT_PASSWORD=placeholder \
MYSQL_DATABASE=eod \
MYSQL_USER=eod \
MYSQL_PASSWORD=placeholder \
DISCORD_WEBHOOK_URL=https://example.com/webhook \
docker compose -f docker-compose.prod.yml config >/tmp/eod-compose-prod.yml
```

- [ ] **Step 7: Commit**

Run:

```bash
git add docker-compose.prod.yml
git commit -m "infra: add loki and alloy to production compose"
```

### Task 5: Mirror Loki and Alloy in Dev Compose

**Files:**
- Modify: `docker-compose.yml`

- [ ] **Step 1: Add Loki service**

Use the same Loki service as production, but name the container `eod-loki-dev` and use volume `loki-data-dev`.

- [ ] **Step 2: Add Alloy service**

Use the same Alloy service as production, but:

```yaml
container_name: eod-alloy-dev
environment:
  - SERVER_NAME=${SERVER_NAME:-local-dev}
  - ENVIRONMENT=${ENVIRONMENT:-dev}
volumes:
  - ./monitoring/alloy/config.alloy:/etc/alloy/config.alloy:ro
  - /eod/logs:/var/log/eod:ro
  - alloy-data-dev:/var/lib/alloy
```

- [ ] **Step 3: Upgrade dev Grafana image**

Use the same Grafana version as production.

- [ ] **Step 4: Add dev volumes**

Add:

```yaml
  loki-data-dev:
    name: eod_loki-data-dev
  alloy-data-dev:
    name: eod_alloy-data-dev
```

- [ ] **Step 5: Validate dev compose**

Run:

```bash
docker compose -f docker-compose.yml config
```

Expected: config renders successfully with existing dev defaults plus any required app env vars.

- [ ] **Step 6: Commit**

Run:

```bash
git add docker-compose.yml
git commit -m "infra: add loki and alloy to dev compose"
```

---

## Chunk 5: Grafana Datasource and Drilldown

### Task 6: Provision Loki Datasource

**Files:**
- Modify: `monitoring/grafana/provisioning/datasources/prometheus.yml`

- [ ] **Step 1: Add Loki datasource**

Keep Prometheus as default and add:

```yaml
  - name: Loki
    uid: loki
    type: loki
    access: proxy
    url: http://loki:3100
    isDefault: false
    editable: true
    jsonData:
      maxLines: 1000
```

- [ ] **Step 2: Validate YAML parses**

Run:

```bash
ruby -e 'require "yaml"; YAML.load_file("monitoring/grafana/provisioning/datasources/prometheus.yml"); puts "ok"'
```

Expected: `ok`.

- [ ] **Step 3: Commit**

Run:

```bash
git add monitoring/grafana/provisioning/datasources/prometheus.yml
git commit -m "infra: provision loki datasource in grafana"
```

### Task 7: Smoke Test Drilldown Locally

**Files:**
- No expected file changes.

- [ ] **Step 1: Ensure local log directory exists**

Run:

```bash
sudo mkdir -p /eod/logs
sudo chown "$USER" /eod/logs
printf '2026-05-03 12:00:00.000 INFO  [main] com.eod.eod.Test - drilldown smoke test\n' >> /eod/logs/application.log
```

- [ ] **Step 2: Start observability services**

Run:

```bash
docker compose -f docker-compose.yml up -d loki alloy grafana
```

Expected: `eod-loki-dev`, `eod-alloy-dev`, and `eod-grafana-dev` are running.

- [ ] **Step 3: Verify Loki readiness**

Run:

```bash
docker compose -f docker-compose.yml exec loki wget -qO- http://localhost:3100/ready
```

Expected: readiness output indicates Loki is ready.

- [ ] **Step 4: Verify logs reached Loki**

Run:

```bash
docker compose -f docker-compose.yml exec loki wget -qO- 'http://localhost:3100/loki/api/v1/labels'
```

Expected: labels include `service_name`, `server`, `env`, `container`, and `level`.

- [ ] **Step 5: Query by server**

Run from the host:

```bash
curl -G 'http://localhost:3100/loki/api/v1/query_range' \
  --data-urlencode 'query={service_name="eod-backend",server="local-dev"}' \
  --data-urlencode 'limit=10'
```

If Loki is not published to the host in dev, run the equivalent query from inside the Loki container or temporarily publish `3100:3100` only in dev.

Expected: response includes the smoke test log line.

- [ ] **Step 6: Verify in Grafana**

Open `http://localhost:3001`.

Expected:

- Connections > Data sources shows `Loki`.
- Explore can query `{service_name="eod-backend",server="local-dev"}`.
- Drilldown > Logs shows `eod-backend` as a service.
- Filters include `server`, `env`, `container`, and `level`.

---

## Chunk 6: Documentation and Operations

### Task 8: Update Monitoring README

**Files:**
- Modify: `monitoring/README.md`

- [ ] **Step 1: Add required env vars**

Add:

```markdown
- `SERVER_NAME`: stable server identifier used as the Loki `server` label, for example `eod-prod-01`.
- `ENVIRONMENT`: deployment environment used as the Loki `env` label. Defaults to `prod` in production compose.
```

- [ ] **Step 2: Add logs architecture section**

Add:

````markdown
## Logs

Application logs are written by Logback to `/logs/application.log` inside the app container. In production, Docker Compose mounts host `/eod/prod/logs` to that path.

Grafana Alloy tails `/eod/prod/logs/application.log` through a read-only mount, parses the current Logback text format, attaches labels, and sends entries to Loki.

Important labels:

- `service_name="eod-backend"`: primary Logs Drilldown service label.
- `server`: physical or VM server name from `SERVER_NAME`.
- `env`: deployment environment.
- `container`: Docker container role.
- `level`: parsed log severity.

In Grafana, use Drilldown > Logs and select the Loki datasource. For direct LogQL checks, use:

```logql
{service_name="eod-backend", server="eod-prod-01"}
```
````

- [ ] **Step 3: Add smoke-test commands**

Document:

```bash
docker compose -f docker-compose.prod.yml ps loki alloy grafana
docker compose -f docker-compose.prod.yml logs --tail=100 alloy
docker compose -f docker-compose.prod.yml exec loki wget -qO- http://localhost:3100/ready
```

- [ ] **Step 4: Commit**

Run:

```bash
git add monitoring/README.md
git commit -m "docs: document logs drilldown operations"
```

---

## Chunk 7: Production Rollout

### Task 9: Deploy Safely

**Files:**
- No expected file changes.

- [ ] **Step 1: Prepare production env**

On the production server, set:

```bash
SERVER_NAME=eod-prod-01
ENVIRONMENT=prod
```

Use the real server name. If there are multiple servers, each server must have a unique stable `SERVER_NAME`.

- [ ] **Step 2: Create host log directory if missing**

Run on the production server:

```bash
sudo mkdir -p /eod/prod/logs
```

Expected: directory exists and app can continue writing logs through its existing mount.

- [ ] **Step 3: Pull and start new services**

Run:

```bash
docker compose -f docker-compose.prod.yml pull grafana loki alloy
docker compose -f docker-compose.prod.yml up -d grafana loki alloy
```

Expected: existing app and database remain running; new observability services start.

- [ ] **Step 4: Check Alloy ingestion**

Run:

```bash
docker compose -f docker-compose.prod.yml logs --tail=100 alloy
```

Expected: no parse errors, no repeated push errors to Loki.

- [ ] **Step 5: Check Grafana**

Open Grafana on port `3002`.

Expected:

- `Loki` datasource is healthy.
- Drilldown > Logs is visible.
- `eod-backend` appears.
- Filtering by the current `SERVER_NAME` returns logs.

---

## Risks and Decisions

- **Grafana image pin:** Do not stay on `grafana/grafana:11.1.0`; it predates the default Logs Drilldown availability. Use a pinned `12.x` image or at least `11.6.11+`.
- **Alloy image pin:** The plan shows `grafana/alloy:latest` for initial simplicity, but production should prefer a pinned version after confirming the current stable Alloy tag.
- **Label cardinality:** Keep labels low-cardinality. Do not label by request id, user id, item id, URL, exception message, or trace id. Put those into structured metadata or log fields instead.
- **Current text logs:** Regex parsing is acceptable for the first pass. JSON logs are a cleaner follow-up if Drilldown fields need to be richer.
- **Loki retention:** This plan does not set retention. Add retention only after deciding the target storage window, for example 7, 14, or 30 days.
- **Security:** Keep Loki internal-only in production. Grafana is already the user-facing access point.

## Source References

- Grafana Logs Drilldown requires a Loki datasource and supports Loki-only default datasource configuration.
- Grafana Logs Drilldown is default-installed in Grafana `v11.6.11+`; Grafana `v12+` includes Drilldown apps in navigation.
- Loki Drilldown troubleshooting requires `limits_config.volume_enabled: true` for log volume.
- Grafana Alloy `loki.source.file` tails files and forwards them to `loki.write`, with built-in file matching and persisted positions.
