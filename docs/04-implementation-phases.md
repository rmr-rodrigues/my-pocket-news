# 04 — Implementation Phases

---

## Strategy

The guiding constraint is working software first. The goal is to share a URL and receive a summary notification on a real device as early as possible, before investing in UI polish, search, or advanced configuration. Phase 1 is the complete end-to-end flow. Later phases add refinements, convenience features, and extensibility.

The initial test audience is the developer (dogfooding on a personal device).

---

## Phase Summary

| Phase | Focus | Room DB | WorkManager | LLM | UI |
|---|---|---|---|---|---|
| 1 — Core MVP | Share → Extract → Summarise → Persist | Yes | Yes | Yes | Basic list + detail + settings |
| 2 — Refinements | Manual entry, search, retry, article management | Yes | Yes | Yes | Enhanced |
| 3 — Extensibility | Local models, multi-level summaries, export | Yes | Yes | Yes + local | Enhanced |

---

## Phase 1 — Core MVP

**Goal:** A developer can share any static-HTML news article URL from their Android device and receive a notification with the title when processing is complete; opening the app shows the summary and extracted body.

### In Scope

- Android project scaffold: Gradle (Kotlin DSL), min SDK 26, single-module app
- Room database with the `articles` table
- `SettingsScreen` to configure LLM provider, API key, and model name
- `EncryptedSharedPreferences`-backed `SettingsRepository`
- `ShareActivity` with `ACTION_SEND` / `text/plain` Intent Filter
- `ArticleProcessingWorker` (CoroutineWorker): fetch HTML → Jsoup + Readability4J extraction → LLM call → persist
- `LlmClient` interface with `OpenAiLlmClient` and `OpenRouterLlmClient` implementations
- Notification channels: processing (silent foreground) and completion/error
- `ArticleListScreen`: flat list sorted by `createdAt` descending, status indicators
- `ArticleDetailScreen`: title, summary, extracted body, link to original URL
- Jetpack Navigation Compose wiring

### Out of Scope

- Search or filtering of saved articles
- Manual URL entry (only Share intent)
- Re-summarisation of existing articles
- Tags, folders, or any organisational structure
- JavaScript-rendered pages
- Paywall bypass
- Cloud sync or backup
- Unit or instrumented tests (deferred to Phase 2)

### Definition of Done

- Sharing a static HTML news article URL opens My Pocket News, shows a silent notification "Processing article…"
- Within a reasonable time (network dependent), a completion notification appears: "Article saved: [title]"
- Opening the app shows the article in the list with its summary preview
- Tapping the article shows the full detail screen with summary and extracted body
- Sharing a URL when no LLM provider is configured navigates the user to Settings
- Sharing an unreachable URL results in a FAILED article visible in the list with an error message

### Task Summary

| Task | Description |
|---|---|
| INFRA-1 | Android project scaffold and Gradle configuration |
| INFRA-2 | Room database, entity, DAO, and database class |
| INFRA-3 | EncryptedSharedPreferences-backed settings storage |
| BG-1 | ArticleProcessingWorker: HTML fetch and article extraction pipeline |
| BG-2 | LlmClient interface and provider implementations |
| BG-3 | Notification channels and notification posting |
| FE-1 | Settings screen with provider, key, and model inputs |
| FE-2 | Article list screen with status indicators |
| FE-3 | Article detail screen |
| FE-4 | ShareActivity, Intent Filter, and WorkManager enqueue |

→ Full task detail: [docs/phases/phase-01/todo-01.md](phases/phase-01/todo-01.md)

---

## Phase 2 — Refinements

**Goal:** The app is convenient and robust enough for daily personal use: manual URL entry, search, retry on failed articles, and basic article management (delete).

### In Scope

- Manual URL entry from within the app (no Share required)
- Full-text search across saved articles (title + summary + body)
- Retry action for FAILED articles
- Delete article action (swipe-to-delete or contextual menu)
- Unit tests for the extraction pipeline and LlmClient implementations
- Improved error messages surfaced in the UI

### Out of Scope

- Tags or folders
- Cloud sync
- Multiple LLM configuration profiles

### Definition of Done

- User can add an article by pasting a URL directly in the app without using the Share menu
- Searching for a word in an article title returns that article in results
- A FAILED article shows a "Retry" button that re-enqueues the processing job
- Swiping an article in the list deletes it with an undo snackbar

---

## Phase 3 — Extensibility

**Goal:** The app supports local LLM models (on-device inference) and offers more flexible summarisation options, enabling fully offline use.

### In Scope

- Integration with a local model runtime (e.g. llama.cpp via JNI, or MLC LLM)
- Multi-level summaries: one-sentence TL;DR + paragraph summary
- Export: share summary as plain text
- `LlmClient` extended to support local model providers without network access

### Out of Scope

- Cloud sync
- Multiple user profiles

### Definition of Done

- User can configure a local model in Settings and summarise an article without any network call to an external API
- Each article detail screen shows both a one-sentence TL;DR and a paragraph summary
- User can share the summary text via the Android Share sheet

---
