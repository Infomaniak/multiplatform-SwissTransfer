# Guest uploads with v2 API

## Questions for the backend

1. I assume the `Re-send code` endpoint will always return the `id` that was passed in the path. Correct?
2. What is the `email_validation_request_locked` code for the `Validate email with code` endpoint? If it's a temporary backend issue, shouldn't it just be http 503?
3. Which error code for the `Create transfer` endpoint when the email isn't validated?
4. Can you confirm `Validate email with code` is idempotent?

## The plan

### Adding stuff to v2 based lib APIs
- Figure out whether we need app integrity
  - If so, find out where it should be added, and add it (conditionally, i.e. for guest users only)
- Add email validation for v2 in the outer lib API.

#### Email validation

1. **Create a validation request** (https://developer.infomaniak.com/docs/api/post/1/swisstransfer/email-validations)
   1. Returns a request id, and when it expires.
2. If needed, we can **re-send the code** with the initial id (https://developer.infomaniak.com/docs/api/post/1/swisstransfer/email-validations/%7Buuid%7D/resend)
   1. Returns a new id, and a new expiration timestamp
3. **Validate email with code** actually named "token" (https://developer.infomaniak.com/docs/api/post/1/swisstransfer/email-validations/%7Buuid%7D/confirm)
4. Check email validation if needed with **Validation request status** (https://developer.infomaniak.com/docs/api/get/1/swisstransfer/email-validations/%7Buuid%7D)

TODO: Add a Flow returning function that exposes the validation status for a given email,
so it's 100% crystal clear if the pending validation expired, and if we need to resend the code.
It might also expose the validationId to use.

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
