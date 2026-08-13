# AGENTS.md - STDatabase

> For the repository overview, see the root `AGENTS.md`. See also `STCommon/AGENTS.md` for the shared interfaces
> that database models implement, and `STCore/AGENTS.md` for how controllers are surfaced through managers.

## Module Summary

`STDatabase` owns local persistence for SwissTransfer. It currently hosts **two** persistence stacks in parallel
during an ongoing migration:

- **Realm Kotlin** (legacy) – the historical storage for transfers / uploads / app settings.
- **Room (KMP) + KSP + SQLite bundled** – the new storage being introduced (see `dao/`, `Converters.kt`,
  `getAppDatabase` in `STCore`).

It exposes:

- `RealmProvider` / `DatabaseProvider` / `DatabaseConfig` – lifecycle of the underlying DB instances per user.
- `RealmMigrations` – schema migration definitions for Realm.
- `controllers/` – higher-level access patterns (`AppSettingsController`, `TransferController`,
  `UploadController`, `UploadTokensController`, `FileController`).
- `dao/` – Room DAOs (`AppSettingsDao`, `TransferDao`, `UploadDao`, `DownloadManagerRefDao`).
- `models/` – Realm + Room entities, organised by domain (`appSettings/`, `transfers/`, `upload/`) with a `v2/`
  subpackage under transfers/appSettings where applicable.

## Tech Stack & Dependencies

Declared in `STDatabase/build.gradle.kts`:

- **Realm Kotlin**: `realm.base`
- **Room (KMP)**: `androidx.room.runtime` (api – see TODO in the build file about downgrading to `implementation`),
  `androidx.sqlite.bundled`, `androidx.room.compiler` via KSP for `kspAndroid`, `kspIosSimulatorArm64`,
  `kspIosArm64`, `kspMacosArm64`.
- **Serialization**: `kotlinx.serialization.json` (for converters / persisted JSON blobs).
- **SKIE** for Swift bridging.
- **Desugaring**: `coreLibraryDesugaring(libs.desugar.jdk.libs)` is enabled — keep usage of `java.time.*` desugared
  APIs allowed.
- **androidMain**: `splitties.appctx` to get an `Application` context on Android.
- Test: `kotlin.test`, `kotlinx.coroutines.test`, plus `androidx.test` + Robolectric on Android JVM.

`room { schemaDirectory("$projectDir/schemas") }` — Room schema JSONs are committed to `STDatabase/schemas/`. Any
schema-changing edit MUST regenerate these.

## Layout

```
STDatabase/
├── src/
│   ├── commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/database/
│   │   ├── DatabaseConfig.kt
│   │   ├── DatabaseProvider.kt        # Aggregates Realm + Room handles
│   │   ├── RealmProvider.kt
│   │   ├── RealmMigrations.kt
│   │   ├── Converters.kt              # Room TypeConverters
│   │   ├── StringExtensions.kt
│   │   ├── controllers/               # Domain-level controllers (Realm + Room facades)
│   │   ├── dao/                       # Room @Dao interfaces
│   │   ├── models/
│   │   │   ├── appSettings/{,v2/}
│   │   │   ├── transfers/{,v2/}
│   │   │   └── upload/
│   │   └── utils/
│   ├── androidMain/        # Android-specific Room/Realm wiring
│   ├── androidUnitTest/    # Android JVM/Robolectric tests
│   ├── appleMain/          # iOS Room driver wiring
│   ├── commonTest/         # KMP unit tests
│   └── appleTest/          # iOS-specific tests (e.g. database/v2/utils)
└── schemas/                # Room exported schemas (commit them!)
```

## Local Norms

### Realm vs Room

- New tables / entities should be implemented in **Room** unless they extend an existing Realm graph for which
  migration is not yet planned. When in doubt, check whether the surrounding domain already has Room DAOs.
- Keep Realm and Room models in their existing locations (`models/.../v2/` is generally the Room/new world; legacy
  `RealmObject` types stay in the non-`v2/` packages alongside their controllers).
- `EmbeddedRealmObject` / `RealmObject` types must stay binary-compatible with previously persisted data — any
  schema change requires a migration entry in `RealmMigrations`.

### Controllers & DAOs

- Apps consume controllers, not raw Realm queries or Room DAOs. When adding behaviour, add it as a method on the
  appropriate `*Controller` (or create a new one) and inject the DAO/Realm dependency.
- Controllers must be safe to call from any dispatcher; suspending APIs are preferred over returning blocking
  Realm queries. Use `Flow` for observation.

### Migrations

- Bump the Room database version and write a `Migration` whenever you change a Room entity.
- For Realm, append a step to `RealmMigrations` rather than editing earlier steps – older clients still need the
  full chain.
- Regenerate and commit the JSON under `STDatabase/schemas/` for Room schema changes.

### Concurrency

- CI runs `testDebugUnitTest` and `iosSimulatorArm64Test` **separately** because of a known Realm concurrency issue
  with `allTests`. Don't reintroduce `allTests` in workflows or local scripts.

### Public API

- Anything exposed from controllers / DAOs is part of the iOS / Android library API. Prefer `internal` for helpers,
  and re-export only what `SwissTransferInjection` actually needs.

## Conventions

- Copyright header required (see root `AGENTS.md`).
- Kotlin official style; max one consecutive blank line.
- Package root: `com.infomaniak.multiplatform_swisstransfer.database`.
- Annotate Room entities/DAOs with the standard Room annotations and use `@TypeConverters(Converters::class)`.

## Commands

```bash
# Build
./gradlew :STDatabase:assemble

# Tests
./gradlew :STDatabase:testDebugUnitTest
./gradlew :STDatabase:iosSimulatorArm64Test

# Regenerate Room schemas (happens during compilation when entities change)
./gradlew :STDatabase:kspDebugKotlinAndroid

# iOS XCFramework
./gradlew :STDatabase:assembleSTDatabaseReleaseXCFramework
```

## JIT Index

```bash
# Realm entities
rg -n "RealmObject|EmbeddedRealmObject" STDatabase/src/commonMain

# Room entities and DAOs
rg -n "@Entity|@Dao|@Database|@TypeConverters" STDatabase/src/commonMain

# Controllers (domain APIs)
rg -n "class .*Controller" STDatabase/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/database/controllers

# Realm migrations
rg -n "AutomaticSchemaMigration|migrationContext|migration" STDatabase/src/commonMain
```

## Gotchas

- Don't add code touching Realm/Room to other modules — keep persistence here.
- Don't import an Android-only Realm / Room artifact in `commonMain`; use the KMP variants already declared in
  the version catalog.
- Renaming a Realm field on a stored model corrupts existing user data — write a migration instead.
- Tests that exercise both Realm and Room in the same JVM fork can deadlock; mirror the CI strategy by running the
  Android JVM and iOS simulator test tasks independently.
