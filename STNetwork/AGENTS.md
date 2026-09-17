# AGENTS.md - STNetwork

> For the repository overview, see the root `AGENTS.md`. See also `STCommon/AGENTS.md` for shared contracts, and
> `STCore/AGENTS.md` for how the network repositories are wired into `SwissTransferInjection`.

## Module Summary

`STNetwork` is the Ktor-based HTTP layer for SwissTransfer. It exposes:

- `ApiClientProvider` – Ktor `HttpClient` factory configured with the right engine per platform
  (OkHttp on Android, Darwin on iOS) and shared content negotiation / serialization setup.
- `UnauthorizedHandler` – cross-cutting hook invoked when the API returns 401-style errors.
- Repository classes used by the managers in `STCore`:
    - `TransferRepository` / `TransferV2Repository`
    - `UploadRepository` / `UploadV2Repository`
- Request DTOs (`network/requests/`, including `v2/`), response models (`network/models/`), and a typed exception
  hierarchy (`network/exceptions/`).

## Tech Stack & Dependencies

Declared in `STNetwork/build.gradle.kts`:

- **Ktor**: `client-core`, `client-content-negotiation`, `client-json`, `client-encoding`
- **Engines**: `ktor-client-okhttp` (`androidMain`), `ktor-client-darwin` (`iosMain`)
- **Serialization**: `kotlinx.serialization.json` (`api`)
- **SKIE** for Swift-friendly suspend/Flow bridging
- Test: `kotlin.test` and `ktor-client-mock`

It depends on `:STCommon` only.

## Layout

```
STNetwork/src/
├── commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/network/
│   ├── ApiClientProvider.kt          # Ktor HttpClient factory
│   ├── UnauthorizedHandler.kt
│   ├── repositories/                 # TransferRepository, UploadRepository (+ v2)
│   ├── requests/                     # Path / query builders. v2/ for the new API.
│   ├── models/                       # Network DTOs
│   │   ├── transfer/                 # +v2/
│   │   └── upload/                   # request/ and response/ (each with v2/)
│   ├── exceptions/                   # Typed network errors
│   ├── serializers/                  # Custom kotlinx.serialization serializers
│   └── utils/                        # ApiUrlMatcher, etc.
└── commonTest/kotlin/.../network/    # Unit tests using ktor-client-mock
```

## Local Norms

### HTTP client

- All HTTP traffic must go through the `HttpClient` produced by `ApiClientProvider`. Don't build ad-hoc Ktor clients
  in repositories – consistent timeouts, interceptors, JSON config and unauthorized handling depend on this.
- New endpoints should add a request in `network/requests/` (or `requests/v2/` for the v2 API) and be invoked from a
  repository — not directly from a manager in `STCore`.

### Error handling

- Network repositories MUST map non-2xx responses and engine failures to one of the typed exceptions in
  `network/exceptions/`:
    - `UnauthorizedException`, `TooManyRequestException`, `NetworkException`, `AttestationTokenException`,
      `ContainerErrorsException`, `EmailValidationException`, `FetchTransferException`, `UploadErrorsException`,
      `ApiException`.
- Reuse the existing `withUploadErrorHandling`-style wrappers (e.g. in `UploadV2Repository`) instead of duplicating
  `try/catch` ladders.
- Codes like `not_authorized` / `too_many_request` from `ApiV2ErrorException` are already mapped — keep the mapping
  authoritative inside the repository helpers.

### v1 vs v2

- The codebase is in the middle of a v1 → v2 migration. Endpoint variants live in dedicated `v2/` subpackages
  (`requests/v2/`, `models/.../v2/`, `repositories/*V2Repository.kt`). When extending a v2 flow, mirror that
  structure — do not retrofit changes into the v1 types.

### Serialization

- All DTOs are `@Serializable`. Use `@SerialName` whenever the JSON field name differs from the Kotlin property
  (`camelCase` in Kotlin, often `snake_case` over the wire).
- Put custom serializers in `network/serializers/` and register them on the JSON instance in `ApiClientProvider` so
  every client picks them up.

### URL helpers

- `ApiUrlMatcher` is strict by design: it only matches `https` URLs without query parameters, and `extractUUID`
  uses `substringAfterLast("/")`. Don't loosen these rules without coordinating with downstream consumers — many
  deep-link flows rely on this strictness.

## Conventions

- Copyright header required (see root `AGENTS.md`).
- Kotlin official style; max one consecutive blank line.
- Package root: `com.infomaniak.multiplatform_swisstransfer.network`.
- Public API surface is consumed by `STCore` and indirectly by Android / iOS apps via the XCFramework — keep it
  intentional. Prefer `internal` for helpers.

## Commands

```bash
# Build
./gradlew :STNetwork:assemble

# Tests (Android JVM variant + iOS simulator – matches CI)
./gradlew :STNetwork:testDebugUnitTest
./gradlew :STNetwork:iosSimulatorArm64Test

# iOS XCFramework
./gradlew :STNetwork:assembleSTNetworkReleaseXCFramework
```

## JIT Index

```bash
# Repositories and their public APIs
rg -n "class .*Repository|suspend fun " STNetwork/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/network/repositories

# Ktor request builders
rg -n "client\\.(get|post|put|delete|patch)|HttpClient" STNetwork/src/commonMain

# All network exceptions
rg -n "class .*Exception" STNetwork/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/network/exceptions

# Custom kotlinx.serialization serializers
rg -n "object .*Serializer|class .*Serializer" STNetwork/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/network/serializers

# v2 surface (new API)
rg -n "v2" STNetwork/src/commonMain --files-with-matches
```

## Gotchas

- Ktor engines are platform-specific: tests run on the JVM engine (mock), so a code path that works in
  `commonTest` may still fail on Darwin if you depend on JVM-only Ktor features.
- The Android engine is `okhttp`; do not add an additional engine module in `androidMain`.
- Don't add Android `Context`/`Application` dependencies in repositories — `STNetwork` is platform-agnostic.
- Do not block coroutine threads (`runBlocking`, `Thread.sleep`, …). The iOS XCFramework runs these on the main
  thread by default.
