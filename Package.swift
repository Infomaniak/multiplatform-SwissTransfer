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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.0.1/STCore.xcframework.zip",
            checksum: "4fe8b4e10103eee18a7888299209fa7508bb1520944bb2a47b9f42eb16fb94f0"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.0.1/STDatabase.xcframework.zip",
            checksum: "8883cc8656a595b55df470824f7c25f519193921c836647e73a9fe6fbe2a9d6c"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.0.1/STNetwork.xcframework.zip",
            checksum: "f0fcb39db1d94a5462c419418d8016edf2f9171a843cc8a060590549ee048c19"
        ),
    ]
)
