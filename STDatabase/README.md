# SwissTransfer Database Module - Documentation

## Introduction

The Database module is a critical part of the SwissTransfer Core project. It is responsible for managing all database operations
using Room, providing controllers for database interactions.

## Installation

### Android

To install the Database module for Android, add the following dependency to your `build.gradle.kts` file:

```kts
implementation("com.github.infomaniak.multiplatform-SwissTransfer:database:{tag}")
```

### iOS

For iOS, the Database module is provided as part of the XCFramework. Follow these steps to integrate it into your Xcode project:

1. Download the XCFramework from the [releases page](https://github.com/infomaniak/multiplatform-SwissTransfer/releases).
2. Drag and drop the XCFramework into your Xcode project.
3. Make sure the framework is correctly linked in your target’s build settings.

## Overview

The Database module handles database operations using Room, offering a structured way to manage data persistence. It exposes
various controllers to facilitate database interactions.

## Features

- **Room Integration**: Manages all database operations using Room.
- **Controllers**: Exposes controllers for efficient database management.
- **Data Persistence**: Ensures data is stored and retrieved efficiently.

## Contributing

We welcome contributions to the SwissTransfer Network module! If you find a bug or want to add a new feature, please open an issue
or submit a pull request.

## License

This project is licensed under the GNU General Public License. See the LICENSE file for more details.
