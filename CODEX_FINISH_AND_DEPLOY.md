# CODEX MASTER PROMPT — FINISH AND DEPLOY MISSION/90

You are a senior full-stack Java engineer, staff product designer, security engineer and DevOps lead. You are working inside the provided `uday-job-mission` directory. **Do not start a new project or remove features.** Your job is to get this existing application production-ready and online as an MVP as quickly as practical, while remaining truthful about tests and deployment status.

## Context and non-negotiable goal

The app is **MISSION/90**, a professional personal job-readiness command center for a learner starting **November 9, 2026**. Its 90-day journey ends **February 6, 2027**, with a major interview-ready checkpoint by **January 2027**. Prepare a new student for Java, core Java, OOP, SQL, networking, Linux, frontend HTML/CSS/JavaScript/React, Spring Boot, DSA, system design (LLD/HLD), AppSec/VAPT and a little SOC, plus a **purchased agentic-AI and RAG course every Saturday/Sunday**.

The user can do **10–12 focused study hours** a day, sometimes 12–14 with breaks, but no encouragement to skip sleep. Daily deliverables need to be measurable, not only video lectures. Learning from absolute beginner to employable junior engineer in 90 days is a best-effort goal, not a guaranteed placement. Prioritize Java backend and secure API fundamentals over spreading effort too thin.

## Existing work to preserve

- Spring Boot 3.5.7, Java 21, Spring Web/Security/JPA, H2 for local data, PostgreSQL for cloud.
- Static responsive website in `src/main/resources/static/{index.html,style.css,app.js}` (this MVP does not require a React bundler; React is a curriculum topic).
- Actual persisted daily task checkboxes through `PATCH /api/tasks/{id}`.
- Dashboard: completed task count, overall progress, day index, focus hours and streak.
- 90-day / 13-week seeded curriculum with seven learning pillars every day.
- DSA problem log, job application pipeline, daily journal (wins, blockers, energy, actual hours, next step).
- Basic owner-only Spring Security login + CSRF tokens.
- Dockerfile and Render Blueprint `render.yaml`.
- Local preview mode when opened as a file, clearly marked browser-only.

## Priority 0: make it genuinely run

1. Inspect the existing code first, including `README.md`, and keep the UI styling unless broken.
2. Install or use Java 21 and Maven as needed; `mvn -B clean verify`. Fix every compilation error and failing test. Verify Spring Boot 3.5.7 and dependency availability; upgrade to a supported stable 3.5.x or later if necessary and keep Java 21 compatibility. If dependencies are unreachable, say that honestly and document exact blocker.
3. Run the app locally, sign in, and test every workflow end to end, including authenticated session handling and CSRF-safe PATCH/POST/PUT/DELETE. Fix login redirection, secure cookies, frontend API errors, and error handling.
4. Verify seeded data: 90 days × 7 tasks = 630 tasks; 13 week cards; Monday Nov 9 begins the sprint; weekends devote **3 hours to the purchased agentic-AI/RAG course**; each day approximately 10–11 focus hours, breaks and sleep excluded.
5. Verify a checkbox stays checked after reload and a backend process restart, in a persistent DB. Same for DSA, jobs and reflections. The browser must not claim cloud data sync while running in local preview mode.
6. Add focused integration tests for API authentication, CSRF failure/success, data persistence, task toggle behavior, DSA logging, application status, and initial seeding idempotence. Run tests and show summary.
7. Make any code refactors only when they improve reliability. Keep a single-origin architecture; no frontend API keys in client JS.

## Priority 1: improve beginner curriculum precision

Daily tasks cannot simply repeat the same seven weekly titles. Improve the 90-day task generator with specific daily outcomes. Each day should name concrete Java exercises, 2–3 DSA problems or patterns at suitable difficulty, a safe cyber lab or defense exercise, core-CS/system-design discussion, frontend deliverable, optional AI topic and placement action. Preserve calendar dates and saved completions; if migration is needed, never wipe existing user progress. By Jan 1 shift at least 35% of study time toward timed OAs, mock interviews, applications/referrals, and explaining the portfolio. Include a few catch-up and revision blocks rather than every day being maximal new content. Track GitHub commits/deliverables (manual field or linked commit URL).

## Priority 2: security and data integrity

- Enforce a strong user-chosen `APP_PASSWORD`; never put secrets in code, examples or repo. Production fails to start unless APP_PASSWORD and PostgreSQL JDBC URL are configured.
- Protect user data from unauthenticated access. Harden session cookies for Render HTTPS, prevent CSRF, output-escape user input, reject unsafe URLs and validate request lengths. Do not silently ignore API validation errors.
- Never test unauthorized targets; AppSec content must use local intentionally vulnerable labs or systems owned/authorized by the learner.
- Confirm database credentials and username are environment secrets only. Prefer migration scripts (Flyway) for production schema evolution; avoid destructive migrations.
- Do not fake any real company placement opening or Cisco hiring date.
- Add a reasonable backup/export mechanism for the user's own records (download JSON/CSV) if this does not block deployment.

## Priority 3: deploy the ONE real app with persistent free-tier storage

Target the lowest-friction free-cloud route, current as of November 2026:

**Render Free Web Service (Docker) + Neon Free PostgreSQL.** Render's free app sleeps after around 15 minutes of no requests and has an ephemeral filesystem. Do NOT use local H2 or SQLite in production. Render's free hosted PostgreSQL expires after 30 days; prefer Neon for persistent user data.

Tasks:
1. Inspect whether the user has connected/authorized GitHub, Render, and Neon access; use already connected sessions where permitted. If account sign-in or provider permissions are needed, request the **one concrete action** required. Do not invent credentials or claim deployment if not verified.
2. Push the source to a new repo named `mission90-career-command-center` under the user's chosen/authorized GitHub account (do not overwrite existing repositories). Preserve `.gitignore`; don't commit `.env`.
3. Create or use a Neon Free PostgreSQL project. Set `JDBC_DATABASE_URL` as a valid JDBC string, e.g. `jdbc:postgresql://HOST/DATABASE?sslmode=require`, with `DB_USER` and `DB_PASSWORD` separately. Consider pooling and database sleep/wake behavior.
4. Deploy on Render Free as a Docker Web Service or Blueprint using `render.yaml`. Configure Render env secrets `APP_USER`, unique `APP_PASSWORD`, `JDBC_DATABASE_URL`, `DB_USER`, `DB_PASSWORD` and `COOKIE_SECURE=true`. Avoid exposing private variables in logs, screenshots, markdown, or commit history.
5. Monitor build and startup logs. Fix free-tier memory failures (512 MB class) via JVM tuning or simpler dependencies. Verify the app responds and that login, task saving, reload and restart truly persist to Neon.
6. Return **verified** public website URL and GitHub URL, and brief exact login instructions. Never fabricate URLs or claim it's deployed before testing.

If free-host provider access is unavailable, still finish code, tests and a deploy-ready repo; clearly state the precise missing connection action. The user has a short available window; minimize unnecessary questions, prefer sensible defaults and work autonomously.

## UX acceptance criteria

- Premium dark navy dashboard, lime accents, responsive mobile design; preserve existing style character.
- Task checkboxes clickable and backend-persistent; progress bars react immediately.
- Ability to select a day; daily plan corresponds to actual 2026/2027 date.
- Clear "today" agenda, target focus hours, task descriptions, completed ratio and streak.
- DSA solved-problem records with status, topic, difficulty, elapsed minutes, insight.
- Job application record with stage, deadline, verified link, follow-up note.
- Daily reflection with energy, wins, blockers, next action and actual focus hours.
- Roadmap of weeks and per-week completion.
- No fake productivity metrics or seeded "completed" records; initial state 0%.
- Backend persistence and security are required, not just localStorage.

## Notifications

The user **already has a ChatGPT daily plan automation scheduled for 7:30 AM IST** and a separate placement-email watch. The site must not falsely claim to integrate with ChatGPT notifications. An optional later enhancement is email reminders via a free email provider, requiring explicit opt-in, safe unsubscribe and a reliable external cron. Do not block MVP deployment for email notifications. Site checkboxes do not automatically control ChatGPT reminders.

## Final verification report

Return a compact report with (a) exact local tests executed and their results, (b) 5 feature screenshots or equivalent view checks, (c) secure persistence and database verification, (d) verified public URL, GitHub repo, and free tier caveats, and (e) exact blocker / next action if any step is incomplete.

Begin by reading the project; then implement, test, push and deploy rather than replying with a long speculative plan.
