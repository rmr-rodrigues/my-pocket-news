# Phase 1 — Task List

<!-- TEMPLATE — replace all [PLACEHOLDERS] before use.
     This file is the implementation guide for Phase 1.
     It describes HOW to build what phase-01.md specifies, in what order,
     and how to verify each step.

     It does NOT contain product decisions, feature descriptions in prose,
     or implementation logic. Those belong in phase-01.md.

     Task IDs must appear in the corresponding git commit messages.
     Example: "INFRA-1: add docker-compose.yml"

     TASK STATUS — use a checkbox in the heading to signal completion:
       ### [ ] INFRA-1 — docker-compose.yml   ← pending
       ### [x] INFRA-1 — docker-compose.yml   ← done
     The frontend reads these headings to show task progress.
     Do not use any other status format. -->

> Full product decisions and feature descriptions: [phase-01.md](phase-01.md)

---

## Quick Context

<!-- Operational facts agents need before touching any file.
     Repos, deployed URLs, current schema versions, conventions.

EXAMPLE:
- Backend repo: `my-backend`; deployed on Fly.io (`https://my-backend.fly.dev`)
- Flutter repo: `my-app`; debug APK runs on CPH2219
- Last Alembic migration: `0012`; next is `0013`
- Drift schema: `v5`; next migration will be `v6`
- Commit convention: `BE-XX: description` for backend, `APP-XX: description` for app
-->

- [CONTEXT]
- [CONTEXT]

---

## Conventions

<!-- Project-specific patterns agents must follow.
     Language, framework, file naming, database, etc.

EXAMPLE:

### Backend
- New tables follow the pattern in `alembic/versions/`
- All UUIDs use `sa.UUID(as_uuid=True)` with `default=uuid.uuid4`
- Foreign keys: `sa.ForeignKey("table.column", ondelete="CASCADE")`

### Flutter
- New drift tables go in `lib/core/database/tables/`
- After any drift change: `dart run build_runner build --delete-conflicting-outputs`
- Verify with `flutter analyze` — zero errors before marking task complete
-->

[CONVENTIONS]

---

## Feature 1 — [Feature Name]

<!-- Group tasks by feature, mirroring the structure of phase-01.md.
     Tasks within a feature are ordered by dependency. -->

### [ ] [PREFIX]-1 — [Task Name]

<!-- Short description of what this task produces (2-4 sentences).
     Not a restatement of product behaviour — that is in phase-01.md.
     Focus on the artefact: what file is created, what schema is added,
     what endpoint is registered.

EXAMPLE:

### [ ] INFRA-1 — docker-compose.yml

Three services: `indexer`, `api`, `frontend`. All use bind mounts.
External paths are loaded from `config/mounts.env` via `env_file`.

**Service: indexer**
- Image: built from `Dockerfile` at repo root
- Command: `python indexer/main.py`
- Volumes: opencode.db (ro), workspace root (ro), `./data/` (rw), `./config/` (ro)
- `restart: unless-stopped`

**Depends on:** —
-->

[DESCRIPTION]

**Depends on:** —

---

### [ ] [PREFIX]-2 — [Task Name]

<!-- For database tasks: include the full SQL schema.
     Schemas are spec, not implementation — they belong here.

EXAMPLE:

### [ ] DB-1 — Create session and chunk tables

Migration `0013_add_session_chunk_tables.py`.

```sql
CREATE TABLE sessions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE chunks (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id  UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    role        VARCHAR NOT NULL,   -- 'user' | 'assistant'
    text        TEXT NOT NULL,
    sequence    INTEGER NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_chunks_session_id ON chunks (session_id, sequence);
```

**Decisions:**
- `role` is stored as a plain string — no enum in Phase 1
- `sequence` allows ordering without relying on insertion order

**Files to create/modify:**
- `alembic/versions/0013_add_session_chunk_tables.py`
- `app/models/session.py` (new)
- `app/models/chunk.py` (new)
- `alembic/env.py` — import new models

**Depends on:** —
-->

[DESCRIPTION]

**Files to create/modify:**
- [FILE]

**Depends on:** [PREFIX]-1

---

### [ ] [PREFIX]-3 — [Task Name]

<!-- For API tasks: include the JSON request/response shape.

EXAMPLE:

### [ ] API-1 — GET /health endpoint

Returns API status and whether the database file exists on disk.

**Response shape:**
```json
{
  "status": "ok",
  "db_path": "/data/cockpit.db",
  "db_exists": true
}
```

**Decisions:**
- `db_exists` is checked at request time — not cached
- No authentication in Phase 1

**Files to create/modify:**
- `api/main.py` — register route

**Depends on:** —
-->

[DESCRIPTION]

**Files to create/modify:**
- [FILE]

**Depends on:** [PREFIX]-2

---

## Feature 2 — [Feature Name]

### [ ] [PREFIX]-4 — [Task Name]

[DESCRIPTION]

**Files to create/modify:**
- [FILE]

**Depends on:** [PREFIX]-1

---

## Phase Verification

<!-- Work through these in order. Each must pass before marking the phase complete.
     These mirror the Definition of Done in phase-01.md.
     Each item must be independently verifiable — no "it seems to work".

EXAMPLE:
- [ ] `docker compose config` runs without errors
- [ ] `docker compose up` starts all three services without errors
- [ ] `SELECT COUNT(*) FROM chunk` returns a non-zero result
- [ ] `GET http://localhost:8000/health` returns `{"status": "ok", "db_exists": true}`
- [ ] Watcher detects a new session within 30 seconds: row count increases by 1
-->

- [ ] [VERIFICATION]
- [ ] [VERIFICATION]
- [ ] [VERIFICATION]

---

## Open Items

<!-- Things that do not block this phase but must be tracked for later.

EXAMPLE:
- No unit tests in Phase 1. `chunker.py` is a good candidate for tests in Phase 2.
- `--retag` flag is a no-op. Full implementation in Phase 3.
- The JSON shape of `part.data` in opencode.db should be confirmed against a
  real database row before finalising IDX-2.
-->
