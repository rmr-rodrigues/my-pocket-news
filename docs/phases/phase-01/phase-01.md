# Phase 1 — Core MVP

## Goal

Build the complete end-to-end flow: a user shares an article URL from any Android app, the app fetches and extracts the article content in the background, calls the configured LLM API, persists the result locally, and notifies the user when the summary is ready. At the end of this phase, the app is usable on a real device for daily personal reading triage.

---

## What Is Not in This Phase

- Search or filtering of saved articles
- Manual URL entry — only the Android Share intent
- Re-summarisation of existing articles
- Tags, folders, or organisational structure
- Unit or instrumented tests
- JavaScript-rendered pages (SPAs)
- Paywall bypass
- Cloud sync or backup
- Multiple LLM configuration profiles

---

## Features

### Share Intent Capture

The app registers an `Activity` with an `Intent Filter` for `ACTION_SEND` with MIME type `text/plain`. When the user shares a URL from any app (browser, Twitter, Reddit, etc.), Android presents My Pocket News in the share sheet.

On receiving the intent:
- The activity extracts the URL from the `EXTRA_TEXT` extra
- If the URL is not a valid HTTP/HTTPS URL, the activity shows a toast and finishes without saving
- If no LLM provider is configured in Settings, the activity navigates to `SettingsScreen` before enqueueing work
- Otherwise, the activity creates an `Article` record in Room with status `PENDING` and immediately enqueues an `ArticleProcessingWorker` for that article ID
- The activity finishes (does not stay open) — the user returns to the app they were using

The share capture must be instant from the user's perspective. The activity must not block on network or database calls on the main thread.

---

### Background Processing Pipeline

`ArticleProcessingWorker` runs as a `CoroutineWorker`. It is responsible for the full processing pipeline for a single article.

**Step 1 — Fetch HTML**

OkHttp performs a GET request to the article URL. The worker sets a `User-Agent` header that resembles a standard browser to avoid bot-blocking. A 10-second connection timeout and a 30-second read timeout are used.

If the request fails (network error, non-2xx response, or no body), the article is marked `FAILED` with a descriptive `errorMessage` and processing stops.

**Step 2 — Extract Article Content (Jsoup + Readability4J)**

The raw HTML string is passed to Jsoup to produce a cleaned `Document`. Readability4J then processes this document to extract:
- `title` — the article title (falls back to the `<title>` tag if Readability4J returns empty)
- `byline` — author (stored as part of `bodyText` header if present; not a separate field in v1)
- `content` (HTML) → stripped to plain text for `bodyText`

If both `title` and `bodyText` are empty after extraction, the article is marked `FAILED` with message `"Could not extract readable content from this page"`.

The body text sent to the LLM is truncated to a maximum of 8000 characters (approximately 2000 tokens) to stay within typical model context limits. Truncation happens at the last sentence boundary before the limit.

**Step 3 — LLM Summarisation**

The worker reads the `LlmProviderConfig` from `EncryptedSharedPreferences` and constructs the appropriate `LlmClient` implementation. It calls `summarise(title, bodyText)`.

The system prompt is fixed:
> "You are a news summariser. Summarise the following article in 3–5 sentences in the same language as the article. Be factual and concise."

The user message is:
> `"<title>\n\n<bodyText>"`

`max_tokens` is 512. `temperature` is 0.3.

If the LLM API returns a non-2xx response, the article is marked `FAILED` with the HTTP status code and response body (truncated to 200 chars) as the error message. A 401/403 response produces the specific message `"Invalid API key — check Settings"`.

**Step 4 — Persist and Notify**

On success:
- The `Article` record is updated: `status = DONE`, `title`, `summary`, `bodyText`, `providerUsed`, `modelUsed`, `processedAt`
- A completion notification is posted on the `completion` channel: title = "Article saved", body = the article title

On failure at any step:
- The `Article` record is updated: `status = FAILED`, `errorMessage`
- An error notification is posted on the `error` channel: title = "Could not save article", body = first 80 chars of `errorMessage`

**Foreground notification:**

When the worker starts, it calls `setForeground()` with a notification on the `processing` channel: "Processing article…". This is a silent, low-importance notification required to prevent Android from killing the worker process.

---

### LLM Provider Abstraction

`LlmClient` is a Kotlin interface:

```kotlin
interface LlmClient {
    suspend fun summarise(title: String, bodyText: String): String
}
```

Two implementations:
- `OpenAiLlmClient` — uses `https://api.openai.com/v1/chat/completions`
- `OpenRouterLlmClient` — uses `https://openrouter.ai/api/v1/chat/completions` with the additional `HTTP-Referer: android-app://com.mypocketnews` header

Both parse the response using Kotlin's built-in JSON parsing (or a minimal `org.json` parse of `choices[0].message.content`). No JSON serialisation library is added in v1.

A factory function `LlmClientFactory.create(config: LlmProviderConfig): LlmClient` selects the implementation based on `config.provider`.

---

### Settings Screen

A simple Compose screen with:
- Provider dropdown: "OpenAI" / "OpenRouter"
- API Key text field (password input, masked)
- Model name text field (free text, pre-filled with a sensible default per provider: `gpt-4o-mini` for OpenAI, `openai/gpt-4o-mini` for OpenRouter)
- Save button — writes to `EncryptedSharedPreferences` and shows a confirmation snackbar

The screen does not validate the API key against the network. Validation only happens at summarisation time.

---

### Article List Screen

A `LazyColumn` of article cards, sorted by `createdAt` descending. Each card shows:
- Article title (or URL hostname if title is empty)
- Summary preview (first 120 characters of summary, or "Processing…" / "Failed" if status is not `DONE`)
- Status indicator: circular progress for `PROCESSING`/`PENDING`, red badge for `FAILED`
- Relative time (e.g. "2 hours ago")

Tapping a card navigates to `ArticleDetailScreen`.

---

### Article Detail Screen

Shows:
- Title
- Summary (full text, no truncation)
- Extracted body text (scrollable, monospace or readable body font)
- "Open original" button — opens the URL in the system browser via an implicit intent
- If status is `FAILED`: error message displayed instead of summary/body

No editing is possible in v1.

---

### Notification Channels

Three channels registered at app startup (`Application.onCreate`):

| Channel ID | Name | Importance |
|---|---|---|
| `processing` | Article processing | `IMPORTANCE_LOW` |
| `completion` | Article ready | `IMPORTANCE_DEFAULT` |
| `error` | Processing error | `IMPORTANCE_DEFAULT` |

---

## Definition of Done

1. Sharing a static HTML news article URL (e.g. from a BBC, Guardian, or Reuters article) from the Android Chrome browser shows My Pocket News in the share sheet
2. After sharing, a silent "Processing article…" notification appears in the notification drawer
3. Within a reasonable time (network-dependent), the notification is replaced by "Article saved: [title]"
4. Opening My Pocket News shows the article in the list with a 3–5 sentence summary preview
5. Tapping the article shows the full summary and extracted article body
6. The "Open original" button opens the article URL in the device browser
7. Sharing a URL when no API key is configured navigates the user to Settings
8. Sharing an unreachable URL results in an article in the list with `FAILED` status and a readable error message
9. The app builds and installs on a physical Android device running Android 8.0+

---

## Outcome

*To be filled in retrospectively when Phase 1 is complete.*
