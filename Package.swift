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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.1/STCore.xcframework.zip",
            checksum: "d8bde6d22afb0595d2d392bff15bc0c3612ac276f93708397a02e52d151c3a1a"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.1/STDatabase.xcframework.zip",
            checksum: "66577fb85575200927907b9cb9d35a9cec525476f9c9897571c63f30ffa0c9d0"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.1/STNetwork.xcframework.zip",
            checksum: "cf4bf51b2900c01d4e7feb93183bdc513b7d5db55c9a4a2cef6ca3d8bb144e67"
        ),
    ]
)
