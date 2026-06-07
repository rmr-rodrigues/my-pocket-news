# Phase 1 — [Phase Name]

<!-- TEMPLATE — replace all [PLACEHOLDERS] before use.
     This file is the product spec for Phase 1.
     It describes WHAT is being built and WHY.
     It does not contain task IDs, file lists, or implementation code.

     The companion file todo-01.md contains the implementation task list.

     Generation prompt (use with a reasoning model):
     "Based on docs/01-product-brief.md, docs/02-mvp-scope.md,
     docs/03-technical-architecture.md, and the Phase 1 summary in
     docs/04-implementation-phases.md, generate the Phase 1 product spec
     following the structure in docs/phases/phase-01/phase-01.md." -->

---

## Goal

<!-- One or two paragraphs. What does this phase produce?
     What becomes possible that was not possible before?
     What does a developer or user gain at the end of this phase?

EXAMPLE:
Phase 1 produces a working indexer. By the end of this phase, all existing
OpenCode sessions are searchable by querying the database directly — no UI,
no API, just real data in a real schema.

The watcher runs continuously and picks up new sessions automatically. The
frontend and API exist only as scaffolded shells, ready to be built on in
Phase 2.
-->

[GOAL]

---

## What Is Not in This Phase

<!-- Explicit deferrals. Prevents scope creep and prevents agents from
     reaching for infrastructure that does not exist yet.

EXAMPLE:
- Tagging pipeline — no chunk_tag rows are written (Phase 3)
- Markdown file indexing (Phase 3)
- Search API endpoint (Phase 2)
- Any real UI — React app is a placeholder page only
- FastAPI beyond a health check
-->

- [DEFERRAL]
- [DEFERRAL]

---

## Feature 1 — [Feature Name]

<!-- One section per feature. Write in prose — not a task list.
     Describe behaviour, rules, product decisions, and edge cases.
     Explain WHY decisions were made, not just what was decided.

EXAMPLE (from a session indexer project):

## Feature 1 — Infrastructure and Docker Compose

The entire stack runs with a single `docker compose up`. All data persists
across container restarts via bind mounts — no named Docker volumes.
Everything is visible and accessible directly on the host filesystem.

A single Python Docker image is used by both the `api` and `indexer` services,
with different `command` entries in `docker-compose.yml`. The `frontend` service
uses the official Node image directly — no custom Dockerfile needed in Phase 1.

External paths are machine-specific and must not be committed. They live in
`config/mounts.env`, copied from `config/mounts.env.example` before first run.
-->

[DESCRIPTION]

---

## Feature 2 — [Feature Name]

<!-- Repeat the feature section pattern as many times as needed.
     Each section covers one coherent area of functionality.

     Include tables where they clarify rules concisely.

EXAMPLE (schema decisions):

## Feature 2 — Database Schema

`cockpit.db` is a SQLite database created by the indexer on first run.
WAL mode is enabled so the API service can read concurrently while the
indexer writes.

**Tables:**

`session` — one row per session. `indexed_at` is null until the session
has been fully processed; the indexer uses this to track what still needs
indexing.

`chunk` — one row per unit of indexed text. `chunk_type` is either
`content` (real text) or `break` (compaction marker, no text).
-->

[DESCRIPTION]

---

## Feature 3 — [Feature Name]

[DESCRIPTION]

---

## Definition of Done

<!-- Observable, independently verifiable outcomes.
     Each item should be checkable without interpretation.
     These are mirrored in the verification checklist in todo-01.md.

EXAMPLE:
- `docker compose up` starts all three services without errors
- Indexer logs show a completed full scan with a non-zero session count
- `SELECT COUNT(*) FROM chunk WHERE chunk_type = 'content'` returns a non-zero result
- `GET http://localhost:8000/health` returns `{"status": "ok", ...}`
- Watcher detects a new session within 30 seconds and indexes it automatically
-->

- [CRITERION]
- [CRITERION]
- [CRITERION]

---

## Outcome

<!-- Filled in retrospectively when the phase is complete.
     Summarise what was actually built, any deviations from the plan,
     and what Phase 2 can now build on. Leave blank until done. -->
