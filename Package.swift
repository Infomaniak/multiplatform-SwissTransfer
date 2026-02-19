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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/6.2.0/STCore.xcframework.zip",
            checksum: "2d8a176232f3cbb16a47aa374d6d279335c31128a48b546bf287c2e6fef0cc52"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/6.2.0/STDatabase.xcframework.zip",
            checksum: "ab20de67b3c5199ca68fed8caaf22d3e5903ebb35dfa20d8fb2f5b3e371ae952"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/6.2.0/STNetwork.xcframework.zip",
            checksum: "5d94b2cf120a724af46bbc3730dc86d95fc73efea4682ec62f35e34bd208334c"
        ),
    ]
)
