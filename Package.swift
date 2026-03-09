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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/7.0.0/STCore.xcframework.zip",
            checksum: "2c7b9efbf180108a84e1f61ecfba4a9d97331ef364c5a0b89f2e5f56fd2f4227"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/7.0.0/STDatabase.xcframework.zip",
            checksum: "da3e60a3966d5354a3790de310724d58af56e18df221c0bcedb81e49110eb1d9"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/7.0.0/STNetwork.xcframework.zip",
            checksum: "77be6ddb86e4576ba5d9d0f0d449e05e240b32a6fd7714413ad96dcd7868c1bc"
        ),
    ]
)
