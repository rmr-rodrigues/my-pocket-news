# 03 — Technical Architecture

<!-- This document defines the technical decisions for this project.
     It covers the stack, infrastructure, deployment strategy, and key architectural choices.
     Focus on the WHY behind each decision, not just the WHAT. -->

---

## Architecture Overview

<!-- ASCII diagram showing the high-level components and how they communicate.
     Adapt to the project type — remove layers that do not apply. -->

```
┌─────────────────────────────────────┐
│         Client (Browser / App)      │
│  [Framework] + local state          │
│  client-side validation             │
└────────────────┬────────────────────┘
                 │ HTTPS / REST + JSON
┌────────────────▼────────────────────┐
│           API Server                │
│  auth, [domain modules]             │
└──────┬──────────────────┬───────────┘
       │                  │
┌──────▼──────┐    ┌──────▼──────────┐
│  Database   │    │   Job Queue     │
│  [choice]   │    │   [choice]      │
└─────────────┘    └──────┬──────────┘
                          │
                   ┌──────▼──────────┐
                   │  Worker Process  │
                   │  [async jobs]    │
                   └──────┬──────────┘
                          │
                   ┌──────▼──────────┐
                   │  Object Storage  │
                   │  [choice]        │
                   └─────────────────┘
```

---

## Tech Stack Decisions

<!-- For each concern, record the chosen technology and the reason.
     This makes it easy for new agents to understand constraints
     without re-opening discussions that are already decided. -->

| Concern | Choice | Reason |
|---|---|---|
| Frontend framework | [e.g. React 18 + TypeScript] | [e.g. team familiarity, ecosystem] |
| Build tool | [e.g. Vite] | [e.g. fast dev server, minimal config] |
| UI / Map library | [e.g. MapLibre GL JS] | [e.g. open source, no API key required] |
| State management | [e.g. Zustand] | [e.g. simple, no boilerplate] |
| Backend framework | [e.g. NestJS] | [e.g. modular, TypeScript-native] |
| Database | [e.g. PostgreSQL + PostGIS] | [e.g. spatial queries, managed via Supabase] |
| Background jobs | [e.g. pg-boss] | [e.g. PostgreSQL-backed, no extra infra] |
| Object storage | [e.g. Cloudflare R2] | [e.g. zero egress cost] |
| Auth | [e.g. Supabase Auth] | [e.g. managed, JWT-compatible] |
| Mobile | [e.g. Android / Kotlin / Jetpack Compose] | [e.g. native performance, Compose UI] |
| Monorepo | [e.g. npm workspaces] | [e.g. native to npm, no extra tooling] |

---

## Components

<!-- Describe each major component: its technology, hosting, and responsibilities.
     Remove components that do not apply to this project. -->

### Frontend / Client

**Technology:** [e.g. React + TypeScript + Vite]
**Hosting:** [e.g. Cloudflare Pages (free tier)]

Responsibilities:
- [responsibility 1 — e.g. render the map and all interactive editing tools]
- [responsibility 2 — e.g. manage local editing state without backend calls]
- [responsibility 3 — e.g. run lightweight client-side validation in real time]
- [responsibility 4 — e.g. trigger import, validation, and publication via API calls]

Key design principles:
- [principle 1 — e.g. editing state lives entirely in the browser between saves]
- [principle 2 — e.g. no per-action API calls during map/canvas interaction]
- [principle 3 — e.g. backend is called for: initial load, save, import, publish]

---

### Backend (API)

**Technology:** [e.g. TypeScript + NestJS]
**Hosting:** [e.g. Hetzner Cloud VPS via Docker + Coolify]

Responsibilities:
- [responsibility 1 — e.g. authentication and authorization]
- [responsibility 2 — e.g. project and organization management]
- [responsibility 3 — e.g. accepting file uploads and queuing import jobs]
- [responsibility 4 — e.g. running validation before publication]

Module structure:

```
src/
  auth/           # authentication, JWT verification
  [module-1]/     # [description]
  [module-2]/     # [description]
  [module-3]/     # [description]
  jobs/           # job queue setup and definitions
  storage/        # object storage client abstraction
  common/         # shared types, utilities, error handling
```

---

### Worker

**Technology:** [e.g. TypeScript — same codebase as API]
**Hosting:** [e.g. same VPS, separate Docker process]

Responsibilities:
- [responsibility 1 — e.g. process import jobs: parse uploaded files]
- [responsibility 2 — e.g. normalize imported data into the database]
- [responsibility 3 — e.g. generate output files on publish]

Job types:

| Job | Trigger | What it does |
|---|---|---|
| `[job.name]` | [trigger] | [description] |
| `[job.name]` | [trigger] | [description] |

---

### Database

**Technology:** [e.g. PostgreSQL + PostGIS]
**Hosting:** [e.g. Supabase (managed, includes PostGIS)]

Main tables:

| Table | Purpose |
|---|---|
| `[table_name]` | [purpose] |
| `[table_name]` | [purpose] |
| `[table_name]` | [purpose] |

Special capabilities used:
- [e.g. PostGIS `GEOMETRY(Point, 4326)` for spatial queries]
- [e.g. spatial index on geometry columns]

---

### Object Storage

**Technology:** [e.g. Cloudflare R2 (S3-compatible)]

Storage paths:

| Path pattern | Contents |
|---|---|
| `[path/{id}]` | [e.g. raw uploaded import files] |
| `[path/{id}]` | [e.g. generated output archives] |

Why this choice:
- [reason 1 — e.g. zero egress cost — output files are polled frequently]
- [reason 2 — e.g. S3-compatible API — easy to swap if needed]

---

### Mobile App (if applicable)

**Technology:** [e.g. Android / Kotlin / Jetpack Compose]
**Distribution:** [e.g. Google Play / direct APK]

Responsibilities:
- [responsibility 1 — e.g. GPS data capture with foreground service]
- [responsibility 2 — e.g. offline-first data collection]
- [responsibility 3 — e.g. export to formats compatible with the web platform]

---

## Authentication

**Approach:** [e.g. Supabase Auth / JWT / OAuth]

- [auth detail 1 — e.g. email/password login with magic link option]
- [auth detail 2 — e.g. JWT issued by Supabase, verified by the API]
- [auth detail 3 — e.g. organization membership checked on every protected route]

Deferred to later:
- [e.g. multi-provider OAuth (Google, etc.)]

---

## Infrastructure Layout

```
[Hosting provider — Frontend]
  └── [domain] (frontend static build)

[Hosting provider — Storage]
  └── [domain] (output file hosting)

[Hosting provider — Backend]
  └── [Orchestration tool]
        ├── [service-name] (API, Docker)
        └── [service-name] (Worker, Docker)

[Database provider]
  └── PostgreSQL [+ PostGIS if applicable]
  └── Auth [if applicable]
```

---

## Deployment Strategy

**Development:**

| Resource | Option | Cost |
|---|---|---|
| VPS (backend) | [e.g. Hetzner CPX11] | [e.g. ~€4/mo] |
| Database | [e.g. Supabase free tier] | €0 |
| Storage | [e.g. Cloudflare R2 free tier] | €0 |
| Frontend | [e.g. Cloudflare Pages free tier] | €0 |
| **Total** | | **~€X/mo** |

**Production (estimated at [N] active users/clients):**

| Resource | Option | Cost |
|---|---|---|
| VPS (backend) | [e.g. Hetzner CPX21] | [e.g. ~€8/mo] |
| Database | [e.g. Supabase Pro] | [e.g. ~$25/mo] |
| Storage | [e.g. Cloudflare R2] | [e.g. ~$1/mo] |
| Frontend | [e.g. Cloudflare Pages free tier] | €0 |
| **Total** | | **~€X/mo** |

**CI/CD:**
- [e.g. GitHub Actions for tests and Docker image builds]
- [e.g. Coolify handles deployment via webhook]

---

## Client-Side Editing Model

<!-- Only relevant for products with a rich editing UI (map editor, canvas, form-heavy).
     Remove this section if not applicable. -->

### Conclusion

The editing experience is **local-first on the client**:

- data is loaded from the backend into the browser once
- user works entirely locally — changes accumulate in local state
- backend is called only when needed (save, import, publish)

### What happens on the client

- [e.g. moving stops on the map]
- [e.g. editing route geometry]
- [e.g. reordering sequences]
- [e.g. lightweight real-time validation feedback]

### What requires the backend

- [e.g. persisting changes permanently]
- [e.g. importing and parsing files]
- [e.g. full validation]
- [e.g. generating output files]
- [e.g. publishing]

---

## Security Considerations

- [e.g. all communication over HTTPS]
- [e.g. JWT tokens verified on every API request]
- [e.g. org isolation enforced at the query level]
- [e.g. uploaded files checked for size limits; no execution of uploaded content]
- [e.g. public-facing output URLs are intentionally public]

---

## Scalability Notes

- [e.g. the modular monolith allows extracting the worker as a separate service later]
- [e.g. the API is stateless and can be scaled horizontally behind a load balancer]
- [e.g. database connection pooling via PgBouncer or managed pooler]
- [e.g. object storage scales transparently without application changes]

For v1, [describe the simplest infrastructure that is sufficient].

---

## Alternatives Considered

<!-- Record alternatives that were evaluated and rejected.
     This prevents re-opening the same discussions in future. -->

| Concern | Alternative | Reason rejected |
|---|---|---|
| [concern] | [alternative] | [reason — e.g. "too expensive at this scale"] |
| [concern] | [alternative] | [reason] |
