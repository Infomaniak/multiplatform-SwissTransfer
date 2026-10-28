# Guest uploads with v2 API

## Questions

Do iOS devs of the team want deprecations or a breaking API straight-away?

## The plan

### Adding stuff to v2 based lib APIs
- Figure out whether we need app integrity
  - If so, find out where it should be added, and add it (conditionally, i.e. for guest users only)
- Add email validation for v2 in the outer lib API.

#### Files to touch

- [UploadV2Manager.kt](STCore/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/managers/UploadV2Manager.kt)
  - `requireCurrentUserId()` should probably not throw anymore when the user is not an `AuthUser`.
  - Get `prepareTransfer` to support app integrity check and email validation?
  - Should the `token` functional parameter stay like that?
- [UploadV2Repository.kt](STNetwork/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/network/repositories/UploadV2Repository.kt)
- [v2/UploadRequest.kt](STNetwork/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/network/requests/v2/UploadRequest.kt)
  - Should the `token` functional parameter stay like that?
  - Add email validation with the new backend API, possibly mimicking [the old UploadRequest.kt file](STNetwork/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/network/requests/UploadRequest.kt).

### Upload related stuff to remove

#### Classes with logic

- [InMemoryUploadManager](STCore/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/managers/InMemoryUploadManager.kt)
- [UploadManager](STCore/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/managers/UploadManager.kt)
- [UploadRepository](STNetwork/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/network/repositories/UploadRepository.kt)
- [requests/UploadRequest](STNetwork/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/network/requests/UploadRequest.kt)

#### Data classes

- [InitUploadBody](STNetwork/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/network/models/upload/request/InitUploadBody.kt)
- [UploadDestination](STCommon/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/common/interfaces/upload/UploadDestination.kt)
- [UploadFileRequest](STNetwork/src/commonMain/kotlin/com/infomaniak/multiplatform_swisstransfer/network/models/upload/request/UploadFileRequest.kt) + some things it references (the ones not used in v2)
