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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.6.0/STCore.xcframework.zip",
            checksum: "d26be667ec5258ec2e808a49f7e908de573a7130811f7a9d2cf3a62bfcf81723"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.6.0/STDatabase.xcframework.zip",
            checksum: "6026cde01f7570ac496fb873e7c0ec579306d51d64e38435b712968526c18e16"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.6.0/STNetwork.xcframework.zip",
            checksum: "d625d2911742f9aa84c8bae352adccf8f32ccb176abe2f1173025cb516884c1d"
        ),
    ]
)
