# 02 — MVP Scope

---

## Core MVP User Flow

1. User finds an article in any Android app (browser, Twitter, Reddit, etc.)
2. User taps the system Share button and selects "My Pocket News"
3. The app enqueues a background job and shows a silent processing notification
4. WorkManager fetches the HTML, extracts the article content using Jsoup + Readability4J, and calls the configured LLM API
5. The LLM returns a summary; the app persists the article and summary to Room
6. A completion notification is shown: "Article saved: [title]"
7. User opens the app and sees the article in the list with its summary
8. User taps the article to read the full extracted body or open the original URL

---

## MVP Entities

### Article

An Article represents a saved piece of web content with its extracted text and LLM-generated summary.

| Field | Type | Required | Notes |
|---|---|---|---|
| `id` | `Long` (auto-generated) | yes | Room primary key, auto-increment |
| `url` | `String` | yes | Original URL received from the Share intent; must be non-empty and a valid HTTP/HTTPS URL |
| `title` | `String` | yes | Extracted from the article HTML; fallback to URL hostname if extraction fails |
| `summary` | `String` | yes | LLM-generated summary; non-empty if status is `DONE` |
| `bodyText` | `String` | no | Full extracted article body (plain text); may be empty if extraction yields no body |
| `providerUsed` | `String` | yes | Provider identifier used for summarisation (e.g. `"openrouter"`, `"openai"`) |
| `modelUsed` | `String` | yes | Model name used (e.g. `"gpt-4o-mini"`, `"mistralai/mistral-7b-instruct"`) |
| `status` | `Enum` | yes | `PENDING`, `PROCESSING`, `DONE`, `FAILED` |
| `errorMessage` | `String` | no | Populated if status is `FAILED`; null otherwise |
| `createdAt` | `Long` | yes | Unix timestamp (ms) at the moment of capture |
| `processedAt` | `Long` | no | Unix timestamp (ms) when processing completed; null until then |

Notes:

- `url` uniqueness is not enforced in v1 — the user may save the same URL twice intentionally
- `status` drives the UI state: list shows a spinner for `PROCESSING`, error badge for `FAILED`
- A `FAILED` article is retained in the database with its `errorMessage`; the user can see what went wrong

---

### LlmProviderConfig

Stores the user's configured LLM provider settings. There is exactly one active configuration at any time.

| Field | Type | Required | Notes |
|---|---|---|---|
| `provider` | `String` | yes | Provider identifier: `"openrouter"` or `"openai"` in v1 |
| `apiKey` | `String` | yes | User-supplied API key; stored in `EncryptedSharedPreferences`, not in Room |
| `model` | `String` | yes | Model name string, free-text to support any OpenAI-compatible model |
| `baseUrl` | `String` | yes | API base URL; defaults: OpenAI → `https://api.openai.com/v1`, OpenRouter → `https://openrouter.ai/api/v1` |

Notes:

- This configuration is stored in `EncryptedSharedPreferences`, not in the Room database
- If no configuration is present, the app shows a prompt to configure before processing can begin
- The `LlmClient` abstraction accepts this config at call time — no singleton dependency

---

## What Is NOT in MVP Scope

The following are explicitly out of the first version:

- Tags, categories, folders, or any organisational structure beyond the flat article list
- Search within saved articles
- Full HTML rendering — body text is plain text only
- Offline-mode article fetching (articles are fetched at time of sharing, not queued for later)
- JavaScript-rendered pages (SPAs with client-side-only rendering) — best-effort only via Readability4J
- Re-summarisation of an already-processed article
- Sharing or exporting articles
- Multiple LLM configuration profiles
- Cloud sync or backup
- Paywall bypass

---

## Input

The only input surface is the Android Share intent:

| Input | Notes |
|---|---|
| Share intent with `text/plain` MIME type containing an HTTP/HTTPS URL | Primary entry point |

Not supported in v1:

- Sharing images or files
- Manual URL entry within the app (may be added in Phase 2)
- RSS feed ingestion

---

## Output

| Output | Notes |
|---|---|
| In-app article list | All saved articles, sorted by `createdAt` descending |
| Article detail screen | Title, summary, full extracted body, original URL link |
| System notifications | Silent processing notification + completion notification |

---

## Validation Rules

### Processing Blockers (article moves to FAILED)

These prevent successful processing and result in a FAILED article with an error message:

- URL is not reachable (network error or non-2xx HTTP response)
- HTML fetch returns no content
- LLM API returns a non-2xx response or malformed JSON
- LLM API key is missing or rejected (401/403)
- Extraction yields neither a title nor any body text (completely empty page)

### Warnings (logged, not surfaced to user in v1)

- Readability4J extraction returns no body text but a title was found — article is saved with empty body
- LLM response is non-empty but shorter than 20 characters — saved as-is, no retry

---

## Single-User Model

This is a single-user, single-device application. There are no accounts, organisations, roles, or multi-tenancy concepts in any version of this product.

---

## Open Questions

- [ ] Should failed articles be retried automatically or only on explicit user action?
- [ ] What is the maximum article body length sent to the LLM (token budget)?
