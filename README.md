# MISSION/90 — Java Security Career Command Center

**90-day sprint:** November 9, 2026 → February 6, 2027. **Interview target:** January 2027. This is a private, single-owner placement and learning dashboard built with **Java 21, Spring Boot, HTML/CSS/vanilla JS, Spring Security and PostgreSQL**.

## Features

- Seven seeded daily study tracks: Core Java / Spring Boot, Java DSA, VAPT and SOC fundamentals, OOP/OS/Networking/SQL/System Design, Frontend/React, Agentic AI/RAG and Placement.
- 90 calendar days and 630 seeded tasks, with 10–11.75 planned focused hours per day. Weekends reserve 180 minutes for the purchased AI/RAG course; January onward reserves at least 35% for placement preparation.
- Persistent task checkboxes, notes, priority, actual minutes and HTTPS deliverable links. Custom daily tasks can be added or deleted.
- Personal dashboard with 14-day completion chart, per-track progress and an activity streak (days completing at least 70% of tasks).
- SQL-backed DSA problem journal, placement pipeline, daily reflections and actual-hours logs.
- Private sign-in, session authentication, CSRF protection, owner-only API, downloadable JSON backup.
- Mobile-friendly offline preview. **Preview is browser-only and cannot sync to the database.** The actual hosted app does not silently fall back to preview when logged out/offline.
- Render deployment configuration, Docker image, optional local PostgreSQL Compose stack, automated GitHub Actions build/test workflow.

## Run locally with Maven

Requirements: **JDK 21 and Maven 3.9+**.

```bash
export APP_USER=uday
read -rs 'APP_PASSWORD?Owner password (16-72 characters): '; echo
export APP_PASSWORD
mvn spring-boot:run
```

Open http://localhost:8080/login.html and sign in. Local development uses H2 database stored at `./data/missiondb.mv.db`; data survives process restarts when that file is kept. The app creates the `data/` directory automatically through H2 when running from the project directory; if needed, run `mkdir -p data` first.

Run the tests with `mvn test`.

## Run with PostgreSQL locally

```bash
export APP_USER=uday
read -rs 'APP_PASSWORD?Owner password (16-72 characters): '; echo
read -rs 'LOCAL_DB_PASSWORD?Local database password: '; echo
export APP_PASSWORD LOCAL_DB_PASSWORD
docker compose up --build
```

The Compose database persists in a Docker named volume. No default database or owner password is supplied. Prompt examples use zsh; use your shell's equivalent hidden-input prompt on other platforms.

## Deploy to Render (free tier) + Neon (free tier)

1. Create a new **private GitHub repo** for the code (do not put this into unrelated repositories). Push the complete source directory.
2. Create a **Neon PostgreSQL** database. In its dashboard copy the hostname, database name, database user and password. JDBC format: `jdbc:postgresql://YOUR_NEON_HOST/YOUR_DATABASE?sslmode=require`. Do NOT use `postgresql://...` in `JDBC_DATABASE_URL`.
3. Create a free Render Web Service from the GitHub repo using the `render.yaml` Blueprint and Docker runtime.
4. Set the following **private Render environment variables** using the hosting UI; do not commit secrets:

| Variable | What to put |
| --- | --- |
| `APP_USER` | Your private login username |
| `APP_PASSWORD` | A unique password of 16-72 characters, entered privately |
| `JDBC_DATABASE_URL` | Neon JDBC URL, starting with `jdbc:postgresql://`, SSL required |
| `DB_USER` | Neon database username |
| `DB_PASSWORD` | Neon database password |
| `SPRING_PROFILES_ACTIVE` | `prod` (already in `render.yaml`) |
| `COOKIE_SECURE` | `true` (already in `render.yaml`) |

5. Wait until the Render deploy **succeeds**. Open `/actuator/health` and verify it reports `UP`. Open the generated HTTPS site, sign in, tick a checkbox, refresh, and verify it stays ticked.
6. Log a DSA problem and a placement application, edit your journal, reload, and verify every record persists. Look at database tables in Neon to verify remote PostgreSQL storage.
7. Use the dashboard's **DOWNLOAD MY DATA BACKUP** control once a week. This backup downloads personal content, so store it privately.

### Important free plan restrictions

- Render free web services spin down after 15 minutes of inactivity, and may need about a minute to wake.
- Render free service disks are **ephemeral**. Do not use H2 in the cloud: an external Neon PostgreSQL database is required by the production startup guard.
- Neon free storage and compute/network quotas apply. If the free plan is exceeded, services can be suspended. Do not assume unlimited uptime.
- This is a **single-user prototype**. Do not create multiple login users without adding proper account ownership and per-user row isolation first. It is not a public school placement-management service yet.

## API (authenticated session required)

| Endpoint | Purpose |
| --- | --- |
| `GET /api/whoami` | Session check |
| `GET /api/bootstrap?date=YYYY-MM-DD` | Tasks, score and journal for a date |
| `GET /api/roadmap` | Weekly study plan |
| `GET /api/stats` | Per-skill progress and recent activity |
| `POST /api/tasks` | Add custom daily task |
| `PATCH /api/tasks/{id}` | Mark done or update note |
| `DELETE /api/tasks/{id}` | Delete a custom task only |
| `GET /api/dsa` · `POST /api/dsa` | Problem-solving journal |
| `PUT /api/dsa/{id}` · `DELETE /api/dsa/{id}` | Edit or delete a problem |
| `GET /api/applications` · `POST /api/applications` | Recruiting pipeline |
| `PATCH /api/applications/{id}` · `DELETE /api/applications/{id}` | Edit dates, notes, stage or remove a record |
| `PUT /api/journal?date=YYYY-MM-DD` | Daily reflection |
| `GET /api/export` | Personal data backup |

All mutation endpoints require a CSRF token from `GET /api/csrf`, sent in `X-XSRF-TOKEN`. The browser UI handles this automatically.

## Notifications

Daily ChatGPT reminders are scheduled separately. The web app does not yet run an email, push notification, or scheduled delivery worker, and **checking tasks does not automatically sync with ChatGPT or Google Sheets**. To add independent reminders, connect an authenticated email service/notification provider and implement a scheduled worker or hosted cron (do not depend on a sleeping free Render server for exact delivery).

## Persistence and security

Flyway V1 initializes a new H2 or PostgreSQL schema; Hibernate validates it rather than changing it automatically. Seeding uses unique date/track keys and preserves completed records on restart. Back up existing pre-Flyway databases and rehearse a deliberate baseline migration; never enable automatic baselining or drop tables to force startup.

The application has exactly one owner account, bcrypt-hashed in server memory from injected configuration. This is not a multi-tenant application: adding users requires per-record ownership before access is enabled. All private APIs require OWNER; cookies are HTTP-only/SameSite=Lax and Secure on Render, with CSRF on every mutation. Owner credential rotation is an environment update plus redeploy; sessions expire/restart separately and are not durable business data. Use a password manager and HTTPS. There is no password recovery email, MFA, distributed login rate limiter, or automated notification worker yet; do not open self-registration or share credentials.

Production startup requires PostgreSQL/TLS and secure cookies. Never store secrets in JDBC URLs, source, Docker layers, README or GitHub Actions. JSON backups include personal notes and must be stored privately. Journal hours are the authoritative daily total; per-task actual minutes are detail, not added again to journal totals. Completed-task planned duration is not actual study time.

## Verification

Run `mvn -B clean verify`, `node --check src/main/resources/static/app.js`, and `node --check src/main/resources/static/enhancements.js`. Tests use isolated H2 and generated credentials, not cloud secrets. GitHub Actions has read-only repository permissions and no deployment credentials. See `DEPLOYMENT_REPORT.md` for actual deployment and browser verification results; hosting is not verified merely by supplying configuration.

Official free-tier references (accessed 2026-11-08): [Render Free](https://render.com/docs/free), [Neon plans](https://neon.com/pricing). Render Free sleeps, has shared monthly limits and no production SLA; Neon Free has bounded compute/storage/egress and limited restore history. Keep an independent private JSON backup. No payment or plan upgrade is part of this deployment.
