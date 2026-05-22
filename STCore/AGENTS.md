# AGENTS.md - STCore

> For the repository overview, see the root `AGENTS.md`. See also `STCommon/AGENTS.md`, `STNetwork/AGENTS.md`, and
> `STDatabase/AGENTS.md` for the modules wired together here.

## Module Summary

`STCore` is the public entry point of the multiplatform-SwissTransfer library. It glues `STCommon`, `STNetwork`,
and `STDatabase` together behind a single facade class, `SwissTransferInjection`, which lazily builds and exposes
all the managers an app needs:

- `appSettingsManager` — `AppSettingsManager`
- `transferManager` — `TransferManager`
- `fileManager` — `FileManager`
- `accountManager` — `AccountManager`
- `uploadManager` / `inMemoryUploadManager` / `UploadV2Manager` / `UploadTokensManager`
- `emailTokensManager`
- `sharedApiUrlCreator` — `SharedApiUrlCreator` (shared deep-link / API URL helpers)

`SwissTransferInjection` is the only entry point consumer apps (`Infomaniak/android-SwissTransfer`,
the iOS app via SPM) should construct.

## Tech Stack & Dependencies

Declared in `STCore/build.gradle.kts`:

- `api(project(":STCommon"))` — re-exported so iOS consumers see the shared interfaces directly.
- `api(libs.ktor.client.core)` — Ktor surface needed by `ApiClientProvider`.
- `implementation(project(":STDatabase"))` and `implementation(project(":STNetwork"))` — internal wiring only.
- SKIE applied for Swift-friendly bridging.
- Configured with `appleExportedProjects = listOf(commonProject)` via the `kotlinMultiplatformConfig` extension,
  which means **only `STCommon` is re-exported in the `STCore` XCFramework**. Public types from `STNetwork` and
  `STDatabase` that need to be visible to Swift consumers must either be moved to `STCommon` or be added to
  `appleExportedProjects` explicitly.

## Layout

```
STCore/src/
├── commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/
│   ├── SwissTransferInjection.kt   # Facade – lazily wires controllers, repos and managers
│   ├── SharedApiUrlCreator.kt      # API URL helpers used by sharing flows
│   ├── managers/                   # AppSettings, Account, File, Transfer, Upload (+ V2), InMemory, UploadTokens
│   ├── mappers/                    # Network DTO ↔ database / domain mappers
│   ├── data/                       # STUser and other facade-level data classes
│   ├── exceptions/                 # Core-level exceptions
│   └── utils/EmailLanguageUtils.kt # expect declaration
├── androidMain/.../utils/EmailLanguageUtils.android.kt   # actual for Android (java.util.Locale)
├── appleMain/.../utils/EmailLanguageUtils.apple.kt       # actual for iOS/macOS (NSLocale)
└── commonTest/.../utils/           # Common unit tests
```

## Local Norms

### Facade pattern

- `SwissTransferInjection` is the only public constructor that consumers should call. It owns the lifecycle of
  the underlying `RealmProvider`, `DatabaseProvider`, `ApiClientProvider` and all managers. Do not encourage
  apps to instantiate managers, controllers, or repositories directly.
- Properties on `SwissTransferInjection` are lazy by design — keep new dependencies lazy too, so the cost of
  constructing the facade stays close to zero.
- `loadUser(userId)` is the user-switching entry point. Any new per-user state must hook into that flow.

### Managers

- A manager wraps **one domain** (transfers, files, accounts…) and exposes coroutine-friendly APIs that the apps
  call. It must:
    - Depend only on controllers (from `STDatabase`) and repositories (from `STNetwork`) — never on Ktor `HttpClient`
      or Realm/Room types directly.
    - Map network exceptions to the domain exceptions already declared in `STCommon`/`STNetwork`; do not leak raw
      Ktor `ResponseException`s to consumers.
    - Be safe to call from any dispatcher. Prefer `withContext(Dispatchers.IO)` (or equivalent) inside the manager
      rather than imposing dispatcher requirements on callers.

### Mappers

- All conversion between network DTOs, database entities, and domain types lives in `mappers/`. Keep mapping logic
  out of repositories/controllers so the boundary remains obvious.

### Platform code

- `STCore` is mostly `commonMain`. Only drop into `androidMain` / `appleMain` for `expect`/`actual` pairs (current
  example: `EmailLanguageUtils`).
- Use the existing `.android.kt` / `.apple.kt` suffix convention for `actual` files.

### Public API

- Everything `public` here ends up in the SPM XCFramework + Maven artifact. Breaking changes must bump
  `Versions.mavenVersionName` and trigger a Package.swift update via `./buildRelease`.
- `STCommon` types appear naturally to Swift because of `appleExportedProjects`. If you need to expose a type from
  `STNetwork`/`STDatabase` to Swift, prefer relocating it to `STCommon`.

## Conventions

- Copyright header required (see root `AGENTS.md`).
- Kotlin official style; max one consecutive blank line.
- Package root: `com.infomaniak.multiplatform_swisstransfer`.
- Tests in `commonTest`; reach for `androidUnitTest`/`appleTest` only when a platform fact is under test.

## Commands

```bash
# Build everything STCore depends on
./gradlew :STCore:assemble

# Tests
./gradlew :STCore:testDebugUnitTest
./gradlew :STCore:iosSimulatorArm64Test

# iOS XCFramework (also rebuilds dependent modules)
./gradlew :STCore:assembleSTCoreReleaseXCFramework

# Publish locally to validate facade wiring
./gradlew :STCore:publishToMavenLocal
```

## JIT Index

```bash
# Facade + managers
rg -n "class .*Manager|class SwissTransferInjection" STCore/src/commonMain

# expect/actual declarations
rg -n "^expect |^actual " STCore/src

# Mappers
rg -n "fun .*toRealm|fun .*toEntity|fun .*toDomain|fun .*toApi" STCore/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/mappers

# Exceptions
rg -n "class .*Exception" STCore/src/commonMain
```

## Gotchas

- Do **not** add a direct dependency from `STCore` on Android `Context` or any Apple framework. Platform context
  is obtained through `STDatabase` (`splitties.appctx` on Android) and through Ktor engines for networking.
- Forgetting to re-export a new shared interface (i.e. adding it to `STNetwork`/`STDatabase` instead of `STCommon`)
  will make it invisible to Swift consumers. Move public-facing contracts to `STCommon`.
- The `userAgent` parameter on `SwissTransferInjection` is part of the public constructor signature — renaming or
  removing it is a breaking change for all consumers.
- Don't bypass `loadUser(userId)` to swap accounts; the realm / database providers depend on it for per-user
  isolation.
