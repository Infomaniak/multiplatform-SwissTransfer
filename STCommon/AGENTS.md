# AGENTS.md - STCommon

> For the repository overview, see the root `AGENTS.md`. For sibling module norms, see `STNetwork/AGENTS.md`,
> `STDatabase/AGENTS.md`, and `STCore/AGENTS.md`.

## Module Summary

`STCommon` is the shared foundation of the multiplatform SwissTransfer library, with no dependencies on other ST
modules. It contains the interfaces, data models, utilities, exceptions and Matomo analytics constants used by all
other modules. It is the **only** module re-exported through the iOS XCFramework of `STCore`
(`appleExportedProjects = listOf(commonProject)` in `STCore/build.gradle.kts`), so its public surface is what iOS
Swift code actually sees.

## Tech Stack & Dependencies

Declared in `STCommon/build.gradle.kts`:

- `kotlinx-coroutines-core` (api)
- `kotlinx-datetime` (api)
- `kotlinx-serialization-json` (api)
- `kotlin.test` (test)

No dependencies on other ST modules — `STCommon` is at the bottom of the dependency graph.

## Layout

```
STCommon/src/
├── commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/common/
│   ├── interfaces/        # Pure abstractions consumed by Network/Database/Core
│   │   ├── appSettings/   # AppSettings contracts
│   │   ├── transfers/     # Transfer & file contracts (incl. v2/)
│   │   ├── upload/        # Upload session contracts
│   │   └── ui/            # UI-level contracts (kept here to share between platforms)
│   ├── models/            # Plain data classes / enums
│   ├── exceptions/        # Domain-level exception hierarchy
│   ├── matomo/            # Matomo analytics constants (events, categories…)
│   └── utils/             # ApiEnvironment, DateUtils, ListExt, ...
├── androidMain/kotlin/.../common/ext/    # JVM/Android-only extensions
└── commonTest/kotlin/.../common/         # JUnit/kotlin.test unit tests
```

## Local Norms

- **No external SDKs** (Realm, Room, Ktor, Android SDK, Foundation, …) may be referenced from `commonMain`.
  If your code needs them, it belongs in `STDatabase`, `STNetwork`, or in a platform source set (`androidMain` / `appleMain`).
- **Interfaces over implementations**: Put a contract here when more than one module needs to depend on it, and
  implement it in the appropriate module. Examples already in the tree: `CrashReportInterface`,
  `interfaces/upload/*`, `interfaces/transfers/v2/*`.
- **Public surface = iOS surface**: Any new public class/function in `STCommon` will be exposed to Swift through
  SKIE. Keep the API small, idiomatic, and Swift-friendly:
    - Avoid `vararg`, default arguments on suspending functions, and `inline` reified APIs in the public surface.
    - Prefer `data class`/`sealed class` over Kotlin-specific constructs that SKIE has to flatten.
- **No Java time APIs.** Use `kotlinx.datetime` for dates/durations.
- **Serialization**: Models that cross the network boundary live in `STNetwork`. Put pure value objects here only
  when they're shared by multiple modules.

## Conventions

- Copyright header on every file (see root `AGENTS.md`).
- Kotlin official style; one consecutive blank line max.
- Package root: `com.infomaniak.multiplatform_swisstransfer.common`.
- Tests in `commonTest` (preferred), Android-only fallbacks in `androidUnitTest`.

## Commands

```bash
# Build module (all targets)
./gradlew :STCommon:assemble

# Run common unit tests (Android variant – matches CI)
./gradlew :STCommon:testDebugUnitTest

# Run iOS simulator tests
./gradlew :STCommon:iosSimulatorArm64Test
```

## JIT Index

```bash
# All interfaces (the contract surface)
rg -n "^interface |^fun interface " STCommon/src/commonMain

# Domain exceptions
rg -n "class .*Exception" STCommon/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/common/exceptions

# Matomo tracking definitions
rg -n "object|const val" STCommon/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/common/matomo

# Shared utilities (date/list/api environment)
rg -n "fun " STCommon/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/common/utils
```

## Gotchas

- Don't introduce a transitive dependency on Realm/Room/Ktor in `commonMain` — it leaks into the public XCFramework
  and into every downstream module.
- Renaming a public symbol here is a breaking change for *all* consumers (Android Maven + iOS SPM). Coordinate with
  a `mavenVersionName` bump and a Package.swift update.
