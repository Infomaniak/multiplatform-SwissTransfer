# AGENTS.md - multiplatform-SwissTransfer (Top Level)

> Navigation Guide: This file describes the repository as a whole. For module-specific norms see
> `STCommon/AGENTS.md`, `STNetwork/AGENTS.md`, `STDatabase/AGENTS.md`, and `STCore/AGENTS.md`.

## Project Snapshot

Kotlin Multiplatform (KMP) core library powering the SwissTransfer apps for Android and iOS (Infomaniak Network SA).
It provides the shared business logic – networking, persistence, upload/download orchestration, account & app
settings management – through a single facade class `SwissTransferInjection`.

- **Languages**: Kotlin (KMP — `commonMain`, `androidMain`, `appleMain` / `iosMain`)
- **Targets**: `android`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`
- **JDK**: 17 (`Versions.javaVersion`); Android `minSdk` 24, `compileSdk` 35
- **Build system**: Gradle (Kotlin DSL) with a composite `buildTools` build providing custom convention plugins
- **Networking**: Ktor (OkHttp on Android, Darwin on iOS) + kotlinx.serialization
- **Persistence**: Realm (legacy) and Room with KSP (new). Both live in `STDatabase`.
- **iOS interop**: SKIE (Touchlab) – see `buildTools/.../SkieExt.kt`; XCFrameworks produced via Kotlin Multiplatform
- **Distribution**:
    - Android → Maven Central / Sonatype snapshots and JitPack (`com.github.infomaniak.multiplatform-SwissTransfer:<module>:<tag>`)
    - iOS → XCFrameworks attached to GitHub Releases and consumed through `Package.swift` (SPM binary targets)

## Repository Layout

```
multiplatform-SwissTransfer/
├── buildTools/             # Included composite build – Gradle convention plugins
│   └── gradle/src/main/kotlin/com/infomaniak/gradle/
│       ├── extensions/     # configureAndroid / configureKotlinMultiplatform / configureSkie
│       ├── plugins/        # KotlinMultiplatformPlugin, PublishPlugin
│       └── utils/Versions.kt   # mavenVersionName, minSdk, compileSdk, javaVersion
├── STCommon/               # Shared interfaces, models, exceptions, utils (see STCommon/AGENTS.md)
├── STNetwork/              # Ktor HTTP client, repositories, network exceptions (see STNetwork/AGENTS.md)
├── STDatabase/             # Realm + Room persistence, controllers, DAOs (see STDatabase/AGENTS.md)
├── STCore/                 # SwissTransferInjection facade + managers (see STCore/AGENTS.md)
├── gradle/libs.versions.toml   # Project version catalog
├── settings.gradle.kts     # Includes :STCommon, :STNetwork, :STDatabase, :STCore
├── build.gradle.kts        # Top-level – aggregates publishing via nmcpAggregation
├── Package.swift           # Swift Package Manager – binary XCFramework targets
├── buildRelease            # Bash script that bumps versions & builds XCFrameworks
└── jitpack.yml
```

### Module dependency graph

```
STCore ──api──▶ STCommon
   │  ──implementation──▶ STNetwork ──implementation──▶ STCommon
   │  ──implementation──▶ STDatabase ──implementation──▶ STCommon
```

`STCommon` is the only module exported via XCFramework as an `api` dependency of `STCore`
(`STCore/build.gradle.kts` → `appleExportedProjects = listOf(commonProject)`).

## Quick Commands

```bash
# Clean
./gradlew clean

# Build everything (all targets)
./gradlew assemble

# Run unit tests (CI splits these because of a Realm concurrency issue – do NOT use allTests)
./gradlew testDebugUnitTest --stacktrace
./gradlew iosSimulatorArm64Test --stacktrace

# Build a single module
./gradlew :STNetwork:build
./gradlew :STCore:assemble

# Publish locally (snapshot to ~/.m2)
./gradlew publishToMavenLocal

# Publish to Sonatype snapshots (CI workflow: publish-to-central-snapshots.yml)
./gradlew publishAggregationToCentralSnapshots

# Publish a release to Maven Central (CI workflow: publish-to-maven-central.yml – version must NOT be a SNAPSHOT)
./gradlew clean publishAggregationToCentralPortal

# Build the iOS XCFrameworks (or use ./buildRelease)
./gradlew :STNetwork:assembleSTNetworkReleaseXCFramework
./gradlew :STDatabase:assembleSTDatabaseReleaseXCFramework
./gradlew :STCore:assembleSTCoreReleaseXCFramework

# Bump version + build XCFrameworks + update Package.swift in one shot
./buildRelease 1.2.3              # both platforms
./buildRelease 1.2.3 --android    # Android publish version only
./buildRelease 1.2.3 --ios        # XCFrameworks + Package.swift checksum/url update
```

## Build Plugins

Modules apply `id("infomaniak.kotlinMultiplatform")` and `id("infomaniak.publishPlugin")` from `buildTools`.
These plugins (see `buildTools/gradle/src/main/kotlin/com/infomaniak/gradle/plugins/`) centralise:

- KMP target setup (`androidTarget`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`)
- Android library config (`minSdk`, `compileSdk`, Java 17)
- SKIE configuration (only applied when the `skie` plugin is declared on the module)
- XCFramework declaration with `baseName = project.name` and `binaryOption("bundleId", "com.infomaniak.multiplatform_swisstransfer.<module>")`
- Publishing metadata, GPG signing, Maven Central / snapshot targets

The exported set of projects per XCFramework is configured through the `kotlinMultiplatformConfig` extension
(see `STMultiplatformExtension`) – e.g. `STCore` exports `STCommon`.

## Universal Conventions

### Code style

- Kotlin official code style (`kotlin.code.style=official` in `gradle.properties`).
- Copyright header required on every Kotlin / build script file. Format (with end year updated when the file is edited):

  ```kotlin
  /*
   * Infomaniak SwissTransfer - Multiplatform
   * Copyright (C) <year>[-<year>] Infomaniak Network SA
   *
   * ... GPLv3 boilerplate ...
   */
  ```

- No blank line between the copyright block and the `package` declaration.
- Never use more than 1 consecutive blank line.
- Keep one-liners for trivial `if/return` / `if/else` expressions; use braces + newlines for non-trivial blocks.

### KMP source set rules

- Put platform-agnostic code in `commonMain`. Only drop down to `androidMain` / `appleMain` / `iosMain` when you need
  a platform API.
- For `expect`/`actual`, name files `Xxx.android.kt`, `Xxx.apple.kt`, `Xxx.ios.kt` consistently with existing examples
  (e.g. `STCore/.../utils/EmailLanguageUtils.android.kt` & `EmailLanguageUtils.apple.kt`).
- Tests go in `commonTest` whenever possible; platform-specific tests live in `androidUnitTest` / `iosTest` / `appleTest`.
- Don't add `iosMain` targets manually unless required — the convention plugin already wires Apple targets.

### Public API

- Public symbols ARE the API surface for Android consumers (Maven) AND for iOS (SKIE-generated Swift).
  Keep public surface intentional and minimal; prefer `internal` for module-private helpers.
- `STCommon` is the *only* module re-exported in the `STCore` XCFramework, so types crossing the boundary to iOS
  consumers should ideally live there (or be exported explicitly through `appleExportedProjects`).
- Breaking the public API requires a corresponding bump of `Versions.mavenVersionName` and SPM `Package.swift`.

### Error handling

- Network errors are mapped through repository helpers (e.g. `UploadV2Repository.withUploadErrorHandling`) into the
  typed exceptions in `STNetwork/.../network/exceptions/` and `STCommon/.../common/exceptions/`.
  Do not throw raw `Throwable`/`Exception` from a public API — map to one of these typed exceptions.

### Commit & PR title convention

CI enforces the Infomaniak semantic-commit format (`.github/workflows/semantic-commit.yml` ⇒
`infomaniak/.github/.github/workflows/semantic-commit.yml@v2`). Every commit message **and** PR title must match:

```
^Merge .+|(^(feat|fix|chore|docs|style|refactor|perf|ci|test)(\(.+\))?: [A-Z0-9].+)
```

Examples: `feat: Add v2 upload session resume`, `fix(network): Map too_many_request`, `chore: Bump kotlinx-coroutines`.

### Releasing

- Maven version: edit `buildTools/gradle/src/main/kotlin/com/infomaniak/gradle/utils/Versions.kt`
  (`mavenVersionName`). `release_android` in `./buildRelease` does this for you.
- SNAPSHOT versions go to Sonatype Snapshots; non-SNAPSHOT goes to Maven Central via the
  `publish-to-maven-central.yml` workflow.
- iOS release flow: `./buildRelease <version> --ios` builds XCFrameworks under `release/`, computes
  `swift package compute-checksum` and rewrites the URLs + checksums in `Package.swift`. Upload the produced
  archives to the matching GitHub Release tag.

### Things you must NOT do

- Don't commit secrets (Sonatype credentials, GPG keys, signing material).
- Don't add Android or iOS-only code to `commonMain`.
- Don't switch the test command in CI to `allTests` (there is a Realm concurrency issue — keep
  `testDebugUnitTest` + `iosSimulatorArm64Test` separate).
- Don't introduce a new dependency without registering it in `gradle/libs.versions.toml`.
- Don't bypass `SwissTransferInjection` from consumer apps — its managers ARE the public API.

## JIT Index

```bash
# All convention plugins / shared Gradle helpers
find buildTools -name "*.kt"

# Find public managers / facade
rg -n "class .*Manager|class SwissTransferInjection" STCore/src/commonMain

# Realm models, Room entities, DAOs and controllers
rg -n "RealmObject|@Entity|@Dao|Controller" STDatabase/src/commonMain

# Repositories (network) and Ktor request builders
rg -n "Repository|HttpClient|client\\..*<" STNetwork/src/commonMain

# Exposed exceptions
rg -n "class .*Exception" STCommon STNetwork

# expect/actual declarations
rg -n "^expect |^actual " STCommon STDatabase STNetwork STCore
```

## Self-correction

- **Stale map**: Update the module / folder tree above when adding new top-level modules or significantly reshaping a
  module's package layout.
- **New norms**: Record cross-module conventions here; module-specific norms belong to that module's `AGENTS.md`.
- **CI behaviour changes**: If the CI workflow names or commands in `.github/workflows/` are renamed, update the
  "Quick Commands" and "Releasing" sections.
