# SwissTransfer Core Module - Documentation

## Introduction

The Core module is an essential part of our cross-platform project, providing the necessary infrastructure to manage transfers and
downloads. The main class of this module is `SwissTransferInjection`, which centralizes access to all functionalities via lazily
initialized properties and methods. This class replaces traditional dependency injections.

## Installation

### Android

To install the Network module for Android, add the following dependency to your `build.gradle.kts` file:

```kts
implementation("com.github.infomaniak.multiplatform-SwissTransfer:core:{tag}")
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

| Type     | Name               | Description                                                                   |
|----------|--------------------|-------------------------------------------------------------------------------|
| Property | transferManager    | A manager used to orchestrate transfer operations.                            |
| Method   | loadDefaultAccount | Loads the default user account and initializes Realm transfers for this user. |

### Details of Properties and Methods

#### Property: `transferManager`

- **Type**: `TransferManager`
- **Description**:
    - `transferManager` is a lazily initialized property that provides a manager to orchestrate all transfer operations. It
      uses `realmProvider` and `apiClientProvider` to configure and manage transfers efficiently.

- **Usage Example**:
  ```kotlin
  val core = SwissTransferInjection()
  val transferManager = core.transferManager
  // Use the transferManager to orchestrate transfers
  ```

#### Method: `loadDefaultAccount`

- **Signature**: `fun loadDefaultAccount()`
- **Description**:
    - `loadDefaultAccount` is a method that loads the default user account and initializes Realm transfers for the default user ID
      defined in the constants. This method is essential to ensure the application is correctly set up for the default user from
      the start.

- **Usage Example**:
  ```kotlin
  val core = SwissTransferInjection()
  core.loadDefaultAccount()
  // The default user account is now loaded and ready to use
  ```

## Contributing

We welcome contributions to the SwissTransfer Network module! If you find a bug or want to add a new feature, please open an issue
or submit a pull request.

## License

This project is licensed under the GNU General Public License. See the LICENSE file for more details.
