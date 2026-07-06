# 🌿 repo-clean

> **Automated stale branch detection, approval-gated deletion, and organisation-wide cleanup for GitHub repositories — built with Spring Boot, PostgreSQL, and React.**

---

## 📋 Table of Contents

- [What It Does](#-what-it-does)
- [Architecture Overview](#-architecture-overview)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [How the Pipeline Works](#-how-the-pipeline-works)
- [Database Schema](#-database-schema)
- [Getting Started](#-getting-started)
- [Configuration Reference](#-configuration-reference)
- [Running with Podman / Docker](#-running-with-podman--docker)
- [Manually Triggering Schedulers](#-manually-triggering-schedulers)
- [Dashboard](#-dashboard)
- [Known Issue & Fix — Jackson Compatibility](#-known-issue--fix--jackson-compatibility)
- [API Reference](#-api-reference)
- [Contributing](#-contributing)

---

## 🔍 What It Does

Large engineering organisations accumulate hundreds of stale branches across dozens of repositories — branches where the last commit was months ago and the author has long since moved on. Left unmanaged these clutter repository UIs, slow down tooling, and create confusion.

**repo-clean** automates the full lifecycle of identifying, notifying, and safely deleting those branches:

1. **Scans** your entire GitHub organisation every week and identifies every branch with no commit in the last 90 days
2. **Emails** the last committer of each stale branch with a per-repository digest, asking them to approve or deny deletion — all via a single tokenised link, no login required
3. **Waits** for an explicit Approve or Deny decision per branch
4. **Reminds** the approver 12 hours before deletion actually happens
5. **Deletes** the branch from GitHub automatically once the reminder window has elapsed
6. **Surfaces** everything on a live React dashboard — stat cards, filterable tables, charts

No branch is ever deleted without an explicit human approval.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        GitHub Organisation                       │
│         (hundreds of repos, many stale branches)                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │ GitHub API (kohsuke/github-api)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Backend                           │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  ┌────────┐  │
│  │ Scheduler 1  │  │ Scheduler 2  │  │ Sched. 3 │  │Sched.4 │  │
│  │ Sunday 02:00 │  │ Sunday 08:00 │  │ /15 min  │  │/15 min │  │
│  │ Detect stale │  │ Send emails  │  │ Reminder │  │Delete  │  │
│  └──────┬───────┘  └──────┬───────┘  └────┬─────┘  └───┬────┘  │
│         │                 │               │             │        │
│         └─────────────────▼───────────────▼─────────────▼──────┤
│                      PostgreSQL Database                         │
│         (organisations, repositories, stale_branches,           │
│          detection_runs, email_audit)                            │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  ApprovalController  (Thymeleaf server-side web pages)   │   │
│  │  GET /approve?token=JWT&action=APPROVE|DENY              │   │
│  │  POST /approve/confirm                                   │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  DashboardApiController  (REST JSON)                     │   │
│  │  GET /api/dashboard/stats                                │   │
│  │  GET /api/dashboard/stale-branches                       │   │
│  │  GET /api/dashboard/deleted-branches                     │   │
│  │  GET /api/dashboard/emailed-users                        │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
                           │
                           │ nginx proxy (/api/*, /approve)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                React Frontend (Vite + TailwindCSS)               │
│                                                                  │
│   Overview · Stale Branches · Deleted · Emailed Users · Charts  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.3 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| GitHub API client | `org.kohsuke:github-api:1.321` |
| Email | Spring Mail + Thymeleaf HTML templates |
| Approval auth | HMAC-SHA256 signed JWT (jjwt 0.11.5) — token embedded in email link |
| Frontend | React 18 + Vite 5 + TailwindCSS 3 |
| Charts | Recharts |
| Routing | React Router DOM v6 |
| Container runtime | Podman / Docker |
| Web server (frontend) | nginx 1.25 |

---

## 📁 Project Structure

```
repo-clean/
│
├── docker-compose.yml
├── .env.example
│
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/github_stale/repo_clean/
│       ├── RepoCleanApplication.java
│       ├── config/
│       │   ├── AppConfig.java
│       │   ├── GitHubConfig.java
│       │   ├── SchedulingConfig.java
│       │   └── SecurityConfig.java
│       ├── domain/
│       │   ├── Organisation.java
│       │   ├── Repository.java
│       │   ├── DetectionRun.java
│       │   ├── StaleBranch.java          ← core state machine
│       │   └── EmailAudit.java
│       ├── repository/                   ← Spring Data JPA interfaces
│       ├── scheduler/
│       │   ├── BranchDetectionScheduler.java   ← Scheduler 1
│       │   ├── EmailDispatchScheduler.java      ← Scheduler 2
│       │   ├── ReminderScheduler.java           ← Scheduler 3
│       │   └── BranchDeletionScheduler.java     ← Scheduler 4
│       ├── service/
│       │   ├── GitHubService.java
│       │   ├── TokenService.java
│       │   ├── EmailService.java
│       │   └── DashboardService.java
│       ├── controller/
│       │   ├── ApprovalController.java
│       │   └── DashboardApiController.java
│       └── dto/
│
│   └── src/main/resources/
│       ├── application.yml
│       ├── db/migration/V1__init_schema.sql
│       └── templates/
│           ├── email/approval-request.html
│           ├── email/reminder.html
│           └── approval/{confirm,result,error,already-done}.html
│
└── frontend/
    ├── Dockerfile
    ├── nginx.conf
    ├── src/
    │   ├── App.jsx
    │   ├── api/dashboard.js
    │   ├── hooks/useFetch.js
    │   ├── components/
    │   │   ├── Sidebar.jsx
    │   │   ├── PageHeader.jsx
    │   │   ├── SearchBar.jsx
    │   │   └── ui.jsx          ← StatCard, StatusBadge, Table, etc.
    │   └── pages/
    │       ├── Overview.jsx
    │       ├── StaleBranches.jsx
    │       ├── DeletedBranches.jsx
    │       ├── EmailedUsers.jsx
    │       └── Dashboard.jsx   ← Recharts page
    └── package.json
```

---

## ⚙️ How the Pipeline Works

### Branch Status State Machine

Every stale branch record moves through these states — it can never skip a step or go backwards:

```
PENDING
  │
  ▼ (Scheduler 2 sends approval email)
EMAIL_SENT
  │
  ├──► DENIED      ← committer clicks "Deny" → flow stops permanently
  │
  ▼ (committer clicks "Approve")
APPROVED           ← scheduled_delete_at = now + 12 hours
  │
  ▼ (Scheduler 3 fires when within 1 hour of scheduled_delete_at)
REMINDER_SENT
  │
  ├──► FAILED      ← GitHub API delete call failed
  │
  ▼ (Scheduler 4 fires after scheduled_delete_at has passed)
DELETED
```

### Scheduler 1 — Stale Branch Detection
**Cron: every Sunday at 02:00 UTC**

- Fetches all non-archived repositories in every configured GitHub organisation
- For each repo, walks every branch and checks the last commit date
- Any branch with a last commit older than `app.stale-days` (default: **90 days**) is saved to the database with `status = PENDING`
- Skips protected branches: `main`, `master`, `develop`, `development`, `staging`, `production`, and any branch starting with `release/` or `hotfix/`
- Duplicate-safe: will not create a second record for the same branch within the same detection run

### Scheduler 2 — Email Dispatch
**Cron: every Sunday at 08:00 UTC**

- Reads all `PENDING` branches
- Groups them by `(committer_email × repository)` — so one committer with stale branches in 3 repos receives **3 separate emails**, one per repo; multiple stale branches in a single repo appear as separate cards in **one email**
- Generates a time-limited HMAC-SHA256 signed JWT per branch, embedded as a URL parameter in each Approve/Deny button
- Email contains: repo name, branch name, last commit message, last commit date, and two CTA buttons per branch
- Sets status to `EMAIL_SENT`

### Committer Approval Flow
- Committer clicks **Approve Deletion** or **Deny Deletion** in the email
- Lands on a Thymeleaf-rendered confirmation page at `GET /approve?token=XXX&action=APPROVE|DENY`
- Confirms via button → `POST /approve/confirm`
- **Approve**: `status → APPROVED`, `scheduled_delete_at = now + 12 hours`
- **Deny**: `status → DENIED`, flow stops permanently — branch is never touched again
- Idempotent: clicking the same link twice shows an "already processed" page
- Expired/invalid tokens show a friendly error page

### Scheduler 3 — 12-Hour Reminder
**Runs every 15 minutes**

- Looks for `APPROVED` branches where `scheduled_delete_at` is within the next 60 minutes
- Sends a final reminder email to the committer including the branch name, exact deletion datetime, and the commit SHA for recovery if needed
- Sets status to `REMINDER_SENT`

### Scheduler 4 — Branch Deletion
**Runs every 15 minutes**

- Looks for `REMINDER_SENT` branches where `scheduled_delete_at` has passed
- Calls the GitHub API to delete the branch ref (`heads/<branch>`)
- On success: `status → DELETED`, `deleted_at = now`
- On failure: `status → FAILED`, logged for investigation

---

## 🗄️ Database Schema

```sql
organisations        ← one row per GitHub org
repositories         ← one row per repo, FK to organisations
detection_runs       ← one row per Scheduler 1 execution, audit trail
stale_branches       ← core table; one row per branch per detection run
email_audit          ← every email sent (type, recipient, status)
```

Key columns on `stale_branches`:

| Column | Purpose |
|---|---|
| `status` | State machine value (PENDING → … → DELETED) |
| `approval_token` | JWT embedded in email link buttons |
| `approved_at` | Timestamp when committer clicked Approve |
| `scheduled_delete_at` | `approved_at + deletion_delay_hours` |
| `reminder_sent_at` | When the 12-hr reminder was dispatched |
| `deleted_at` | When the GitHub API deletion was confirmed |

---

## 🚀 Getting Started

### Prerequisites

- Podman 4+ (or Docker) and `podman-compose` (or `docker-compose`)
- A **GitHub Organisation** (not a personal account — the API call uses `getOrganization()`)
- A GitHub **Personal Access Token** with `Contents: Read & Write` and `Metadata: Read` permissions
- An SMTP mail account (Gmail with an App Password works well)

### 1. Clone the repository

```bash
git clone https://github.com/anas-s-neo/repo-clean.git
cd repo-clean
```

### 2. Create your `.env` file

```bash
cp .env.example .env
```

Edit `.env` and fill in your real values:

```env
# GitHub
GITHUB_TOKEN=ghp_your_personal_access_token
GITHUB_ORGS=your-github-organisation-name

# Mail (Gmail example — use an App Password, not your real password)
MAIL_HOST=smtp.gmail.com
MAIL_USER=you@gmail.com
MAIL_PASS=your16characterapppassword
MAIL_FROM=you@gmail.com

# App
APP_BASE_URL=http://localhost:8080
TOKEN_SECRET=replace-with-at-least-32-random-characters

# Set false to disable auto-scheduling (recommended for first run)
SCHEDULING_ENABLED=false
```

### 3. Build and start the stack

```bash
podman-compose build
podman-compose up -d
podman-compose logs -f backend
```

Wait for:
```
Started RepoCleanApplication in X.XXX seconds
```

### 4. Verify everything is up

```bash
curl http://localhost:8080/actuator/health    # → {"status":"UP"}
curl http://localhost:8080/api/dashboard/health  # → OK
curl -o /dev/null -w "%{http_code}" http://localhost:3000  # → 200
```

Open the dashboard: **http://localhost:3000**

---

## ⚙️ Configuration Reference

All values are set via environment variables or `application.yml`.

| Property | Env var | Default | Description |
|---|---|---|---|
| `github.token` | `GITHUB_TOKEN` | — | GitHub PAT with Contents R/W |
| `github.organisations` | `GITHUB_ORGS` | — | Comma-separated org names to scan |
| `app.stale-days` | — | `90` | Days without a commit = stale |
| `app.deletion-delay-hours` | — | `12` | Hours between Approve and actual deletion |
| `app.token-expiry-days` | — | `7` | JWT approval link validity |
| `app.base-url` | `APP_BASE_URL` | `http://localhost:8080` | Used to build approval URLs in emails |
| `app.token-secret` | `TOKEN_SECRET` | — | HMAC secret for JWT signing (≥32 chars) |
| `scheduling.enabled` | `SCHEDULING_ENABLED` | `true` | Set `false` to disable all cron triggers |
| `scheduling.branch-detection-cron` | — | `0 0 2 * * SUN` | Scheduler 1 cron |
| `scheduling.email-dispatch-cron` | — | `0 0 8 * * SUN` | Scheduler 2 cron |
| `scheduling.reminder-check-rate-ms` | — | `900000` | Scheduler 3 poll rate (ms) |
| `scheduling.deletion-check-rate-ms` | — | `900000` | Scheduler 4 poll rate (ms) |

---

## 🐳 Running with Podman / Docker

```bash
# Start full stack (db + backend + frontend)
podman-compose up -d

# View backend logs live
podman-compose logs -f backend

# Stop (preserve data)
podman-compose down

# Stop and wipe database volume (clean slate)
podman-compose down -v

# Rebuild after code changes
podman-compose build --no-cache backend
podman-compose up -d backend
```

Services exposed on the host:

| Service | Port | Description |
|---|---|---|
| Frontend (nginx) | `3000` | React dashboard |
| Backend (Spring Boot) | `8080` | API + approval web pages |
| PostgreSQL | `5432` | Database (dev access) |

---

## 🔧 Manually Triggering Schedulers

For local testing without waiting for the Sunday cron, add a `TestTriggerController` to fire each scheduler on demand:

```java
// src/main/java/.../controller/TestTriggerController.java
// ⚠️ DELETE BEFORE DEPLOYING TO PRODUCTION

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestTriggerController {

    private final BranchDetectionScheduler detectionScheduler;
    private final EmailDispatchScheduler emailScheduler;
    private final ReminderScheduler reminderScheduler;
    private final BranchDeletionScheduler deletionScheduler;

    @PostMapping("/trigger/detection")
    public String detection() { detectionScheduler.detectStaleBranches(); return "Scheduler 1 triggered"; }

    @PostMapping("/trigger/email")
    public String email() { emailScheduler.sendApprovalEmails(); return "Scheduler 2 triggered"; }

    @PostMapping("/trigger/reminder")
    public String reminder() { reminderScheduler.sendReminders(); return "Scheduler 3 triggered"; }

    @PostMapping("/trigger/deletion")
    public String deletion() { deletionScheduler.deleteApprovedBranches(); return "Scheduler 4 triggered"; }
}
```

Then call them:

```bash
curl -X POST http://localhost:8080/api/test/trigger/detection
curl -X POST http://localhost:8080/api/test/trigger/email
# → check inbox, click Approve, then fast-forward the timer:
podman-compose exec db psql -U postgres -d stalebranchdb -c \
  "UPDATE stale_branches SET scheduled_delete_at = NOW() + INTERVAL '1 minute' WHERE status='APPROVED';"
curl -X POST http://localhost:8080/api/test/trigger/reminder
podman-compose exec db psql -U postgres -d stalebranchdb -c \
  "UPDATE stale_branches SET scheduled_delete_at = NOW() - INTERVAL '1 minute' WHERE status='REMINDER_SENT';"
curl -X POST http://localhost:8080/api/test/trigger/deletion
```

---

## 📊 Dashboard

The React frontend at `http://localhost:3000` has five pages:

| Page | What it shows |
|---|---|
| **Overview** | Stat cards (Pending / Email Sent / Approved / Denied / Reminder Sent / Deleted) + mini-tables of the latest detection run and recent deletions |
| **Stale Branches** | Searchable, filterable table of all stale branches — filter by status or free-text search across branch name, repo, and committer |
| **Deleted Branches** | Full audit table of every branch deleted by the system, with committer and deletion timestamp |
| **Emailed Users** | Every committer who has been notified, with a count of emails received |
| **Charts** | Recharts pie (status breakdown), horizontal bar (top repos by stale count), vertical bar (deletions per committer) |

---

## ⚠️ Known Issue & Fix — Jackson Compatibility

**Symptom** (seen on Spring Boot 4.x builds):

```
Caused by: java.lang.NoSuchFieldError: Class com.fasterxml.jackson.databind.PropertyNamingStrategy
does not have member field 'com.fasterxml.jackson.databind.PropertyNamingStrategy SNAKE_CASE'
    at org.kohsuke.github.GitHubClient.<clinit>(GitHubClient.java:92)
```

**Root cause:** `github-api:1.321` was compiled against an older Jackson version where `PropertyNamingStrategy.SNAKE_CASE` was a static field. Spring Boot 4.x pulls in a newer Jackson release where that field was removed in favour of `PropertyNamingStrategies.SNAKE_CASE`.

**Fix:** Force a compatible Jackson version in `backend/pom.xml`. Add this block immediately after your `</parent>` tag and before `<dependencies>`:

```xml
<!-- Force Jackson 2.15.x — required for github-api:1.321 compatibility -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson</groupId>
            <artifactId>jackson-bom</artifactId>
            <version>2.15.4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then rebuild:

```bash
podman-compose build --no-cache backend
podman-compose up -d backend
```

This project targets **Spring Boot 3.2.3**. If the banner shows `v4.1.0`, your Maven parent version in `pom.xml` has been overridden — verify the `<parent>` block specifies `<version>3.2.3</version>`.

---

## 📡 API Reference

### Dashboard REST API

All endpoints return JSON. Base path: `/api/dashboard`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/stats` | Pipeline counts for all statuses |
| `GET` | `/stale-branches` | All stale branches ever detected |
| `GET` | `/stale-branches/today` | Branches from the most recent detection run |
| `GET` | `/deleted-branches` | All deleted branches, newest first |
| `GET` | `/emailed-users` | All notified committers with email count |
| `GET` | `/health` | Simple health check → `OK` |

### Approval Flow (server-rendered)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/approve?token=JWT&action=APPROVE\|DENY` | Renders confirmation page |
| `POST` | `/approve/confirm` | Processes the confirmation, renders result |

Approval tokens are HMAC-SHA256 signed JWTs, 7-day expiry. Invalid or expired tokens render a friendly error page — not a 500.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Make your changes and add tests
4. Rebuild and verify the full pipeline manually using the test trigger endpoints
5. Submit a pull request

**Before submitting a PR:**
- [ ] Remove any `TestTriggerController` from your branch
- [ ] Ensure `SCHEDULING_ENABLED=false` is not hardcoded
- [ ] Run the existing test suite: `mvn test -pl backend`
- [ ] Confirm the Jackson compatibility fix is present in `pom.xml` if targeting Spring Boot 4.x

---

## 📄 License

This project is licensed under the MIT License.

---

<div align="center">
  <sub>Built with Spring Boot · PostgreSQL · React · Podman</sub>
</div>
