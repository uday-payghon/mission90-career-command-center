# MISSION/90 — deployment checklist

## Preparation
- [ ] Create a private GitHub repository called `mission90-career-command-center` and upload this folder, excluding secrets and generated data.
- [ ] Ensure `mvn test` passes with Java 21 locally or in GitHub Actions.
- [ ] Create a free Neon project and PostgreSQL database. Store database password privately.
- [ ] Import the GitHub repository to Render as a Blueprint; select free service.
- [ ] Configure `APP_USER`, `APP_PASSWORD`, `JDBC_DATABASE_URL`, `DB_USER` and `DB_PASSWORD` in Render environment settings. Never paste passwords into GitHub files.
- [ ] Confirm HTTPS and secure session cookies.

## Health and data validation
- [ ] Visit `https://YOUR-SERVICE.onrender.com/actuator/health` and confirm `UP`.
- [ ] Visit `/login.html`, sign in with your owner credentials.
- [ ] Check a November 9 checkbox; reload; confirm it remains checked.
- [ ] Add a personal task, mark it complete; change date and return; confirm persistence.
- [ ] Log a solved DSA problem and an actual job application; refresh; confirm persistence.
- [ ] Save a reflection; refresh; confirm persistence.
- [ ] Verify progress bars and lifetime skill charts update after completion.
- [ ] Download a JSON backup and ensure it contains expected data.
- [ ] Test mobile layout and log out/in behavior.
- [ ] Check logs for application/database errors and ensure PostgreSQL is active.

## Free-hosting note
Render spin-down after inactivity is expected. Neon free plan quotas apply. Backend hosting is **not complete** merely because a GitHub repo exists; only share the public URL after successful deploy and these checks.
