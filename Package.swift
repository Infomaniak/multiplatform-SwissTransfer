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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.0/STCore.xcframework.zip",
            checksum: "7d9eb2b2aa1e5ee96a9e00b1e02cdebcb839b167c817fe3bc6172f0ae38d5544"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.0/STDatabase.xcframework.zip",
            checksum: "23745bd75784dcaf62266f9e6dc353d2972cda98d78c61e54d42782af07e26a9"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.0/STNetwork.xcframework.zip",
            checksum: "0aabe06d38255daeef96e58050409c56cd06dfe506345152d7a2ad9a5cae9a83"
        ),
    ]
)
