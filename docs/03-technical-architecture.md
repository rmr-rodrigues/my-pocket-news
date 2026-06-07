# 03 — Technical Architecture

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────┐
│                  Android App (single process)        │
│                                                      │
│  ┌─────────────────────────────────────────────────┐ │
│  │  UI Layer (Jetpack Compose)                     │ │
│  │  ArticleListScreen / ArticleDetailScreen /      │ │
│  │  SettingsScreen                                 │ │
│  └────────────────────┬────────────────────────────┘ │
│                       │ ViewModel + StateFlow         │
│  ┌────────────────────▼────────────────────────────┐ │
│  │  Domain / Repository Layer                      │ │
│  │  ArticleRepository  LlmClient (interface)       │ │
│  └──────────┬─────────────────────┬───────────────┘ │
│             │                     │                  │
│  ┌──────────▼──────┐   ┌──────────▼──────────────┐  │
│  │  Room Database  │   │  WorkManager             │  │
│  │  (SQLite)       │   │  ArticleProcessingWorker │  │
│  └─────────────────┘   └──────────┬───────────────┘  │
│                                   │                  │
│                         ┌─────────▼─────────────┐   │
│                         │  OkHttp               │   │
│                         │  ├─ HTML fetch        │   │
│                         │  └─ LLM API call      │   │
│                         └───────────────────────┘   │
└──────────────────────────────────────────────────────┘

External:
  ┌──────────────────────┐
  │  LLM API             │
  │  (OpenAI-compatible) │
  └──────────────────────┘
```

---

## Tech Stack Decisions

| Concern | Choice | Reason |
|---|---|---|
| Language | Kotlin | Android standard; null-safety, coroutines, concise syntax |
| UI framework | Jetpack Compose | Modern Android UI; no XML layouts; declarative state model |
| Local database | Room (SQLite) | Android-native ORM; type-safe queries; no external DB process |
| Networking | OkHttp | Mature, lightweight HTTP client; no DI framework required |
| HTML extraction | Jsoup | Java-native HTML parser; no JS runtime; fast and reliable for static HTML |
| Article extraction | Readability4J | Kotlin/Java port of Mozilla Readability; extracts clean article body and title; no JS runtime required |
| Background processing | WorkManager | Android-recommended for deferrable, guaranteed background work; survives process death and reboots |
| Async | Kotlin Coroutines + Flow | Standard Android async; integrates with Room, WorkManager, and Compose |
| Secure storage | EncryptedSharedPreferences | Android Jetpack security library; API key never stored in plaintext |
| DI | Manual / constructor injection | No Hilt/Dagger in v1; keep the dependency graph simple and explicit |
| Build | Gradle (Kotlin DSL) | Standard Android build system |
| Min SDK | Android 8.0 (API 26) | WorkManager and EncryptedSharedPreferences support; covers ~98% of active devices |

---

## Components

### UI Layer (Jetpack Compose)

**Technology:** Kotlin + Jetpack Compose
**Hosting:** On-device (Android app)

Screens:
- `ArticleListScreen` — sorted list of saved articles with status indicators (processing spinner, failed badge, summary preview)
- `ArticleDetailScreen` — full extracted body, summary, title, and link to original URL
- `SettingsScreen` — provider selector (OpenRouter / OpenAI), API key input, model name input

Key design principles:
- Each screen has one ViewModel; state is a single sealed `UiState` exposed as `StateFlow`
- No business logic in Composables — all logic lives in ViewModels or the repository layer
- Navigation handled by Jetpack Navigation Compose with a single `NavHost`

---

### Repository Layer

**Technology:** Kotlin

`ArticleRepository`:
- Single source of truth for `Article` data
- Exposes `Flow<List<Article>>` from Room for the list screen
- Delegates background processing to WorkManager (enqueues work, does not run it inline)

`LlmClient` (interface):
```kotlin
interface LlmClient {
    suspend fun summarise(title: String, bodyText: String): String
}
```
- One implementation per provider: `OpenAiLlmClient`, `OpenRouterLlmClient`
- Both use the OpenAI-compatible `/v1/chat/completions` endpoint
- The difference is `baseUrl` and optional headers (e.g. `HTTP-Referer` for OpenRouter)
- Selected at runtime based on the persisted `LlmProviderConfig`

---

### Background Processing (WorkManager)

**Technology:** WorkManager + Kotlin Coroutines (`CoroutineWorker`)

`ArticleProcessingWorker`:

1. Receives the article URL as input data
2. Fetches raw HTML via OkHttp
3. Parses and extracts article content using Jsoup (fetch + DOM prep) → Readability4J (article extraction)
4. Reads `LlmProviderConfig` from `EncryptedSharedPreferences`
5. Builds the appropriate `LlmClient` implementation
6. Calls `LlmClient.summarise(title, bodyText)`
7. Updates the `Article` record in Room with status `DONE`, summary, title, and timestamps
8. Posts a completion notification via `NotificationManager`
9. On any unrecoverable error: updates status to `FAILED` with `errorMessage`

Processing notification:
- Posted when work begins (Foreground Service via `setForeground()` in `CoroutineWorker`)
- Silent (no sound, low priority) — informs the OS to keep the process alive
- Dismissed automatically when work completes

---

### Local Database (Room)

**Technology:** Room 2.x (SQLite on-device)

Tables:

| Table | Purpose |
|---|---|
| `articles` | Persists all saved Article records |

Schema:

```sql
CREATE TABLE articles (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    url           TEXT    NOT NULL,
    title         TEXT    NOT NULL DEFAULT '',
    summary       TEXT    NOT NULL DEFAULT '',
    body_text     TEXT    NOT NULL DEFAULT '',
    provider_used TEXT    NOT NULL DEFAULT '',
    model_used    TEXT    NOT NULL DEFAULT '',
    status        TEXT    NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at    INTEGER NOT NULL,
    processed_at  INTEGER
);
```

`ArticleDao`:
- `insert(article: Article): Long`
- `update(article: Article)`
- `getAll(): Flow<List<Article>>`
- `getById(id: Long): Article?`

---

### Secure Settings Storage

**Technology:** `EncryptedSharedPreferences` (Jetpack Security)

Keys stored:
- `pref_provider` — provider identifier string
- `pref_api_key` — API key (never logged or transmitted except to the LLM endpoint)
- `pref_model` — model name string
- `pref_base_url` — API base URL

Accessed via a `SettingsRepository` that wraps `EncryptedSharedPreferences` and exposes typed getters/setters.

---

## LLM API Integration

Both providers use the OpenAI-compatible chat completions endpoint.

**Request shape:**

```json
{
  "model": "<model_name>",
  "messages": [
    {
      "role": "system",
      "content": "You are a news summariser. Summarise the following article in 3–5 sentences in the same language as the article. Be factual and concise."
    },
    {
      "role": "user",
      "content": "<article_title>\n\n<article_body_text>"
    }
  ],
  "max_tokens": 512,
  "temperature": 0.3
}
```

**Response shape (relevant fields):**

```json
{
  "choices": [
    {
      "message": {
        "content": "<summary_text>"
      }
    }
  ]
}
```

**Provider-specific differences:**

| Concern | OpenAI | OpenRouter |
|---|---|---|
| Base URL | `https://api.openai.com/v1` | `https://openrouter.ai/api/v1` |
| Auth header | `Authorization: Bearer <key>` | `Authorization: Bearer <key>` |
| Extra headers | — | `HTTP-Referer: android-app://com.mypocketnews` |
| Model name format | `gpt-4o-mini` | `openai/gpt-4o-mini` or `mistralai/...` |

---

## Notification Channels

| Channel ID | Name | Importance | Use |
|---|---|---|---|
| `processing` | Article processing | `IMPORTANCE_LOW` | Silent foreground notification during WorkManager job |
| `completion` | Article ready | `IMPORTANCE_DEFAULT` | Shown when article is successfully summarised |
| `error` | Processing error | `IMPORTANCE_DEFAULT` | Shown when processing fails |

---

## Security Considerations

- The API key is stored exclusively in `EncryptedSharedPreferences` and never written to Room, logs, or shared preferences in plaintext
- Network calls use HTTPS only; OkHttp enforces TLS
- No data is sent to any server except the configured LLM endpoint and the article's origin server
- The app declares `INTERNET` permission only; no location, contacts, or storage access

---

## Alternatives Considered

| Concern | Alternative | Reason rejected |
|---|---|---|
| HTML extraction | WebView with JS execution | Heavy, async, requires UI thread; overkill for static HTML articles |
| HTML extraction | Mozilla Readability JS via J2V8 | Adds a JS runtime dependency (~5 MB); `readability4j` provides equivalent results without a runtime |
| Background jobs | Foreground Service (manual) | WorkManager is the Android-recommended approach and handles process death, retries, and battery optimisation automatically |
| Networking | Retrofit | Adds annotation processing; OkHttp directly is sufficient for two fixed endpoints |
| DI | Hilt | Adds compile-time complexity; the dependency graph is small enough for manual injection in v1 |
| Storage | DataStore (Proto) | EncryptedSharedPreferences is simpler for a small set of typed scalar settings |
