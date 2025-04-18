# SwissTransfer Core Module - Documentation

## Introduction

The Core module is an essential part of our cross-platform project, providing the necessary infrastructure to manage transfers and
downloads. The main class of this module is `SwissTransferInjection`, which centralizes access to all functionalities via lazily
initialized properties and methods. This class replaces traditional dependency injections.

## Installation

### Android

To install the Network module for Android, add the following dependency to your `build.gradle.kts` file:

```kts
implementation("com.github.infomaniak.multiplatform-SwissTransfer:STCore:{tag}")
```

### iOS

For iOS, the Network module is provided as part of the XCFramework. Follow these steps to integrate it into your Xcode project:

1. Download the XCFramework from the [releases page](https://github.com/infomaniak/multiplatform-SwissTransfer/releases).
2. Drag and drop the XCFramework into your Xcode project.
3. Make sure the framework is correctly linked in your target’s build settings.

## Main Class: SwissTransferInjection

### Description

`SwissTransferInjection` is responsible for initializing all the classes needed to manage transfers and downloads. It offers a
centralized access point to orchestrate transfer operations.

### Table of Public Properties and Methods

| Type     | Name                  | Description                                                                |
|----------|-----------------------|----------------------------------------------------------------------------|
| Property | userAgent             | Customize client api userAgent.                                            |
| Property | appSettingsManager    | A manager used to orchestrate AppSettings operations.                      |
| Property | transferManager       | A manager used to orchestrate Transfers operations.                        |
| Property | fileManager           | A manager used to orchestrate Files operations.                            |
| Property | accountManager        | A manager used to orchestrate Accounts operations.                         |
| Property | inMemoryUploadManager | A manager used to perform Uploads operations, without session persistence. |
| Property | uploadManager         | A manager used to orchestrate Uploads operations.                          |
| Property | emailTokensManager    | A manager used to orchestrate EmailTokens operations.                      |
| Property | sharedApiUrlCreator   | An utils to help use shared routes                                         |

### Details of Properties and Methods

#### Property: `appSettingsManager`

- **Type**: `AppSettingsManager`
- **Description**:
    - `appSettingsManager` is a lazily initialized property that provides a manager to orchestrate all AppSettings operations. It
      uses `realmProvider` to configure and manage AppSettings efficiently.

- **Usage Example**:
  ```kotlin
  val core = SwissTransferInjection(userAgent = "user_agent")
  val appSettingsManager = core.appSettingsManager
  // Use the appSettingsManager to orchestrate AppSettings
  ```

#### Property: `transferManager`

- **Type**: `TransferManager`
- **Description**:
    - `transferManager` is a lazily initialized property that provides a manager to orchestrate all transfer operations.

- **Usage Example**:
  ```kotlin
  val core = SwissTransferInjection(userAgent = "user_agent")
  val transferManager = core.transferManager
  // Use the transferManager to orchestrate Transfers
  ```

#### Property: `fileManager`

- **Type**: `FileManager`
- **Description**:
    - `fileManager` is a lazily initialized property that provides a manager to orchestrate all file operations.

- **Usage Example**:
  ```kotlin
  val core = SwissTransferInjection(userAgent = "user_agent")
  val fileManager = core.fileManager
  // Use the fileManager to orchestrate Files
  ```

#### Property: `accountManager`

- **Type**: `AccountManager`
- **Description**:
    - `accountManager` is a lazily initialized property that provides a manager to orchestrate all Accounts operations. It uses
      `appSettingsController`, `uploadController`, `transfersController` and `realmProvider` to configure and manage Accounts
      efficiently.

- **Usage Example**:
  ```kotlin
  val core = SwissTransferInjection(userAgent = "user_agent")
  val accountManager = core.accountManager
  // Use the accountManager to orchestrate Accounts
  ```

- **Type**: `UploadManager`
- **Description**:
    - `uploadManager` is a lazily initialized property that provides a manager to orchestrate all Uploads operations. It uses
      `uploadController` and `uploadRepository` to configure and manage Uploads efficiently.

- **Usage Example**:
  ```kotlin
  val core = SwissTransferInjection(userAgent = "user_agent")
  val uploadManager = core.uploadManager
  // Use the uploadManager to orchestrate Uploads
  ```

- **Type**: `EmailTokensManager`
- **Description**:
    - `emailTokensManager` is a lazily initialized property that provides a manager to orchestrate all EmailTokens operations. It
      uses `EmailTokensController` to do operations.

- **Usage Example**:
  ```kotlin
  val core = SwissTransferInjection(userAgent = "user_agent")
  val emailTokensManager = core.emailTokensManager
  // Use the EmailTokensManager to orchestrate EmailTokens
  ```

#### Property: `sharedApiUrlCreator`

- **Type**: `SharedApiUrlCreator`
- **Description**:
    - `sharedApiUrlCreator` is an utility class responsible for creating API URLs for shared routes.

- **Usage Example**:
  ```kotlin
  val core = SwissTransferInjection(userAgent = "user_agent")
  val accountManager = core.sharedApiUrlCreator
  ```

## Contributing

We welcome contributions to the SwissTransfer Network module! If you find a bug or want to add a new feature, please open an issue
or submit a pull request.

## License

This project is licensed under the GNU General Public License. See the LICENSE file for more details.
