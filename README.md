# multiplatform-SwissTransfer Core Project

Core KMP for SwissTransfer application. This repository contains the centralized codebase for the SwissTransfer
project, implemented using Kotlin Multiplatform (KMP) to support both Android and iOS platforms.

## Overview

The SwissTransfer Core project is the heart of the SwissTransfer system, providing the essential infrastructure for network
operations, database management, and shared utilities. The core functionality is encapsulated in the `SwissTransferInjection`
model, which integrates various managers to simplify interactions with the database and API calls.

## Main Modules

This repository is structured into four main modules:

### 1. Network

**Description**:
The Network module handles all network-related operations by utilizing the API. It exposes repositories that can be used if this
module is integrated.

**Repositories**:

- `TransferRepository`
- `UploadRepository`

**Key Features**:

- Manages API calls
- Provides repositories for network operations

### 2. Database

**Description**:
The Database module is responsible for managing the database operations using Realm. It exposes controllers for database
interactions.

**Key Features**:

- Manages database operations with Realm
- Exposes controllers for database management

### 3. Common

**Description**:
The Common module is used internally and contains shared code that is utilized across other modules. It includes interfaces for
models and various utilities.

**Key Features**:

- Provides shared code and utilities
- Contains interfaces for models used across modules

### 4. Core

**Description**:
The Core module is the main module that leverages the other modules to expose the `SwissTransferInjection` model. This model
contains managers that abstract the logic of database operations and API calls. Users can simply call the manager to perform an
action without worrying about database storage or API calls.

**Key Features**:

- Integrates network and database modules
- Provides `SwissTransferInjection` for centralized management
- Abstracts complex operations into simple manager calls

## Installation

### Android

To install the Core module for Android, follow the next steps.

#### Step 1

Add the following dependency to your `settings.gradle.kts` file:

```kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
````

#### Step 2

Add the following dependency to your `build.gradle.kts` file:

```kts
implementation("com.github.infomaniak.multiplatform-SwissTransfer:core:{tag}")
```

`build.gradle` file:

```gradle
implementation 'com.github.infomaniak.multiplatform-SwissTransfer:core:{tag}'
```

For others modules:

```kts
implementation("com.github.infomaniak.multiplatform-SwissTransfer:{module_name}:{tag}")
```

### iOS

For iOS, the Core module is provided as an XCFramework. Follow these steps to integrate it into your Xcode project:

1. Download the XCFramework from the [releases page](https://github.com/infomaniak/multiplatform-SwissTransfer/releases).
2. Drag and drop the XCFramework into your Xcode project.
3. Make sure the framework is correctly linked in your target’s build settings.

## Getting Started

To get started with the SwissTransfer Core project, clone this repository and follow the setup instructions for your respective
platform (Android or iOS).

## Usage

Here’s a brief example of how to use the `SwissTransferInjection` class from the Core module:

```kotlin
val core = SwissTransferInjection()
core.loadDefaultAccount()

val manager = core.transferManager
// Use the manager to perform actions
```

## Release Process

To release a new version of the SwissTransfer Core project, you can use the `buildRelease` script. This script allows you to
specify the version and optionally limit the release to a specific platform (iOS or Android).

### Usage

```bash
./buildRelease <version> [--ios | --android]
```

- `<version>`: The version number in the format `x.x.x`. This parameter is required and cannot start with `-` or `--`.
- `--ios`: Optional. Release for the iOS platform only.
- `--android`: Optional. Release for the Android platform only.

### Examples

- Release for both platforms:

    ```bash
    ./buildRelease 1.2.3
    ```

- Release for iOS only:

    ```bash
    ./buildRelease 1.2.3 --ios
    ```

- Release for Android only:

    ```bash
    ./buildRelease 1.2.3 --android
    ```

If no parameters are provided or the parameters are incorrect, the script will display usage instructions.

### iOS Release Note

For iOS releases, once the release process is complete, the generated archives will be located in the **release** directory.
You will need to upload these archives as release assets on GitHub.

## Contributing

If you see a bug or an enhanceable point, feel free to create an issue, so that we can discuss about it, and once approved, we or
you (depending on the priority of the bug/improvement) will take care of the issue and apply a merge request.
Please, don't do a merge request before creating an issue.

## License

This project is licensed under the GNU General Public License. See the LICENSE file for more details.

```
Copyright (C) 2024 Infomaniak Network SA

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
```
