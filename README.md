# Task Planner

An offline-first task management app built with **Compose Multiplatform** (Kotlin), targeting **Android** and **Linux Desktop**. Tasks sync to a cloud backend via a Ktor server hosted on Render, backed by Neon Postgres.

## Features

- **Task management** -- Create, edit, delete tasks with title, description, due date/time
- **4-level priority system** -- Critical (red), High (orange), Medium (yellow), Low (blue)
- **Categories** -- User-created categories with custom colors. A default "General" category is auto-created on registration
- **"My Day" view** -- Virtual view that filters tasks where due date equals today, sorted by priority then time
- **Sectioned task lists** -- Tasks are split into Pending and Completed sections. Completed section is collapsible (collapsed by default)
- **Offline-first** -- All writes go to local SQLDelight DB first. A sync queue tracks pending operations and processes them when online
- **Background sync** -- Automatic sync every 60 seconds when logged in, with manual sync button
- **Conflict resolution** -- Last-write-wins based on `updated_at` timestamps
- **Theme system** -- System/Light/Dark mode toggle + 7 preset color themes (Blue, Purple, Green, Coral, Teal, Orange, Pink)
- **Settings screen** -- Theme configuration, update checker (Android), app version info
- **Auto-update** (Android) -- Checks GitHub Releases API on launch, notifies via snackbar when an update is available
- **Single-instance** (Desktop) -- File lock prevents multiple windows from opening simultaneously
- **Invite-code registration** -- New user registration requires a server-side invite code

## Architecture

```
┌─────────────────────────────────────────────┐
│                  Client                     │
│          (Compose Multiplatform)            │
│                                             │
│  ┌──────────┐  ┌────────────┐  ┌────────┐  │
│  │    UI     │→ │ Repository │→ │SQLDelight│ │
│  │ (Screens) │  │   Layer    │  │  (Local) │ │
│  └──────────┘  └─────┬──────┘  └────┬───┘  │
│                      │              │       │
│                ┌─────┴──────┐       │       │
│                │SyncManager │←──────┘       │
│                │ SyncQueue  │               │
│                └─────┬──────┘               │
└──────────────────────┼──────────────────────┘
                       │ HTTPS (Ktor Client)
                       ▼
┌──────────────────────────────────────────────┐
│              Ktor Server (Render)             │
│                                              │
│  Auth (JWT + bcrypt) ─── Routes ─── DAOs     │
│                                     │        │
│                              Exposed ORM     │
└──────────────────────┬───────────────────────┘
                       │ JDBC
                       ▼
┌──────────────────────────────────────────────┐
│           Neon Postgres (Free Tier)           │
└──────────────────────────────────────────────┘
```

### Tech Stack

| Layer | Technology |
|-------|-----------|
| UI framework | Compose Multiplatform 1.7.3 |
| Language | Kotlin 2.1.0 |
| Navigation | Voyager |
| Dependency injection | Koin Multiplatform |
| Local database | SQLDelight 2.0.2 |
| HTTP client | Ktor Client 3.0.3 |
| Server framework | Ktor Server 3.0.3 |
| Server ORM | Exposed 0.57.0 |
| Remote database | Neon Postgres |
| Server hosting | Render (free tier) |
| Auth | JWT (7-day expiry) + bcrypt password hashing |

### Modules

| Module | Purpose |
|--------|---------|
| `shared` | Serializable data models, request/response DTOs shared between client and server |
| `composeApp` | Compose Multiplatform client with `commonMain`, `androidMain`, and `desktopMain` source sets |
| `server` | Ktor server application with REST API, database access, and auth |

### Offline-First Sync

1. All create/update/delete operations write to the local SQLDelight database immediately
2. Each mutation is also recorded in a `SyncQueue` table (entity type, entity ID, operation, payload)
3. `SyncManager` processes the queue when the device is online, pushing changes to the server
4. Periodic background sync runs every 60 seconds
5. On sync pull, the server returns all entities updated since the client's last sync timestamp
6. Conflicts are resolved with **last-write-wins** using `updated_at` timestamps

## Prerequisites

- **JDK 17** (required for Gradle, Compose Desktop, and the server)
- **Android SDK** (for Android target; API 26+ minimum, API 35 target)
- **Gradle 8.10** (included via wrapper -- `./gradlew`)
- **`gh` CLI** (optional, for `install.sh` and release management)

## Development Setup

### 1. Clone the repository

```bash
git clone https://github.com/grayankit/task-planner.git
cd task-planner
```

### 2. Configure environment

Copy the example env file and fill in your values:

```bash
cp .env.example .env
```

Required variables for local server development:

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | Neon Postgres connection string |
| `JWT_SECRET` | Secret key for JWT signing |
| `JWT_ISSUER` | JWT issuer claim (default: `task-planner`) |
| `JWT_AUDIENCE` | JWT audience claim (default: `task-planner-users`) |
| `PORT` | Server port (default: `8080`) |
| `INVITE_CODE` | Required invite code for user registration |

### 3. Run the server locally

```bash
./gradlew :server:run
```

The server starts at `http://localhost:8080`. The production server is deployed at `https://task-planner-api-vhkm.onrender.com`.

The client's server URL is configured in `composeApp/src/commonMain/.../di/AppModule.kt` as `DEFAULT_BASE_URL`.

### 4. Run the desktop app

```bash
./gradlew :composeApp:run
```

### 5. Run on Android

Build a debug APK and install on a connected device or emulator:

```bash
./gradlew :composeApp:assembleDebug
```

Or run directly via Android Studio / IntelliJ with the Android run configuration.

> **Note:** `assembleRelease` requires signing environment variables (`KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) and the keystore file at `composeApp/keystore/release.keystore`. These are only available in CI.

## Release Workflow

Releases are automated via GitHub Actions. The workflow is triggered by pushing a `v*` tag.

### Steps to create a release

#### 1. Bump the version

Update the version in **two places**:

**`gradle.properties`** (single source of truth for Android + Desktop packaging):

```properties
app.version.name=1.3.0
app.version.code=4
```

**`composeApp/src/desktopMain/.../util/AppVersion.kt`** (manual sync required -- no BuildConfig on Desktop):

```kotlin
actual fun getAppVersion(): String = "1.3.0"
```

Android reads the version from `BuildConfig.VERSION_NAME`, which is derived from `gradle.properties` automatically.

#### 2. Commit and push

```bash
git add -A
git commit -m "Bump version to v1.3.0"
git push
```

#### 3. Tag and push

```bash
git tag -a v1.3.0 -m "v1.3.0: description of changes"
git push origin v1.3.0
```

#### 4. CI takes over

The GitHub Actions workflow (`.github/workflows/release.yml`) runs three jobs:

1. **build-android** -- Builds a signed release APK using the keystore from GitHub secrets
2. **build-linux** -- Builds a Compose Desktop distributable and packages it as a tar.gz (self-contained with bundled JVM)
3. **create-release** -- Creates a GitHub Release with both artifacts attached and auto-generated release notes

### Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | Base64-encoded release keystore file |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias in the keystore |
| `KEY_PASSWORD` | Key password |

Set these via `gh secret set` or the GitHub repository settings UI.

## Server Deployment

The server is deployed on **Render** (free tier, 512MB RAM) using Docker.

### Render setup

1. Create a new Web Service on Render, connected to the GitHub repo
2. Use the `render.yaml` blueprint, or configure manually:
   - **Runtime:** Docker
   - **Dockerfile path:** `./Dockerfile`
   - **Plan:** Free
3. Set environment variables in the Render dashboard:
   - `DATABASE_URL` -- Neon Postgres connection string
   - `INVITE_CODE` -- Registration invite code
   - `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE` are auto-configured via `render.yaml`

### Neon Postgres setup

1. Create a free Neon project at [neon.tech](https://neon.tech)
2. Copy the connection string from the dashboard
3. Set it as `DATABASE_URL` on Render

> **Note:** The JDBC driver cannot parse credentials embedded in the URI. The server's `DatabaseFactory` handles this by parsing the URI and passing username/password as separate HikariCP properties.

### Docker details

The `Dockerfile` uses a multi-stage build:
- **Build stage:** Gradle 8.10 + JDK 17, produces a fat JAR (~25MB)
- **Runtime stage:** Eclipse Temurin JRE 17 Alpine, JVM tuned with `-Xmx384m` for Render's 512MB limit

Render's free tier spins down after 15 minutes of inactivity. First request after spin-down takes ~30 seconds for cold start.

## Linux Desktop Installation

The `install.sh` script downloads the latest release from GitHub and installs it locally.

### Prerequisites

- `gh` CLI installed and authenticated (`gh auth login`)

### Install latest version

```bash
./install.sh
```

### Install a specific version

```bash
./install.sh v1.2.0
```

This will:
1. Download the Linux tar.gz from the GitHub Release
2. Extract to `~/.local/bin/TaskPlanner/`
3. Create a `.desktop` entry for your application launcher

The app can then be launched from your desktop environment's application menu or directly:

```bash
~/.local/bin/TaskPlanner/bin/TaskPlanner
```

## Project Structure

```
task-planner/
├── .github/workflows/
│   └── release.yml                 # CI: signed APK + Linux tar.gz → GitHub Release
├── composeApp/                     # Compose Multiplatform client
│   ├── build.gradle.kts            # Android + Desktop targets, signing config
│   ├── keystore/                   # Release keystore (gitignored)
│   └── src/
│       ├── commonMain/
│       │   ├── kotlin/.../
│       │   │   ├── App.kt                     # Root composable, update check
│       │   │   ├── di/AppModule.kt             # Koin module definition
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   ├── DriverFactory.kt        # expect: SQLDelight driver
│       │   │   │   │   └── PreferencesFactory.kt    # expect: key-value prefs
│       │   │   │   ├── remote/
│       │   │   │   │   ├── ApiService.kt            # Ktor HTTP client calls
│       │   │   │   │   └── HttpClientFactory.kt     # Ktor client config
│       │   │   │   ├── repository/
│       │   │   │   │   ├── AuthRepository.kt        # Login/register, token mgmt
│       │   │   │   │   ├── TaskRepository.kt        # Task CRUD + local queries
│       │   │   │   │   └── CategoryRepository.kt    # Category CRUD
│       │   │   │   ├── sync/
│       │   │   │   │   ├── SyncEnqueuer.kt          # Records mutations to queue
│       │   │   │   │   └── SyncManager.kt           # Processes queue, pulls updates
│       │   │   │   └── update/
│       │   │   │       └── UpdateChecker.kt         # GitHub Releases API check
│       │   │   ├── ui/
│       │   │   │   ├── components/
│       │   │   │   │   ├── TaskCard.kt              # Task list item card
│       │   │   │   │   ├── SectionedTaskList.kt     # Pending/Completed sections
│       │   │   │   │   ├── CategoryItem.kt          # Drawer category item
│       │   │   │   │   └── PrioritySelector.kt      # Priority picker chips
│       │   │   │   ├── screen/
│       │   │   │   │   ├── auth/AuthScreen.kt       # Login + register with invite code
│       │   │   │   │   ├── myday/MyDayScreen.kt     # Today's tasks + nav drawer
│       │   │   │   │   ├── tasklist/TaskListScreen.kt   # Category task list
│       │   │   │   │   ├── taskdetail/TaskDetailScreen.kt # Task create/edit form
│       │   │   │   │   ├── categories/CategoriesScreen.kt # Manage categories
│       │   │   │   │   └── settings/SettingsScreen.kt     # Theme, updates, about
│       │   │   │   └── theme/
│       │   │   │       ├── Color.kt                 # Priority + base colors
│       │   │   │       ├── ColorTheme.kt            # 7 preset color theme enum
│       │   │   │       ├── ThemeManager.kt          # Theme state + persistence
│       │   │   │       └── Theme.kt                 # MaterialTheme composable
│       │   │   └── util/
│       │   │       ├── AppVersion.kt                # expect: app version string
│       │   │       └── Platform.kt                  # expect: isAndroid()
│       │   └── sqldelight/.../db/
│       │       ├── Task.sq                          # Task table + queries
│       │       ├── Category.sq                      # Category table + queries
│       │       ├── AuthToken.sq                     # JWT token storage
│       │       ├── SyncQueue.sq                     # Pending sync operations
│       │       └── SyncMeta.sq                      # Last sync timestamp
│       ├── androidMain/
│       │   ├── AndroidManifest.xml
│       │   ├── res/mipmap-*/                        # App launcher icons
│       │   └── kotlin/.../
│       │       ├── MainActivity.kt                  # Android entry point
│       │       ├── TaskPlannerApp.kt                # Application class, Koin init
│       │       ├── data/local/
│       │       │   ├── DriverFactory.kt             # actual: Android SQLite driver
│       │       │   └── PreferencesFactory.kt        # actual: SharedPreferences
│       │       └── util/
│       │           ├── AppVersion.kt                # actual: BuildConfig.VERSION_NAME
│       │           └── Platform.kt                  # actual: isAndroid() = true
│       └── desktopMain/
│           ├── resources/icon.png                   # 512px desktop app icon
│           └── kotlin/.../
│               ├── Main.kt                          # Desktop entry, single-instance lock
│               ├── data/local/
│               │   ├── DriverFactory.kt             # actual: JDBC SQLite driver
│               │   └── PreferencesFactory.kt        # actual: java.util.prefs
│               └── util/
│                   ├── AppVersion.kt                # actual: hardcoded version string
│                   └── Platform.kt                  # actual: isAndroid() = false
├── server/                         # Ktor backend server
│   ├── build.gradle.kts            # Fat JAR config, Exposed + bcrypt deps
│   └── src/main/kotlin/.../server/
│       ├── Application.kt                       # Server entry point, plugin install
│       ├── data/
│       │   ├── DatabaseFactory.kt               # HikariCP + Neon URI parsing
│       │   ├── tables/                          # Exposed table definitions
│       │   │   ├── Tasks.kt
│       │   │   ├── Categories.kt
│       │   │   ├── Users.kt
│       │   │   └── DeletedEntities.kt
│       │   └── dao/                             # Data access objects
│       │       ├── TaskDao.kt
│       │       ├── CategoryDao.kt
│       │       ├── UserDao.kt
│       │       └── DeletedEntityDao.kt
│       ├── plugins/
│       │   ├── Auth.kt                          # JWT authentication config
│       │   ├── CORS.kt                          # CORS policy
│       │   ├── Routing.kt                       # Route registration
│       │   ├── Serialization.kt                 # JSON content negotiation
│       │   └── StatusPages.kt                   # Error handling
│       ├── routes/
│       │   ├── AuthRoutes.kt                    # Register (w/ invite code) + login
│       │   ├── TaskRoutes.kt                    # Task CRUD endpoints
│       │   ├── CategoryRoutes.kt                # Category CRUD endpoints
│       │   └── SyncRoutes.kt                    # Push/pull sync endpoints
│       └── security/
│           ├── JwtConfig.kt                     # JWT token generation/validation
│           ├── PasswordHasher.kt                # bcrypt hashing
│           └── AuthExtensions.kt                # Route auth helpers
├── shared/                         # Shared KMP module
│   ├── build.gradle.kts
│   └── src/commonMain/kotlin/.../shared/
│       ├── model/
│       │   ├── TaskDto.kt                       # Task data transfer object
│       │   ├── CategoryDto.kt                   # Category data transfer object
│       │   ├── UserDto.kt                       # User data transfer object
│       │   └── Priority.kt                      # Priority enum (1-4)
│       ├── request/
│       │   ├── AuthRequest.kt                   # Login/register request body
│       │   ├── CreateTaskRequest.kt
│       │   ├── UpdateTaskRequest.kt
│       │   ├── CreateCategoryRequest.kt
│       │   ├── UpdateCategoryRequest.kt
│       │   └── SyncPushRequest.kt               # Batch sync push payload
│       └── response/
│           ├── ApiResponse.kt                   # Generic API response wrapper
│           ├── AuthResponse.kt                  # JWT token response
│           └── SyncPullResponse.kt              # Sync pull payload
├── gradle/
│   ├── libs.versions.toml          # Version catalog
│   └── wrapper/                    # Gradle wrapper JAR + properties
├── build.gradle.kts                # Root build file
├── settings.gradle.kts             # Module includes + repositories
├── gradle.properties               # App version, JVM args, Android config
├── Dockerfile                      # Multi-stage server build for Render
├── render.yaml                     # Render deployment blueprint
├── install.sh                      # Linux desktop installer script
├── .env.example                    # Environment variable template
└── .gitignore
```
