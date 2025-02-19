// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "STCore", targets: ["Core"]),
        .library(name: "STDatabase", targets: ["Database"]),
        .library(name: "STNetwork", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.7/STCore.xcframework.zip",
            checksum: "87b7716e20142d7495588e60d6a11aa04bc39ec9f2d57f636a5a0715fc57dd88"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.7/STDatabase.xcframework.zip",
            checksum: "5fef22ba9bca931f9a09b0cb0dbfa4d6b61aa37fcd540a1d4a53fa828bdf8e30"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.7/STNetwork.xcframework.zip",
            checksum: "3712ab70a162e6cc32df909e7519525ea5d3fac42d71c7ec7d3afbff106e49b7"
        ),
    ]
)
