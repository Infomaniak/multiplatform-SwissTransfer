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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/7.1.1/STCore.xcframework.zip",
            checksum: "3b62f78a2ad1cc8da4e0a99e2489f3f617245715f4cd27de8e81be1254beec82"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/7.1.1/STDatabase.xcframework.zip",
            checksum: "d392f7b68f868301dfbbd6c3f37438eda7ebbc8884c4ffceeef9b82d16a9c992"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/7.1.1/STNetwork.xcframework.zip",
            checksum: "ac1c521e086a3a040dd4002ad0cf3eade544b11d2aea885b854909b14b33e466"
        ),
    ]
)
