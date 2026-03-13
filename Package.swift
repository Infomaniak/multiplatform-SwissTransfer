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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/7.1.0/STCore.xcframework.zip",
            checksum: "b8e4a74d82755caa12ccc09cf797342604e79e8397daeff3b7e8fba5de317f29"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/7.1.0/STDatabase.xcframework.zip",
            checksum: "4e1fd6ac79b67911765738779d47919c94158d5c670f29dda9c4b8786369b2c3"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/7.1.0/STNetwork.xcframework.zip",
            checksum: "da24b17883bdb56aa8c4b72e99cc9a0df5fee8344b2c6b1cb2afe933f7032c37"
        ),
    ]
)
