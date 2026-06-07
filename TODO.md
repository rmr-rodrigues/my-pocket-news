# Todo

<!-- This file is the phase index for this project.
     It tracks current phase status and links to the per-phase checklists.
     Detailed task checklists live in docs/phases/phase-NN/todo-NN.md. -->

---

**Current phase:** Phase 1 — Core MVP
**Status:** `In Progress`
**Last updated:** 2026-06-07

---

## Phase Index

| Phase | Name | Status | Checklist |
|---|---|---|---|
| 1 | Core MVP | In Progress | [todo-01.md](docs/phases/phase-01/todo-01.md) |
| 2 | Refinements | Not Started | — |
| 3 | Extensibility | Not Started | — |

---

## Notes

<!-- Decisions, blockers, or context that does not belong in a spec doc -->

- LLM provider abstraction uses OpenAI-compatible `/v1/chat/completions` for both OpenAI and OpenRouter
- Article extraction pipeline: OkHttp (fetch) → Jsoup (HTML parse/clean) → Readability4J (content extraction)
- API key stored in EncryptedSharedPreferences only — never in Room or logs
