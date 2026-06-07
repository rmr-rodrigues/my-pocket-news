# todo-01 — Phase 1: Core MVP

## Context

- **Project:** My Pocket News — Android app (Kotlin + Jetpack Compose)
- **Min SDK:** 26 (Android 8.0)
- **Build system:** Gradle with Kotlin DSL (`build.gradle.kts`)
- **Package:** `com.mypocketnews`
- **Key libraries:** Room 2.x, WorkManager 2.x, OkHttp 4.x, Jsoup 1.x, Readability4J, Jetpack Compose, Navigation Compose, Jetpack Security (EncryptedSharedPreferences)
- **Architecture:** Single-module Android app; manual constructor injection (no Hilt)
- **Commit convention:** `type(scope): message` — e.g. `feat(db): add Article entity and DAO`

## Conventions

- All Kotlin files use 4-space indentation
- Compose screen functions are named `*Screen` and accept a `ViewModel` parameter
- ViewModels expose a single `uiState: StateFlow<UiState>` sealed class
- Room entity class names match the domain term exactly: `Article`
- All database column names use `snake_case`; Kotlin field names use `camelCase`
- `Article.status` is stored as `TEXT` in Room using the `ArticleStatus` enum's `name`
- Worker input data key: `KEY_ARTICLE_ID = "article_id"`
- Notification channel IDs: `"processing"`, `"completion"`, `"error"`
- Dependencies are injected via constructor; Activities and Workers access the object graph via `(applicationContext as MyPocketNewsApp)` — no Hilt, no service locator pattern beyond the Application class
- ViewModels that require constructor parameters (e.g. `articleId`) must have a companion `Factory` implementing `ViewModelProvider.Factory`

---

## Group: Infrastructure

### [x] INFRA-1 — Android project scaffold and Gradle configuration

Creates the buildable Android project skeleton.

**Produces:**
- `settings.gradle.kts` with project name `"my-pocket-news"`
- `build.gradle.kts` (root) with Kotlin and Android Gradle Plugin versions
- `app/build.gradle.kts` with:
  - `applicationId = "com.mypocketnews"`
  - `minSdk = 26`, `targetSdk = 34`, `compileSdk = 34`
  - `versionCode = 1`, `versionName = "1.0.0"`
  - Dependencies: Compose BOM, Navigation Compose, Room KSP, WorkManager, OkHttp, Jsoup, Readability4J (`net.dankito.readability4j:readability4j`), Jetpack Security Crypto, Core KTX, Lifecycle ViewModel Compose
- `app/src/main/AndroidManifest.xml`:
  - `INTERNET` permission
  - `POST_NOTIFICATIONS` permission (required for Android 13+)
  - `Application` class declared: `com.mypocketnews.MyPocketNewsApp`
  - `MainActivity` declared as launcher with `android:theme="@style/Theme.MyPocketNews"`
  - `ShareActivity` declared (no launcher intent filter — only share) with `android:theme="@style/Theme.MyPocketNews"`
  - `ArticleProcessingWorker` declared (required for custom `WorkerFactory`)
- `gradle/libs.versions.toml` version catalogue
- `app/src/main/res/values/themes.xml`:
  ```xml
  <style name="Theme.MyPocketNews" parent="android:Theme.Material.Light.NoActionBar"/>
  ```
- `app/src/main/res/values/colors.xml` — placeholder with primary colour
- Icon assets are already present in `app/src/main/res/mipmap-*/` (copied from IconKitchen — do not regenerate)
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` is already present (adaptive icon referencing background/foreground/monochrome layers)
- `AndroidManifest.xml` `<application>` must declare `android:icon="@mipmap/ic_launcher"` and `android:roundIcon="@mipmap/ic_launcher"`

**Dependencies:** none

---

### [x] INFRA-2 — Room database, Article entity, DAO, and database class

Creates the local persistence layer.

**Produces:**
- `app/src/main/java/com/mypocketnews/data/db/ArticleStatus.kt`
  ```kotlin
  enum class ArticleStatus { PENDING, PROCESSING, DONE, FAILED }
  ```
- `app/src/main/java/com/mypocketnews/data/db/Article.kt` — Room `@Entity(tableName = "articles")`:

  | Kotlin field | Column | Type | Notes |
  |---|---|---|---|
  | `id` | `id` | `INTEGER PK AUTOINCREMENT` | `@PrimaryKey(autoGenerate = true)` |
  | `url` | `url` | `TEXT NOT NULL` | |
  | `title` | `title` | `TEXT NOT NULL DEFAULT ''` | |
  | `summary` | `summary` | `TEXT NOT NULL DEFAULT ''` | |
  | `bodyText` | `body_text` | `TEXT NOT NULL DEFAULT ''` | |
  | `providerUsed` | `provider_used` | `TEXT NOT NULL DEFAULT ''` | |
  | `modelUsed` | `model_used` | `TEXT NOT NULL DEFAULT ''` | |
  | `status` | `status` | `TEXT NOT NULL DEFAULT 'PENDING'` | stored as `ArticleStatus.name` |
  | `errorMessage` | `error_message` | `TEXT` | nullable |
  | `createdAt` | `created_at` | `INTEGER NOT NULL` | Unix ms |
  | `processedAt` | `processed_at` | `INTEGER` | nullable Unix ms |

- `app/src/main/java/com/mypocketnews/data/db/ArticleDao.kt`:
  ```kotlin
  @Dao interface ArticleDao {
      @Insert suspend fun insert(article: Article): Long
      @Update suspend fun update(article: Article)
      @Query("SELECT * FROM articles ORDER BY created_at DESC")
      fun getAll(): Flow<List<Article>>
      @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
      suspend fun getById(id: Long): Article?
  }
  ```
- `app/src/main/java/com/mypocketnews/data/db/AppDatabase.kt` — `@Database(entities = [Article::class], version = 1)`
- `TypeConverter` for `ArticleStatus` ↔ `String` declared inside `AppDatabase` via `@TypeConverters`

**Dependencies:** INFRA-1

---

### [x] INFRA-3 — Settings storage backed by EncryptedSharedPreferences

Creates secure storage for LLM provider configuration.

**Produces:**
- `app/src/main/java/com/mypocketnews/data/settings/LlmProviderConfig.kt`:
  ```kotlin
  data class LlmProviderConfig(
      val provider: String,   // "openai" | "openrouter"
      val apiKey: String,
      val model: String,
      val baseUrl: String
  )
  ```
- `app/src/main/java/com/mypocketnews/data/settings/SettingsRepository.kt`:
  - Constructor: `class SettingsRepository(context: Context)`
  - Creates `EncryptedSharedPreferences` internally using master key alias `"mpn_master_key"`
  - Keys: `pref_provider`, `pref_api_key`, `pref_model`, `pref_base_url`
  - `fun getConfig(): LlmProviderConfig?` — returns null if `pref_api_key` is blank
  - `fun saveConfig(config: LlmProviderConfig)`
  - `fun isConfigured(): Boolean`

**Dependencies:** INFRA-1

---

## Group: Background Processing

### [x] BG-1 — Article extraction pipeline (HTML fetch + Jsoup + Readability4J)

Creates the extraction logic used inside the worker.

**Produces:**
- `app/src/main/java/com/mypocketnews/data/extraction/ExtractionException.kt`:
  ```kotlin
  class ExtractionException(message: String, cause: Throwable? = null) : Exception(message, cause)
  ```
- `app/src/main/java/com/mypocketnews/data/extraction/ExtractedArticle.kt`:
  ```kotlin
  data class ExtractedArticle(val title: String, val bodyText: String)
  ```
- `app/src/main/java/com/mypocketnews/data/extraction/ArticleExtractor.kt`:
  - Constructor: `class ArticleExtractor(private val okHttpClient: OkHttpClient)`
  - `suspend fun extract(url: String): ExtractedArticle` — runs on `Dispatchers.IO`
  - OkHttp: `connectTimeout(10, SECONDS)`, `readTimeout(30, SECONDS)`, `User-Agent: Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36`
  - On non-2xx response or network error: throws `ExtractionException`
  - Passes raw HTML string and the original URL to `Readability(url, jsoupDocument).parse()`
  - `title`: use `article.title` if non-blank, else `jsoupDocument.title()`, else empty string
  - `bodyText`: strip HTML from `article.content` using `Jsoup.parse(content).text()`
  - If both `title` and `bodyText` are blank after extraction: throws `ExtractionException("Could not extract readable content from this page")`
  - Body text truncation: if `bodyText.length > 8000`, truncate at the last `'.'` or `'\n'` before index 8000; if no such boundary found, hard-truncate at 8000

**Dependencies:** INFRA-1

---

### [x] BG-2 — LlmClient interface and provider implementations

Creates the LLM abstraction layer.

**Produces:**
- `app/src/main/java/com/mypocketnews/data/llm/LlmException.kt`:
  ```kotlin
  open class LlmException(message: String) : Exception(message)
  class LlmAuthException(message: String) : LlmException(message)
  ```
- `app/src/main/java/com/mypocketnews/data/llm/LlmClient.kt`:
  ```kotlin
  interface LlmClient {
      suspend fun summarise(title: String, bodyText: String): String
  }
  ```
- `app/src/main/java/com/mypocketnews/data/llm/OpenAiLlmClient.kt`:
  - Constructor: `class OpenAiLlmClient(private val config: LlmProviderConfig, private val okHttpClient: OkHttpClient)`
  - Implements `LlmClient`
  - Endpoint: `config.baseUrl + "/chat/completions"` (baseUrl already includes `/v1`)
  - Headers: `Authorization: Bearer ${config.apiKey}`, `Content-Type: application/json`
  - Builds request body as a raw JSON string (no serialisation library)
  - Parses response with `org.json.JSONObject`: extracts `choices[0].message.content`
  - On 401/403: throws `LlmAuthException("Invalid API key — check Settings")`
  - On other non-2xx: throws `LlmException("LLM API error ${code}: ${body.take(200)}")`
  - Runs on `Dispatchers.IO`
- `app/src/main/java/com/mypocketnews/data/llm/OpenRouterLlmClient.kt`:
  - Constructor: `class OpenRouterLlmClient(private val config: LlmProviderConfig, private val okHttpClient: OkHttpClient)`
  - Identical to `OpenAiLlmClient` except adds header: `HTTP-Referer: android-app://com.mypocketnews`
- `app/src/main/java/com/mypocketnews/data/llm/LlmClientFactory.kt`:
  ```kotlin
  object LlmClientFactory {
      fun create(config: LlmProviderConfig, okHttpClient: OkHttpClient): LlmClient =
          when (config.provider) {
              "openrouter" -> OpenRouterLlmClient(config, okHttpClient)
              else         -> OpenAiLlmClient(config, okHttpClient)
          }
  }
  ```

**Request JSON shape:**
```json
{
  "model": "<model>",
  "messages": [
    {"role": "system", "content": "You are a news summariser. Summarise the following article in 3–5 sentences in the same language as the article. Be factual and concise."},
    {"role": "user", "content": "<title>\n\n<bodyText>"}
  ],
  "max_tokens": 512,
  "temperature": 0.3
}
```

**Dependencies:** INFRA-1, INFRA-3

---

### [x] BG-3 — Notification channels and notification helper

Creates the notification infrastructure.

**Produces:**
- `app/src/main/java/com/mypocketnews/notifications/NotificationChannels.kt`:
  - `fun createChannels(context: Context)` — registers three channels:
    - `"processing"` / "Article processing" / `IMPORTANCE_LOW`
    - `"completion"` / "Article ready" / `IMPORTANCE_DEFAULT`
    - `"error"` / "Processing error" / `IMPORTANCE_DEFAULT`
- `app/src/main/java/com/mypocketnews/notifications/AppNotifier.kt`:
  - `fun buildProcessingNotification(context: Context): Notification` — channel `"processing"`, title "My Pocket News", text "Processing article…", `setOngoing(true)`, small icon `android.R.drawable.ic_popup_sync` (placeholder — replaced when real icon drawable is added)
  - `fun postCompletion(context: Context, articleTitle: String, notificationId: Int)` — channel `"completion"`, title "Article saved", text `articleTitle`
  - `fun postError(context: Context, errorMessage: String, notificationId: Int)` — channel `"error"`, title "Could not save article", text `errorMessage.take(80)`
  - All methods guard with `ActivityCompat.checkSelfPermission` for `POST_NOTIFICATIONS` on API 33+

**Dependencies:** INFRA-1

---

### [x] BG-4 — Custom WorkerFactory and ArticleProcessingWorker

Creates the WorkManager worker and the factory required for manual dependency injection.

**Why a custom WorkerFactory is needed:** WorkManager instantiates workers via reflection by default, which does not support constructor parameters. With manual DI (no Hilt), a `WorkerFactory` subclass must be provided so the worker can receive `AppDatabase`, `SettingsRepository`, `OkHttpClient`, and `AppNotifier` as constructor arguments.

**Produces:**
- `app/src/main/java/com/mypocketnews/worker/ArticleProcessingWorker.kt` — `CoroutineWorker`:
  - Constructor: `(appContext: Context, params: WorkerParameters, private val db: AppDatabase, private val settingsRepository: SettingsRepository, private val okHttpClient: OkHttpClient)`
  - `doWork()` pipeline:
    1. Read `KEY_ARTICLE_ID` from `inputData.getLong(KEY_ARTICLE_ID, -1L)`; if `-1L`, return `Result.failure()`
    2. Look up article via `db.articleDao().getById(articleId)` — if null, return `Result.failure()`
    3. Update article status to `PROCESSING` via `db.articleDao().update(...)`
    4. Call `setForeground(ForegroundInfo(articleId.toInt(), AppNotifier.buildProcessingNotification(applicationContext)))`
    5. Call `ArticleExtractor(okHttpClient).extract(article.url)` — on `ExtractionException`: update to `FAILED`, post error notification, return `Result.failure()`
    6. Call `settingsRepository.getConfig()` — if null: update to `FAILED` with `"No LLM provider configured"`, post error notification, return `Result.failure()`
    7. Call `LlmClientFactory.create(config, okHttpClient).summarise(extracted.title, extracted.bodyText)` — on `LlmAuthException`: update to `FAILED` with `"Invalid API key — check Settings"`, post error notification, return `Result.failure()`; on other `LlmException`: update to `FAILED` with exception message, post error notification, return `Result.failure()`
    8. Update article: `status = DONE`, `title`, `summary`, `bodyText`, `providerUsed = config.provider`, `modelUsed = config.model`, `processedAt = System.currentTimeMillis()`
    9. Post completion notification via `AppNotifier.postCompletion(...)`
    10. Return `Result.success()`
  - Companion object: `const val KEY_ARTICLE_ID = "article_id"`

- `app/src/main/java/com/mypocketnews/worker/AppWorkerFactory.kt`:
  ```kotlin
  class AppWorkerFactory(
      private val db: AppDatabase,
      private val settingsRepository: SettingsRepository,
      private val okHttpClient: OkHttpClient
  ) : WorkerFactory() {
      override fun createWorker(
          appContext: Context,
          workerClassName: String,
          workerParameters: WorkerParameters
      ): ListenableWorker? {
          return if (workerClassName == ArticleProcessingWorker::class.java.name) {
              ArticleProcessingWorker(appContext, workerParameters, db, settingsRepository, okHttpClient)
          } else null
      }
  }
  ```

- `AndroidManifest.xml` must disable WorkManager's default initialisation and declare the custom factory:
  ```xml
  <provider
      android:name="androidx.startup.InitializationProvider"
      android:authorities="${applicationId}.androidx-startup"
      android:exported="false"
      tools:node="merge">
      <meta-data
          android:name="androidx.work.WorkManagerInitializer"
          android:value="androidx.startup"
          tools:node="remove"/>
  </provider>
  ```
  WorkManager is then initialised manually in `MyPocketNewsApp.onCreate()` via:
  ```kotlin
  WorkManager.initialize(
      this,
      Configuration.Builder().setWorkerFactory(AppWorkerFactory(database, settingsRepository, okHttpClient)).build()
  )
  ```

**Dependencies:** INFRA-2, INFRA-3, BG-1, BG-2, BG-3

---

## Group: Application Wiring

### [ ] APP-1 — Application class and dependency graph

Creates the app-level object graph and wires all singletons.

**Produces:**
- `app/src/main/java/com/mypocketnews/MyPocketNewsApp.kt` — `Application` subclass:
  ```kotlin
  class MyPocketNewsApp : Application() {
      val okHttpClient: OkHttpClient by lazy { OkHttpClient.Builder().build() }
      val database: AppDatabase by lazy { Room.databaseBuilder(this, AppDatabase::class.java, "mpn.db").build() }
      val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

      override fun onCreate() {
          super.onCreate()
          NotificationChannels.createChannels(this)
          WorkManager.initialize(
              this,
              Configuration.Builder()
                  .setWorkerFactory(AppWorkerFactory(database, settingsRepository, okHttpClient))
                  .build()
          )
      }
  }
  ```
- All Activities and Workers access dependencies via `(applicationContext as MyPocketNewsApp).database` etc.

**Dependencies:** INFRA-2, INFRA-3, BG-3, BG-4

---

## Group: UI

### [ ] FE-1 — Settings screen

Creates the LLM provider configuration UI.

**Produces:**
- `app/src/main/java/com/mypocketnews/ui/settings/SettingsViewModel.kt`:
  - Constructor: `class SettingsViewModel(private val settingsRepository: SettingsRepository)`
  - `uiState: StateFlow<SettingsUiState>` — data class with fields: `provider`, `apiKey`, `model`, `saved: Boolean`
  - Initialised from `settingsRepository.getConfig()` on creation
  - `fun save(provider: String, apiKey: String, model: String)` — builds `LlmProviderConfig` (sets `baseUrl` based on provider), calls `settingsRepository.saveConfig()`, sets `saved = true`
  - Default base URLs: `"openai"` → `"https://api.openai.com/v1"`, `"openrouter"` → `"https://openrouter.ai/api/v1"`
  - Default models: `"openai"` → `"gpt-4o-mini"`, `"openrouter"` → `"openai/gpt-4o-mini"`
  - Companion `Factory`:
    ```kotlin
    companion object {
        fun factory(settingsRepository: SettingsRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(settingsRepository) as T
        }
    }
    ```
- `app/src/main/java/com/mypocketnews/ui/settings/SettingsScreen.kt`:
  - Receives `viewModel` instantiated via `viewModel(factory = SettingsViewModel.factory(...))`
  - Provider dropdown (two options: "OpenAI", "OpenRouter") — implemented as `ExposedDropdownMenuBox`
  - API Key `OutlinedTextField` with `visualTransformation = PasswordVisualTransformation()`
  - Model `OutlinedTextField` — pre-filled with default per selected provider; updates when provider changes
  - "Save" `Button` — calls `viewModel.save()`
  - `LaunchedEffect(uiState.saved)` — shows `Snackbar("Settings saved")` when `saved` becomes true, then resets flag

**Dependencies:** APP-1

---

### [ ] FE-2 — Article list screen

Creates the main screen showing all saved articles.

**Produces:**
- `app/src/main/java/com/mypocketnews/ui/list/ArticleListViewModel.kt`:
  - Constructor: `class ArticleListViewModel(private val db: AppDatabase)`
  - `val articles: StateFlow<List<Article>>` — collected from `db.articleDao().getAll()` in `viewModelScope`
  - Companion `Factory`:
    ```kotlin
    companion object {
        fun factory(db: AppDatabase) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ArticleListViewModel(db) as T
        }
    }
    ```
- `app/src/main/java/com/mypocketnews/ui/list/ArticleListScreen.kt`:
  - Receives `viewModel` instantiated via `viewModel(factory = ArticleListViewModel.factory(...))`
  - `LazyColumn` of `ArticleCard` composables
  - Each card shows: title (fallback to URL hostname via `Uri.parse(url).host ?: url`), summary preview (first 120 chars of `summary`, or `"Processing…"` if status is `PENDING`/`PROCESSING`, or `"Failed"` if `FAILED`), relative time (`DateUtils.getRelativeTimeSpanString`), status indicator
  - Status indicator: `CircularProgressIndicator` (size 16.dp) for `PENDING`/`PROCESSING`; red `Text("Failed", color = MaterialTheme.colorScheme.error)` for `FAILED`; nothing for `DONE`
  - Empty state: centred `Text("No saved articles yet.\nShare a URL to get started.")`
  - Tapping a card calls `onArticleClick(article.id)`
  - `TopAppBar` with title "My Pocket News" and a settings icon button calling `onSettingsClick()`

**Dependencies:** APP-1

---

### [ ] FE-3 — Article detail screen

Creates the screen showing full article content.

**Produces:**
- `app/src/main/java/com/mypocketnews/ui/detail/ArticleDetailViewModel.kt`:
  - Constructor: `class ArticleDetailViewModel(private val db: AppDatabase, private val articleId: Long)`
  - `val uiState: StateFlow<ArticleDetailUiState>` — sealed class: `Loading`, `Success(article: Article)`, `NotFound`
  - Collects from `db.articleDao().getById(articleId)` — note: `getById` is `suspend`, not `Flow`; use `viewModelScope.launch` and re-query on demand, or add a `Flow`-returning query `getByIdFlow(id): Flow<Article?>` to `ArticleDao`
  - **Add to `ArticleDao`:** `@Query("SELECT * FROM articles WHERE id = :id LIMIT 1") fun getByIdFlow(id: Long): Flow<Article?>`
  - Companion `Factory`:
    ```kotlin
    companion object {
        fun factory(db: AppDatabase, articleId: Long) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ArticleDetailViewModel(db, articleId) as T
        }
    }
    ```
- `app/src/main/java/com/mypocketnews/ui/detail/ArticleDetailScreen.kt`:
  - Receives `articleId: Long` and `viewModel` instantiated via `viewModel(factory = ArticleDetailViewModel.factory(db, articleId))`
  - `Loading` state: centred `CircularProgressIndicator`
  - `NotFound` state: centred `Text("Article not found")`
  - `Success` state:
    - `TopAppBar` with back arrow calling `onBack()`; title = article title (truncated to one line)
    - Scrollable `Column`:
      - Title (`MaterialTheme.typography.headlineSmall`)
      - If `FAILED`: `Text(article.errorMessage ?: "Unknown error", color = error)`
      - If `DONE`: "Summary" section header + `Text(article.summary)`; "Full article" section header + `Text(article.bodyText)`
      - "Open original" `OutlinedButton` — fires `Intent(ACTION_VIEW, Uri.parse(article.url))`

**Dependencies:** APP-1, INFRA-2

---

### [ ] FE-4 — ShareActivity, navigation host, and app entry points

Creates the navigation graph and the share entry point.

**Produces:**
- `app/src/main/java/com/mypocketnews/ui/MainActivity.kt`:
  - Extends `ComponentActivity`
  - `setContent { AppNavHost(app) }` where `app = application as MyPocketNewsApp`
- `app/src/main/java/com/mypocketnews/ui/AppNavHost.kt`:
  - Receives `app: MyPocketNewsApp`
  - Routes: `"list"`, `"detail/{articleId}"`, `"settings"`
  - Start destination: `"list"`
  - `"list"` → `ArticleListScreen(viewModel = viewModel(factory = ArticleListViewModel.factory(app.database)), onArticleClick = { navController.navigate("detail/$it") }, onSettingsClick = { navController.navigate("settings") })`
  - `"detail/{articleId}"` → extracts `articleId` from `backStackEntry.arguments`, instantiates `ArticleDetailScreen`
  - `"settings"` → `SettingsScreen(viewModel = viewModel(factory = SettingsViewModel.factory(app.settingsRepository)))`
- `app/src/main/java/com/mypocketnews/ShareActivity.kt`:
  - Extends `ComponentActivity`
  - `onCreate()` runs in `lifecycleScope.launch`:
    1. Extract URL from `intent.getStringExtra(Intent.EXTRA_TEXT)`
    2. Validate with `Patterns.WEB_URL.matcher(url).matches()` — if invalid: `Toast("Not a valid URL")`, finish
    3. Check `app.settingsRepository.isConfigured()` — if false: start `MainActivity` with extra `"start_destination" = "settings"`, finish
    4. Insert `Article(url = url, status = PENDING, createdAt = System.currentTimeMillis())` via `app.database.articleDao().insert(article)` — this is a `suspend` call, safe inside `lifecycleScope.launch`
    5. Enqueue `OneTimeWorkRequestBuilder<ArticleProcessingWorker>().setInputData(workDataOf(KEY_ARTICLE_ID to articleId)).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).build()` via `WorkManager.getInstance(this).enqueue(...)`
    6. `Toast("Saving article…")`, finish
- `AndroidManifest.xml` updates:
  - `ShareActivity` intent filter: `action=ACTION_SEND`, `category=DEFAULT`, `mimeType=text/plain`
  - `MainActivity` handles the `"start_destination"` extra to navigate directly to settings if present

**Dependencies:** APP-1, FE-1, FE-2, FE-3, BG-4

---

## Phase 1 Verification Checklist

- [ ] Project builds without errors: `./gradlew :app:assembleDebug`
- [ ] App installs on a physical or emulated Android 8.0+ device
- [ ] App icon appears correctly in the launcher (adaptive icon on API 26+)
- [ ] Sharing a BBC/Guardian/Reuters article URL from Chrome shows My Pocket News in the share sheet
- [ ] After sharing, a silent "Processing article…" notification appears
- [ ] After processing, notification shows "Article saved: [title]"
- [ ] App list screen shows the article with summary preview
- [ ] Detail screen shows full summary and extracted body text
- [ ] "Open original" button opens the URL in the browser
- [ ] Sharing without a configured API key navigates to Settings
- [ ] Saving valid settings shows "Settings saved" snackbar
- [ ] Sharing an unreachable URL produces a FAILED article with error message in the list

---

## Open Items

- Maximum body text length (8000 chars) may need tuning based on real-world LLM cost and quality testing
- WorkManager retry policy is not configured in v1 — consider adding `setBackoffCriteria` in Phase 2
- `POST_NOTIFICATIONS` runtime permission request dialog is not implemented in v1 (user must enable manually on Android 13+); add permission request flow in Phase 2
- `MainActivity` handling of `"start_destination"` extra (navigate to settings after share with no config) should be verified — alternative is to use a dedicated `Intent` flag instead of an extra
