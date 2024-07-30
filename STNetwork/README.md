# SwissTransfer Network Module - Documentation

## Introduction

The Network module is a key component of the SwissTransfer Core project. It manages all network-related operations, utilizing the
API and providing repositories for transfer and upload operations.

## Installation

### Android

To install the Network module for Android, add the following dependency to your `build.gradle.kts` file:

```kts
implementation("com.github.infomaniak.multiplatform-SwissTransfer:STNetwork:{tag}")
```

### iOS

For iOS, the Network module is provided as part of the XCFramework. Follow these steps to integrate it into your Xcode project:

1. Download the XCFramework from the [releases page](https://github.com/infomaniak/multiplatform-SwissTransfer/releases).
2. Drag and drop the XCFramework into your Xcode project.
3. Make sure the framework is correctly linked in your target’s build settings.

## Overview

The Network module handles communication with external APIs, offering a streamlined way to manage data transfers and uploads. It
exposes two primary repositories: `TransferRepository` and `UploadRepository`, which can be used for orchestrating network
operations.

## Features

- **API Client Management**: Handles all API calls required for data transfer and upload.
- **Repositories**: Provides `TransferRepository` and `UploadRepository` for managing network operations.
- **Error Handling**: Implements robust error handling mechanisms for network requests.

## Repositories

### TransferRepository

Manages data transfers between the client and server.

#### Functions

- `getTransfer(linkUUID:String) -> ApiResponse<TransferApi>`: Get a transfer by linkUUID.

### UploadRepository

Handles file uploads to the server.

#### Functions

TODO

## Usage

Here’s an example of how to use the `TransferRepository` and `UploadRepository`:

#### Android

```kotlin
val apiClientProvider = ApiClientProvider()
val transferRepository = TransferRepository(apiClientProvider)
val uploadRepository = UploadRepository(apiClientProvider)

// Start a transfer
transferRepository.startTransfer()

// Upload a file
uploadRepository.uploadFile(file)
```

#### iOS

```swift
let transferRepository = TransferRepository()
let uploadRepository = UploadRepository()

// Start a transfer
transferRepository.startTransfer()

// Upload a file
uploadRepository.uploadFile(file)
```

## Contributing

We welcome contributions to the SwissTransfer Network module! If you find a bug or want to add a new feature, please open an issue
or submit a pull request.

## License

This project is licensed under the GNU General Public License. See the LICENSE file for more details.
