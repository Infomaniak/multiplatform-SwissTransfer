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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.1/STCore.xcframework.zip",
            checksum: "6e5873533853fc5e606fc8d2d2a08720602cb5e6c69622c1de0d5a19d10c0833"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.1/STDatabase.xcframework.zip",
            checksum: "cb9dc865f996a814be6412225c0550161fe6db84e1d2a22f49f49fbad4e61340"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.1/STNetwork.xcframework.zip",
            checksum: "549fbe0b5079d966001f24b81fcaf0123f25b2a57d6edd64eb4c5dc9b92a26d2"
        ),
    ]
)
