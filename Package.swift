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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.4/STCore.xcframework.zip",
            checksum: "c24adac9483d65b5a81f6b9d5e9c3b16faf56db5c805998b789826436739818f"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.4/STDatabase.xcframework.zip",
            checksum: "cbd83562b4813751555437a9a6c6678d0f9645cf65a37ea83ef19096547f6395"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.4/STNetwork.xcframework.zip",
            checksum: "9320cdee2ae2c75d83389186c3b19525286addcc7e0b7ee6bf5168d7c14d28b5"
        ),
    ]
)
