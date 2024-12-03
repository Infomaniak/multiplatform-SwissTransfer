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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.3/STCore.xcframework.zip",
            checksum: "9aec9bb083a80efc8f4c6b7561a8923c0a99b2d43a91e3abe2371100448f6d4c"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.3/STDatabase.xcframework.zip",
            checksum: "12aeb4adec3a0e622ab18b5a8338ea69dbba40bb0dd057887754d2a82f25d997"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.3/STNetwork.xcframework.zip",
            checksum: "95c9b37ce09f2531caf3670bc346d4f63f2a90d55a5525a3adbc40d964a2d81b"
        ),
    ]
)
