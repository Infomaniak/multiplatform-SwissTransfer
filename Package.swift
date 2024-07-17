// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "Core", targets: ["Core"]),
        .library(name: "Database", targets: ["Database"]),
        .library(name: "Network", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/v0.0.1/Core.xcframework.zip",
            checksum: "core-xcframework-checksum"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/v0.0.1/Database.xcframework.zip",
            checksum: "database-xcframework-checksum"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/v0.0.1/Network.xcframework.zip",
            checksum: "network-xcframework-checksum"
        ),
    ]
)
