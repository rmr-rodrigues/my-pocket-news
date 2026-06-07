# AGENTS.md

## Purpose

This file is the first-stop operational guide for any LLM coding agent working in this repository.
Read this file in full before making any changes or assumptions.

---

## Repository State

<!-- Update this section as the project evolves -->

Current repository contents:

- `README.md` — project hub and navigation index
- `AGENTS.md` — this file
- `TODO.md` — live progress index (current phase + links to phase checklists)
- `docs/01-product-brief.md` — product vision and positioning
- `docs/02-mvp-scope.md` — MVP entities and validation rules
- `docs/03-technical-architecture.md` — stack and infrastructure decisions
- `docs/04-implementation-phases.md` — phase plan with goals and definitions of done
- `docs/phases/phase-01/phase-01.md` — detailed task breakdown for phase 1
- `docs/phases/phase-01/todo-01.md` — executable checklist for phase 1

Currently present:

- [ ] Build configuration (`build.gradle.kts`, `package.json`, `vite.config.ts`, etc.)
- [ ] Source code
- [ ] Test configuration
- [ ] Lint configuration
- [ ] CI workflow files

> Do not invent build or test commands. Do not report unverified commands as working.
> If tooling is absent, say so explicitly.

---

## Repository Structure

The canonical file layout for this template is:

```
project/
├── README.md                                  ← hub: one-liner, status, quick links, key decisions
├── AGENTS.md                                  ← this file
├── TODO.md                                    ← phase index: current phase, status, links to todo-NN.md
│
└── docs/
    ├── 01-product-brief.md                    ← vision, problem, users, value proposition
    ├── 02-mvp-scope.md                        ← entities, in/out of scope, validation rules
    ├── 03-technical-architecture.md           ← stack, infrastructure, key decisions
    ├── 04-implementation-phases.md            ← phase summaries with goals and definitions of done
    └── phases/
        ├── phase-01/
        │   ├── phase-01.md                    ← complete task breakdown for phase 1
        │   └── todo-01.md                     ← executable checklist for phase 1
        ├── phase-02/
        │   ├── phase-02.md
        │   └── todo-02.md
        └── ...
```

**Rules:**
- File names are fixed. Do not rename spec docs or phase files.
- Each phase lives in its own subfolder: `docs/phases/phase-NN/`.
- The number suffix keeps files unambiguous when multiple phase folders are open simultaneously.
- `TODO.md` at the root is a **phase index only** — it links to `todo-NN.md` files, not a full task list.
- Optional supplementary docs (e.g. `competitor-comparison.md`) go directly in `docs/`.

### The Two Phase Documents

Each phase produces exactly two files with distinct responsibilities:

---

**`phase-NN.md` — Product spec (the WHAT and WHY)**

Written in prose. Answers: what does this phase build, why does each decision
exist, what is explicitly out of scope, and how do we know the phase is done?

Contains:
- Goal (one paragraph)
- What is not in this phase (explicit deferrals)
- One section per feature — behaviour, rules, edge cases, product decisions
- Definition of Done (observable, verifiable outcomes)
- Outcome (filled in retrospectively when the phase is complete)

Does NOT contain:
- File lists or folder structures
- Implementation code or algorithms
- Task IDs or checklists
- Instructions to agents

---

**`todo-NN.md` — Implementation guide (the HOW and IN WHAT ORDER)**

Written as a structured task list. Answers: what exactly must be created or
modified, in what order, with what observable outcome?

Contains:
- Quick context (repos, versions, relevant paths, commit conventions)
- Conventions (project-specific patterns agents must follow)
- Tasks grouped by feature, each with a short ID (`FEAT-1`, `DB-2`, etc.)
- Per task: what it produces, any relevant data structures (SQL schema,
  JSON shape, key types), files to create or modify, dependencies on other tasks
- Phase verification checklist (ordered, each item independently verifiable)
- Open items (things that do not block this phase)

**Task status is signalled with a checkbox in the task heading:**
```
### [ ] INFRA-1 — docker-compose.yml   ← pending
### [x] INFRA-1 — docker-compose.yml   ← done
```
This is the only status format used. The frontend parses these headings to
show task progress per phase. Agents update the checkbox when a task is complete.

Does NOT contain:
- Implementation logic, algorithms, or full function bodies
- Prose descriptions of product behaviour (that belongs in `phase-NN.md`)
- Generated or speculative code

---

The guiding principle: a product manager can read `phase-NN.md` without
knowing the tech stack. An engineer implementing a task reads `todo-NN.md`
without needing to understand the full product vision.

### Naming Conventions

| Convention | Rule |
|---|---|
| Spec documents | `NN-kebab-case.md` with two-digit prefix (01, 02, ...) |
| Phase folders | `phase-NN` with two-digit prefix (01, 02, ...) |
| Phase spec files | `phase-NN.md` inside `docs/phases/phase-NN/` |
| Phase task lists | `todo-NN.md` inside `docs/phases/phase-NN/` |
| Placeholders | `[SCREAMING_SNAKE_CASE]` for required, `<!-- comment -->` for optional |
| Task IDs | Short prefix + number: `INFRA-1`, `DB-2`, `API-3`, `FE-1` |

### Phase Lifecycle

When a phase is ready to implement:
1. Generate `docs/phases/phase-NN/phase-NN.md` — product spec (see `TEMPLATE_GUIDE.md`)
2. Generate `docs/phases/phase-NN/todo-NN.md` — implementation task list
3. Update `TODO.md` root index: set current phase, add row for the new phase with link
4. Update `README.md` status table

When a phase is complete:
1. Mark all tasks done in `todo-NN.md`
2. Fill in the **Outcome** section of `phase-NN.md` (what was actually built)
3. Update `TODO.md` root index: mark phase complete
4. Update `README.md` status to next phase
5. Generate the next phase's files

---

## Project Type

<!-- Mark all that apply -->

- [ ] Web app (frontend)
- [ ] Mobile app (Android / iOS)
- [ ] API / Backend
- [ ] Full-stack (web + backend)
- [ ] Monorepo multi-app
- [ ] CLI / scripts

---

## Tech Stack

<!-- Fill in once decided. Leave blank or mark TBD if not yet decided. -->

| Concern | Choice | Notes |
|---|---|---|
| Frontend framework | [e.g. React 18 + TypeScript] | |
| Build tool | [e.g. Vite] | |
| Map / UI library | [e.g. MapLibre GL JS] | |
| State management | [e.g. Zustand] | |
| Backend framework | [e.g. NestJS] | |
| Database | [e.g. PostgreSQL + PostGIS] | |
| Background jobs | [e.g. pg-boss] | |
| Object storage | [e.g. Cloudflare R2] | |
| Auth | [e.g. Supabase Auth] | |
| Hosting — frontend | [e.g. Cloudflare Pages] | |
| Hosting — backend | [e.g. Hetzner + Coolify] | |
| Mobile | [e.g. Android / Kotlin / Jetpack Compose] | |

---

## Build Commands

<!-- Update when build tooling is configured -->

Current status: **No build command available** — no build tool configuration is present.

When configured, expected commands will be recorded here:

```
# Example (do not use until verified):
# npm run dev         — start dev server
# npm run build       — production build
# ./gradlew build     — Android/Kotlin build
```

Detection order when verifying:
1. `package.json` → `npm run dev / build / test`
2. `gradlew` / `gradlew.bat` → `./gradlew build`
3. Other task runners or scripts

---

## Lint Commands

Current status: **No lint command available** — no linter configuration is present.

```
# When configured:
# npm run lint
# ./gradlew lint
# ./gradlew detekt
# ./gradlew ktlintCheck
```

---

## Test Commands

Current status: **No test command available** — no test runner configuration is present.

```
# When configured:
# npm test                                             — run all tests
# ./gradlew test                                       — Kotlin unit tests
# ./gradlew test --tests "com.example.MyTest"          — single test class
# ./gradlew :app:testDebugUnitTest                     — Android unit tests
# ./gradlew :app:connectedDebugAndroidTest             — Android instrumented tests
```

Single-test guidance:
- Prefer the narrowest supported target
- Use a fully-qualified class name when possible
- Re-run the exact failing test after fixing it

---

## Code Style

### Formatting

- Keep formatting tool-driven when a formatter exists
- Do not manually fight the formatter
- Keep line lengths readable for diffs
- Avoid vertical padding that does not improve readability

### Imports

- Keep imports explicit and minimal
- Remove unused imports
- Avoid wildcard imports unless the project style already uses them

### Types

- Prefer concrete, readable types at public boundaries
- Allow local type inference where the type is obvious
- Avoid `Any`, overly broad nullable types, or stringly-typed structures
- Model domain concepts with named types

### Naming

- Use descriptive names tied to the project domain (see Domain Vocabulary below)
- Use full words over abbreviations unless the abbreviation is established
- Name functions after behavior, not implementation detail
- Name booleans as predicates: `isValid`, `hasGpsFix`, `shouldPublish`

### Functions and Classes

- Keep functions focused and small enough to read without scrolling
- Prefer straightforward control flow over clever abstractions
- Extract helpers only when they remove repetition or isolate meaningful logic
- Avoid deep class hierarchies without evidence they are needed

### Error Handling

- Fail loudly on impossible states
- Return structured errors where callers need to react
- Include actionable context in error messages
- Do not swallow exceptions silently
- Validate external input early

### Nullability and State

- Prefer explicit handling over unsafe assumptions
- Keep mutable state narrow and well-scoped
- Initialize state to valid values when possible

---

## Testing Style

- Add tests for behavior, not implementation trivia
- Prefer one clear assertion path per test
- Name tests after the expected outcome
- Cover edge cases around parsing, validation, and domain transformations
- When fixing a bug, add or update a test that would have caught it

---

## Domain Vocabulary

<!-- Fill in the key terms for this project's domain.
     Agents should use these exact terms in code, comments, and documentation. -->

| Term | Meaning |
|---|---|
| [term] | [definition] |
| [term] | [definition] |

---

## Agent Workflow

Follow this protocol on every task:

1. Read `README.md` and this file before doing anything
2. Read the relevant `docs/` files for context
3. Read `TODO.md` to identify the current phase, then open `docs/phases/phase-NN/todo-NN.md`
4. Read `docs/phases/phase-NN/phase-NN.md` for the full task detail
5. Look for existing build, lint, and test configuration files
6. Follow existing local patterns before introducing new ones
7. Make the smallest correct change
8. Run the narrowest available verification
9. Report what was verified versus what remains unverified
10. Update `todo-NN.md` after completing a task; update `TODO.md` index when phase status changes

---

## Documentation Expectations

- Update `README.md` status table when a phase changes state
- Update `todo-NN.md` as tasks are completed within a phase
- Update `TODO.md` root index when phase status changes
- Update this file when build/test/lint commands become concrete
- Update the **Repository State** section when new files are added
- Document new commands immediately after adding tooling

### Language and Task Descriptions

- All documentation, task titles, descriptions, comments, and prose must be written in **English**.
- Task headings in `todo-NN.md` must use descriptive human-readable titles — not raw identifiers, function signatures, or endpoint paths. The title must convey what the task produces or enables, not how it is implemented. The technical detail (endpoint path, function name, file) belongs in the task body.

  **Good:** `BE-2 — Endpoint to list sessions for a project`
  **Bad:** `BE-2 — GET /projects/{project_id}/sessions`

  **Good:** `FE-4 — Projects browser page listing all indexed projects`
  **Bad:** `FE-4 — ProjectsBrowserPage`

---

## Dashboard Compatibility

The Neat Builds Cockpit dashboard reads `TODO.md` and `todo-NN.md` files directly from disk
and parses them with strict regex rules. If the format deviates, the project shows as
incompatible in the dashboard. The rules below are normative.

### `TODO.md` — required format

**Current phase line** — must appear verbatim, with an em-dash (`—`) as separator:

```
**Current phase:** Phase N — Phase Name
```

- `N` must be an integer.
- The separator must be `—` (U+2014 em-dash). A hyphen `-` is also accepted but em-dash is canonical.
- This line is what the dashboard reads to determine the active phase.

**Phase index table** — must be a markdown table with exactly four columns in this order:

```markdown
| Phase | Name | Status | Checklist |
|---|---|---|---|
| 1 | Foundation | Complete | [todo-01.md](docs/phases/phase-01/todo-01.md) |
| 2 | Search UI  | In Progress | [todo-02.md](docs/phases/phase-02/todo-02.md) |
| 3 | Metadata   | Not Started | — |
```

- **Status** must be exactly one of: `Complete`, `In Progress`, `Not Started`, `Blocked`.
  Any other value is logged as a warning and may render incorrectly.
- **Checklist column**: if the cell contains a markdown link `[text](url)`, the dashboard
  marks `has_todo = true` and enables the task progress bar. A plain `—` or any text
  without a link sets `has_todo = false`.
- **`has_spec`** is determined by whether the file
  `docs/phases/phase-NN/phase-NN.md` exists on disk — it is not read from this table.

### `todo-NN.md` — required format

Tasks are parsed from `##` group headings and `###` task headings. Content outside
a `##` heading (e.g. preamble, conventions) is ignored by the parser.

```markdown
## Group Title

### [ ] TASK-1 — Task title here

Optional description text. Can span multiple lines and include code blocks.
Everything between this heading and the next heading is the task description.

### [x] TASK-2 — Another task (completed)
```

Rules:
- **Group heading**: `##` followed by any text. Creates a named task group.
- **Task heading**: `###` followed by `[ ]` (pending) or `[x]` (done), case-insensitive.
- **Task ID**: optional prefix before ` — ` or ` - ` (e.g. `BE-1`, `FE-2`, `INFRA-3`).
  If present, it is shown as a badge in the task list. If absent, no badge is shown.
- **Task title**: everything after the separator. Must be a human-readable description
  of what the task produces, not a file path or function name.
- **Description**: all lines between the task `###` heading and the next heading.
  Rendered as markdown in the dashboard. May be empty.

### Compatibility check

A project is shown as **compatible** in the dashboard if `TODO.md` contains both:
1. A parseable `**Current phase:**` line, **or**
2. At least one valid row in the phase index table.

A project missing `TODO.md`, or whose `TODO.md` matches neither condition, is shown
with an incompatibility warning. It is never hidden from the list.

---

## Current Bottom Line

<!-- Keep this section short and brutally honest about the actual state of the repo -->

At the time this file was written:

- This repository contains only planning and spec documents
- No source code exists yet
- No build, lint, or test configuration is present
- No runnable commands can be verified

When the repository grows beyond planning docs, update this file immediately.
