# MISSION/90 Deployment Verification

Date: 2026-11-08 (Asia/Kolkata).

## Resources

- Private source: https://github.com/uday-payghon/mission90-career-command-center
- Application commit: `4cd244adbb08450c40e07454e528d69ef2578072`.
- Render: `mission90-career-command-center`, Free web service, Singapore, non-root Java 21 Docker runtime. Auto-deploy is deliberately off; future changes need a manual deploy after tests.
- Service: `srv-dbo5ijijnfac73dv3340`.
- URL: https://mission90-career-command-center.onrender.com
- Health check: `/actuator/health/readiness`, including database connectivity and application readiness; liveness is separate.
- Neon: `mission90-career-command-center`, project `still-butterfly-97930977`, PostgreSQL 16, Singapore, Free organization plan, compute capped at 0.25 CU. Passwords were supplied through private environment configuration, never committed.

## Fixes and completed behavior

- Repaired the original missing Spring Security `Customizer` import and moved Spring Boot to the 3.5.16 maintenance release while retaining Java 21.
- Removed fallback owner/database passwords; require a privately configured 16-72 character owner password. Bcrypt, session login, owner-only private APIs, CSRF, safe API errors, Secure/HTTP-only/SameSite cookies and production startup validation are in place.
- Added Flyway V1 with indexes and constraints. Hibernate validates instead of modifying schemas. No legacy database was dropped or migrated.
- Made curriculum initialization idempotent and transactional, retaining existing checked tasks. Exactly 630 seeded tasks span 90 dates, 2026-11-09 through 2027-02-06. Plans are 10-11.75 focus hours/day, weekend AI course blocks are 180 minutes, and placement preparation is at least 35% from January onward.
- Added task notes, priority, actual duration and HTTPS deliverable links; DSA edit/revision dates; application edit/delete, interview and follow-up dates; weekly/monthly progress and upcoming dates; working sign-out.
- Added bounded inputs and HTTPS URL checks. Planned duration of completed tasks is explicitly distinguished from actual journal hours.
- Fixed stale asynchronous screen refreshes and prevented checkbox changes while a save is pending. Failed syncs do not claim database synchronization.
- Self-hosted the original DM Sans/Space Grotesk fonts with their SIL licenses, avoiding blocked Google requests without weakening CSP. Added a matching M favicon.
- Preserved the existing design and seven study tracks. There is no fabricated AI integration or guaranteed hiring outcome.

## Verification evidence

- Maven `verify`: 17 tests, 0 failures/errors/skips; executable JAR packaged successfully.
- GitHub Actions: https://github.com/uday-payghon/mission90-career-command-center/actions/runs/42474483714 passed Java 21 tests and packaging for the application commit.
- Docker build: passed, multi-stage image with a non-root runtime, 256 MB maximum Java heap and 144 MB metaspace limit.
- Browser syntax checks: `app.js` and `enhancements.js` passed.
- Source secret scan: 35 files checked, no owner password, Neon password, private key or recognized token patterns found.
- Local Chrome against the actual Neon PostgreSQL database: login/logout, checkbox check/uncheck and refresh, task details, DSA create/edit, application create/edit and dates, journal, JSON backup, and private-API logout rejection passed.
- A full local JVM restart retained task, edited DSA, edited application and journal records. Only explicitly disposable QA records were removed afterward; 630 curriculum tasks remained.
- Desktop screenshots of overview, daily tasks, roadmap, DSA and applications were inspected. A 390px mobile viewport had no page overflow. No JavaScript runtime or console errors remained after self-hosting fonts.
- Live HTTPS: 200 for login and readiness (`UP`); HTTP redirects to HTTPS; anonymous private API requests return 401.
- Live Chrome workflow: login, check/uncheck, refresh, task note/duration/priority, DSA create/edit, application create/edit and dates, journal and backup all passed. Session cookies were Secure, HTTP-only and SameSite=Lax; logout removed private API access.
- Initial Render deployment `dep-dbo5ikajnfac73dv35ng` reached LIVE at 2026-11-08 11:16:33 UTC.
- Exact-commit redeployment `dep-dbo5l49srm7s73b19teg` reached LIVE at 2026-11-08 11:21:10 UTC. A fresh browser session verified saved task, edited DSA, edited application and journal data survived server replacement in Neon. QA-only records were cleaned afterward and the original 630 tasks preserved.
- Post-redeploy mobile checks passed for overview, daily tasks (with task details expanded), roadmap, DSA, applications and journal at 390px. Browser runtime and console checks passed, including font loading. Reviewed 61 redeploy/startup log lines with no application ERROR, startup-failure or out-of-memory entries observed.
- Screenshots after cleanup: [overview](docs/screenshots/live-overview.png), [daily](docs/screenshots/live-today.png), [roadmap](docs/screenshots/live-roadmap.png), [DSA](docs/screenshots/live-dsa.png), [applications](docs/screenshots/live-applications.png), [mobile](docs/screenshots/live-mobile.png).
- A later documentation-only commit records this evidence; the deployed Java/static/Docker application files remain identical to the application commit above.

## Scope and limitations

This is a single-owner application, not a multi-user service. It has no staff registration, multi-tenant row isolation, password-recovery email, MFA, distributed login throttling, live AI provider, or background email/push worker. These must not be advertised as implemented. Existing ChatGPT reminders were not changed and do not synchronize with this app.

The cloud database contains no imported citizen/project data. Temporary QA records are clearly synthetic and cleaned after persistence verification. H2 is local development only; cloud startup requires PostgreSQL/TLS. JSON backups contain private notes; download and store them privately. Rehearse restore and any future migration before changing schema.

Render Free sleeps after inactivity and can take roughly a minute or more to wake; deploy/startup on its small CPU can also take minutes. Its disk is ephemeral, so all durable application records live in Neon. Sessions are not durable and require login after server replacement. Free quotas and limited restore history apply; there is no paid-resource provisioning or uptime guarantee. Use independent backups rather than treating a successful restart test as a disaster-recovery guarantee.

Official references, accessed 2026-11-08: [Render Free](https://render.com/docs/free), [Render health checks](https://render.com/docs/health-checks), [Neon pricing](https://neon.com/pricing), [Neon scale to zero](https://neon.com/docs/manage/computes#scale-to-zero).

## Files

Updated from the supplied ZIP: `pom.xml`, `Dockerfile`, `compose.yaml`, `render.yaml`, `.gitignore`, `.env.example`, `.github/workflows/tests.yml`, `README.md`, `DEPLOY_CHECKLIST.md`; Java `SecurityConfig`, `MissionApi`, `PlanSeeder`, `TaskItem`, `DsaProblem`, `JobApplication`, `Repositories`; static `index.html`, `login.html`, `app.js`, `style.css`; existing integration tests.

Added: Flyway `V1__mission_schema.sql`, `MissionWorkflowTest`, `enhancements.js`, `favicon.svg`, two local font files and two license files, and this report. Existing unrelated projects and the source ZIP were not modified.
