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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.1.1/STCore.xcframework.zip",
            checksum: "5beece112a10ba5e989c9a3b1ecfc479520e0611669546778a8e315bd2ac4487"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.1.1/STDatabase.xcframework.zip",
            checksum: "bf8890042967d108b4c9b4948ed9d3ea8104318e51ae566eb0834032631a427a"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.1.1/STNetwork.xcframework.zip",
            checksum: "6db648cb7a88e12acfd0308c8578ce5f4d4b4b31df8c9d236494705b002c2e00"
        ),
    ]
)
