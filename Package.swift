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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/12.0.0/STCore.xcframework.zip",
            checksum: "4001553b7a599d86c313a243f3fb015b01e022c684eef43ef75325e134497bbb"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/12.0.0/STDatabase.xcframework.zip",
            checksum: "4bbc2fbd92c60b305a1797d6f8caf2eb3976ff28bef11e5aac3c24d4beace7a4"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/12.0.0/STNetwork.xcframework.zip",
            checksum: "1539c23cd60768569827aafa030d9135727bdcd3a518c7cfea5466b649598178"
        ),
    ]
)
