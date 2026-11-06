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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.1.3/STCore.xcframework.zip",
            checksum: "6681a68de1eb9f70d9ba4e44ce65015196d87828ef69697d7d9a9d5e2e34a22d"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.1.3/STDatabase.xcframework.zip",
            checksum: "033c3c0f2768e00cdd9ae0ea6b6e0961f92788b61d1e26a46c066898286310d3"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.1.3/STNetwork.xcframework.zip",
            checksum: "91b657af0530a58adec8a4a7d721d9e41b380d8a25b864dc85ce9e19d5a7cdd6"
        ),
    ]
)
